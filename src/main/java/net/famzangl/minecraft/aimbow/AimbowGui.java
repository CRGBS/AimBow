package net.famzangl.minecraft.aimbow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.famzangl.minecraft.aimbow.aiming.ColissionData;
import net.famzangl.minecraft.aimbow.aiming.ColissionSolver;
import net.famzangl.minecraft.aimbow.aiming.Bow.ReverseBowSolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public final class AimbowGui {
    private static final double AUTO_AIM_RANGE=128.0D;
    private static final float AIM_SMOOTH=0.35F;
    private final Minecraft mc=Minecraft.getMinecraft();
    private final List<Vec3> trajectory=new ArrayList<Vec3>(200);
    private List<ColissionData> collisions=Collections.emptyList();
    private boolean autoAim;

    public boolean isAutoAim(){return autoAim;}
    public void setAutoAim(boolean value){autoAim=value;}

    @SubscribeEvent public void onTick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END || !autoAim || !AimBowMod.trajectoryState || mc.thePlayer==null || mc.theWorld==null) return;
        ItemStack item=mc.thePlayer.getItemInUse();
        if(item==null || item.getItem()!=Items.bow) return;
        ColissionSolver solver=ColissionSolver.forItem(item,mc);
        if(solver!=null) autoAim(solver);
    }

    @SubscribeEvent public void onWorldRender(RenderWorldLastEvent event){
        trajectory.clear(); collisions=Collections.emptyList();
        if(!AimBowMod.trajectoryState || mc.thePlayer==null || mc.theWorld==null) return;
        ColissionSolver solver=ColissionSolver.forItem(mc.thePlayer.getHeldItem(),mc);
        if(solver==null) return;
        collisions=solver.computeCurrentColissionPoints(trajectory);
        drawTrajectory();
        if(!collisions.isEmpty() && collisions.get(0).hitEntity==null) drawBlock(new BlockPos(collisions.get(0).x,collisions.get(0).y,collisions.get(0).z),event.partialTicks);
    }

    @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Post event){
        if(event.type!=RenderGameOverlayEvent.ElementType.ALL || mc.thePlayer==null || !AimBowMod.trajectoryState) return;
        ScaledResolution res=new ScaledResolution(mc);
        if(AimBowMod.blockDistanceState && !collisions.isEmpty()){
            ColissionData hit=collisions.get(0); double d=mc.thePlayer.getPositionEyes(event.partialTicks).distanceTo(new Vec3(hit.x,hit.y,hit.z));
            mc.fontRendererObj.drawStringWithShadow(String.format("Hit distance: %.1f",d),res.getScaledWidth()/2+10,res.getScaledHeight()/2-4,0xFFFFFF);
        }
        if(AimBowMod.crossHairState) drawCrosshair(res.getScaledWidth()/2-8,res.getScaledHeight()/2-8,!collisions.isEmpty() && collisions.get(0).hitEntity!=null);
    }

    private void drawTrajectory(){
        if(trajectory.size()<2) return;
        Entity viewer=mc.getRenderViewEntity(); if(viewer==null) return;
        double vx=mc.getRenderManager().viewerPosX,vy=mc.getRenderManager().viewerPosY,vz=mc.getRenderManager().viewerPosZ;
        GlStateManager.pushMatrix();
        try{
            GlStateManager.disableTexture2D(); GlStateManager.disableLighting(); GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770,771,1,0); GL11.glLineWidth(Math.max(1,AimBowMod.lineWidth));
            WorldRenderer wr=Tessellator.getInstance().getWorldRenderer(); wr.begin(GL11.GL_LINE_STRIP,DefaultVertexFormats.POSITION_COLOR);
            float r=AimBowMod.red/255F,g=AimBowMod.green/255F,b=AimBowMod.blue/255F,a=AimBowMod.alpha/255F;
            for(Vec3 p:trajectory) wr.pos(p.xCoord-vx,p.yCoord-vy,p.zCoord-vz).color(r,g,b,a).endVertex();
            Tessellator.getInstance().draw();
        }finally{GL11.glLineWidth(1);GlStateManager.color(1,1,1,1);GlStateManager.enableTexture2D();GlStateManager.disableBlend();GlStateManager.popMatrix();}
    }

    private void drawBlock(BlockPos pos,float partial){
        Entity viewer=mc.getRenderViewEntity(); if(viewer==null) return;
        double x=viewer.lastTickPosX+(viewer.posX-viewer.lastTickPosX)*partial;
        double y=viewer.lastTickPosY+(viewer.posY-viewer.lastTickPosY)*partial;
        double z=viewer.lastTickPosZ+(viewer.posZ-viewer.lastTickPosZ)*partial;
        AxisAlignedBB box=new AxisAlignedBB(pos.getX(),pos.getY(),pos.getZ(),pos.getX()+1,pos.getY()+1,pos.getZ()+1).expand(.002,.002,.002).offset(-x,-y,-z);
        GlStateManager.pushMatrix();
        try{GlStateManager.disableTexture2D();GlStateManager.enableBlend();GlStateManager.depthMask(false);GL11.glLineWidth(2);
            WorldRenderer wr=Tessellator.getInstance().getWorldRenderer();wr.begin(GL11.GL_LINES,DefaultVertexFormats.POSITION_COLOR);
            float r=AimBowMod.red/255F,g=AimBowMod.green/255F,b=AimBowMod.blue/255F,a=AimBowMod.alpha/255F;
            line(wr,box.minX,box.minY,box.minZ,box.maxX,box.minY,box.minZ,r,g,b,a);line(wr,box.maxX,box.minY,box.minZ,box.maxX,box.minY,box.maxZ,r,g,b,a);
            line(wr,box.maxX,box.minY,box.maxZ,box.minX,box.minY,box.maxZ,r,g,b,a);line(wr,box.minX,box.minY,box.maxZ,box.minX,box.minY,box.minZ,r,g,b,a);
            line(wr,box.minX,box.maxY,box.minZ,box.maxX,box.maxY,box.minZ,r,g,b,a);line(wr,box.maxX,box.maxY,box.minZ,box.maxX,box.maxY,box.maxZ,r,g,b,a);
            line(wr,box.maxX,box.maxY,box.maxZ,box.minX,box.maxY,box.maxZ,r,g,b,a);line(wr,box.minX,box.maxY,box.maxZ,box.minX,box.maxY,box.minZ,r,g,b,a);
            line(wr,box.minX,box.minY,box.minZ,box.minX,box.maxY,box.minZ,r,g,b,a);line(wr,box.maxX,box.minY,box.minZ,box.maxX,box.maxY,box.minZ,r,g,b,a);
            line(wr,box.maxX,box.minY,box.maxZ,box.maxX,box.maxY,box.maxZ,r,g,b,a);line(wr,box.minX,box.minY,box.maxZ,box.minX,box.maxY,box.maxZ,r,g,b,a);
            Tessellator.getInstance().draw();
        }finally{GL11.glLineWidth(1);GlStateManager.depthMask(true);GlStateManager.enableTexture2D();GlStateManager.disableBlend();GlStateManager.popMatrix();}
    }
    private void line(WorldRenderer w,double x1,double y1,double z1,double x2,double y2,double z2,float r,float g,float b,float a){w.pos(x1,y1,z1).color(r,g,b,a).endVertex();w.pos(x2,y2,z2).color(r,g,b,a).endVertex();}

    private void drawCrosshair(int x,int y,boolean hit){
        mc.getTextureManager().bindTexture(Gui.icons);GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(770,771,1,0);
        GlStateManager.color(hit?1F:0F,hit?0F:1F,0F,1F);drawTexturedModalRect(x,y,0,0,16,16);GlStateManager.color(1,1,1,1);GlStateManager.disableBlend();
    }
    private void drawTexturedModalRect(int x,int y,int u,int v,int w,int h){
        float s=1F/256F;WorldRenderer wr=Tessellator.getInstance().getWorldRenderer();wr.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX);
        wr.pos(x,y+h,0).tex(u*s,(v+h)*s).endVertex();wr.pos(x+w,y+h,0).tex((u+w)*s,(v+h)*s).endVertex();wr.pos(x+w,y,0).tex((u+w)*s,v*s).endVertex();wr.pos(x,y,0).tex(u*s,v*s).endVertex();Tessellator.getInstance().draw();
    }

    private void autoAim(ColissionSolver solver){
        EntityPlayerSP player=mc.thePlayer;AxisAlignedBB area=player.getEntityBoundingBox().expand(AUTO_AIM_RANGE,AUTO_AIM_RANGE/2,AUTO_AIM_RANGE);
        ReverseBowSolver reverse=new ReverseBowSolver(solver.getGravity(),solver.getVelocity());Vec3 current=player.getLook(1);Vec3 best=null;double bestAngle=Math.toRadians(30);
        for(Entity e:mc.theWorld.getEntitiesWithinAABB(Entity.class,area)){
            if(e==player || e.isDead || !e.canBeCollidedWith() || !(e instanceof EntityLivingBase) || ((EntityLivingBase)e).getHealth()<=0) continue;
            Vec3 look=reverse.getLookForTarget(e);if(look==null) continue;double len=look.lengthVector();if(len<1E-7) continue;look=look.normalize();
            List<ColissionData> test=solver.computeColissionWithLook(look);if(test.isEmpty() || test.get(0).hitEntity!=e) continue;
            double dot=Math.max(-1,Math.min(1,current.dotProduct(look)));double angle=Math.acos(dot);if(angle<bestAngle){bestAngle=angle;best=look;}
        }
        if(best!=null){float yaw=(float)Math.toDegrees(Math.atan2(best.zCoord,best.xCoord))-90F;float pitch=(float)-Math.toDegrees(Math.atan2(best.yCoord,Math.sqrt(best.xCoord*best.xCoord+best.zCoord*best.zCoord)));
            player.rotationYaw+=wrap(yaw-player.rotationYaw)*AIM_SMOOTH;player.rotationPitch+=(pitch-player.rotationPitch)*AIM_SMOOTH;player.rotationPitch=Math.max(-90,Math.min(90,player.rotationPitch));}
    }
    private float wrap(float a){a%=360;if(a>=180)a-=360;if(a< -180)a+=360;return a;}
}

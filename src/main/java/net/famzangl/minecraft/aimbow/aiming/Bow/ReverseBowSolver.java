package net.famzangl.minecraft.aimbow.aiming.Bow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public final class ReverseBowSolver {
    private static final int MAX_STEPS=200;
    private final float gravity,velocity;
    public ReverseBowSolver(float gravity,float velocity){this.gravity=gravity;this.velocity=velocity;}
    public Vec3 getLookForTarget(Entity target){
        EntityPlayerSP player=Minecraft.getMinecraft().thePlayer;
        if(player==null || target==null || velocity<=1.0E-4F) return null;
        AxisAlignedBB box=target.getEntityBoundingBox();
        double tx=(box.minX+box.maxX)*0.5D, ty=(box.minY+box.maxY)*0.5D, tz=(box.minZ+box.maxZ)*0.5D;
        double dx=tx-player.posX,dz=tz-player.posZ, horizontal=Math.sqrt(dx*dx+dz*dz);
        if(horizontal<1.0E-6D) return null;
        float vertical=getVertical((float)horizontal,(float)(ty-(player.posY+player.getEyeHeight())));
        float horizontalComponent=(float)Math.sqrt(Math.max(0.0D,1.0D-vertical*vertical));
        return new Vec3(dx/horizontal*horizontalComponent,vertical,dz/horizontal*horizontalComponent);
    }
    private float getVertical(float distance,float targetY){
        float low=-0.99F,high=0.99F;
        for(int i=0;i<40;i++){
            float mid=(low+high)*0.5F, horizontal=(float)Math.sqrt(1-mid*mid);
            float y=yAtDistance(horizontal*velocity,mid*velocity,distance);
            if(y>targetY) high=mid; else low=mid;
        }
        return (low+high)*0.5F;
    }
    private float yAtDistance(float mx,float my,float distance){
        float x=0,y=0;
        for(int i=0;i<MAX_STEPS && mx>1.0E-6F;i++){
            if(x+mx>=distance) return y+(distance-x)/mx*my;
            x+=mx;y+=my;mx*=0.99F;my=my*0.99F-gravity;
        }
        return y;
    }
}

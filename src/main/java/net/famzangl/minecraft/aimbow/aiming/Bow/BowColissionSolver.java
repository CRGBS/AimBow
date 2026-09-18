package net.famzangl.minecraft.aimbow.aiming.Bow;

import java.util.List;
import net.famzangl.minecraft.aimbow.aiming.ColissionSolver;
import net.famzangl.minecraft.aimbow.aiming.RayData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class BowColissionSolver extends ColissionSolver {
    private float lastForce=1.0F;
    public BowColissionSolver(Minecraft mc, EntityLivingBase shooter) { super(mc,shooter); }
    @Override protected MovingObjectPosition computeHit(RayData ray,int tick) {
        Vec3 start=new Vec3(ray.prevPosX,ray.prevPosY,ray.prevPosZ);
        Vec3 end=new Vec3(ray.posX,ray.posY,ray.posZ);
        MovingObjectPosition closest=minecraft.theWorld.rayTraceBlocks(start,end,false,true,false);
        if(closest!=null) end=closest.hitVec;
        double best=closest==null ? Double.MAX_VALUE : start.distanceTo(closest.hitVec);
        AxisAlignedBB area=ray.boundingBox.addCoord(ray.motionX,ray.motionY,ray.motionZ).expand(1,1,1);
        List<Entity> entities=minecraft.theWorld.getEntitiesWithinAABB(Entity.class,area);
        for(Entity entity:entities) {
            if(!entity.canBeCollidedWith() || (entity==shootingEntity && tick<5)) continue;
            MovingObjectPosition candidate=entity.getEntityBoundingBox().expand(0.3D,0.3D,0.3D).calculateIntercept(start,end);
            if(candidate!=null) {
                double distance=start.distanceTo(candidate.hitVec);
                if(distance<best) { candidate.entityHit=entity; closest=candidate; best=distance; }
            }
        }
        return closest;
    }
    @Override public float getVelocity() { return lastForce*1.5F; }
    @Override protected RayData generateRayData() {
        ItemStack using=minecraft.thePlayer==null ? null : minecraft.thePlayer.getItemInUse();
        if(using!=null && using.getItem() instanceof ItemBow) {
            int duration=minecraft.thePlayer.getItemInUseDuration();
            float draw=duration/20.0F; draw=(draw*draw+draw*2.0F)/3.0F;
            lastForce=Math.min(draw,1.0F);
        } else lastForce=1.0F;
        return new BowRayData(lastForce);
    }
}

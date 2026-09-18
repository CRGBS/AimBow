package net.famzangl.minecraft.aimbow.aiming;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.famzangl.minecraft.aimbow.aiming.Bow.BowCollisionSolver;
import net.famzangl.minecraft.aimbow.aiming.EnderPearl.EnderPearlCollisionSolver;
import net.famzangl.minecraft.aimbow.aiming.Fishing.FishingCollisionSolver;
import net.famzangl.minecraft.aimbow.aiming.Potion.PotionCollisionSolver;
import net.famzangl.minecraft.aimbow.aiming.Throwables.ThrowableCollisionSolver;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
public abstract class CollisionSolver {
    protected final Minecraft minecraft; protected final EntityLivingBase shootingEntity;
    protected CollisionSolver(Minecraft mc,EntityLivingBase shooter){minecraft=mc;shootingEntity=shooter;}
    protected abstract RayData generateRayData(); public abstract float getVelocity();
    protected MovingObjectPosition computeProjectileHit(RayData ray,int tick,float border){
        Vec3 start=new Vec3(ray.prevPosX,ray.prevPosY,ray.prevPosZ),end=new Vec3(ray.posX,ray.posY,ray.posZ);
        MovingObjectPosition bestHit=minecraft.theWorld.rayTraceBlocks(start,end,false,true,false);
        Vec3 entityEnd=bestHit==null?end:bestHit.hitVec; double best=bestHit==null?Double.MAX_VALUE:start.distanceTo(bestHit.hitVec);
        AxisAlignedBB area=ray.boundingBox.addCoord(ray.motionX,ray.motionY,ray.motionZ).expand(1,1,1);
        List<Entity> entities=minecraft.theWorld.getEntitiesWithinAABB(Entity.class,area);
        for(Entity entity:entities){if(!entity.canBeCollidedWith()||(entity==shootingEntity&&tick<5))continue;MovingObjectPosition hit=entity.getEntityBoundingBox().expand(border,border,border).calculateIntercept(start,entityEnd);if(hit!=null){double d=start.distanceTo(hit.hitVec);if(d<best){hit.entityHit=entity;bestHit=hit;best=d;}}}return bestHit;
    }
    public List<CollisionData> computeCurrentCollisionPoints(List<Vec3> trajectory){Entity view=minecraft.getRenderViewEntity();if(view==null||minecraft.theWorld==null)return Collections.emptyList();RayData ray=generateRayData();if(ray==null)return Collections.emptyList();ray.setTrajectorySink(trajectory);ray.shootFrom(view);return run(ray);}
    public List<CollisionData> computeCollisionWithLook(Vec3 look){if(shootingEntity==null||look==null||minecraft.theWorld==null)return Collections.emptyList();RayData ray=generateRayData();if(ray==null)return Collections.emptyList();ray.shootFromTowards(shootingEntity,look);return run(ray);}
    private List<CollisionData> run(RayData ray){List<CollisionData> out=new ArrayList<CollisionData>(1);for(int i=0;i<200&&!ray.isDead();i++){ray.moveTick();MovingObjectPosition hit=computeProjectileHit(ray,i,.3F);if(hit!=null&&hit.hitVec!=null){out.add(new CollisionData(hit.hitVec.xCoord,hit.hitVec.yCoord,hit.hitVec.zCoord,hit.entityHit,i,hit.sideHit,hit.typeOfHit));ray.setDead(true);}}return out;}
    public float getGravity(){RayData r=generateRayData();return r==null?.03F:r.getGravity();}
    public static CollisionSolver forItem(ItemStack s,Minecraft mc){if(s==null||mc==null||!(mc.getRenderViewEntity() instanceof EntityLivingBase))return null;EntityLivingBase e=(EntityLivingBase)mc.getRenderViewEntity();if(s.getItem()==Items.snowball||s.getItem()==Items.egg)return new ThrowableCollisionSolver(mc,e);if(s.getItem()==Items.ender_pearl)return new EnderPearlCollisionSolver(mc,e);if(s.getItem()==Items.experience_bottle||(s.getItem()==Items.potionitem&&ItemPotion.isSplash(s.getMetadata())))return new PotionCollisionSolver(mc,e);if(s.getItem()==Items.bow)return new BowCollisionSolver(mc,e);if(s.getItem()==Items.fishing_rod)return new FishingCollisionSolver(mc,e);return null;}
}

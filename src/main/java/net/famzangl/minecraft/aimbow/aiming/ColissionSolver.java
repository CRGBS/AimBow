package net.famzangl.minecraft.aimbow.aiming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.famzangl.minecraft.aimbow.aiming.Bow.BowColissionSolver;
import net.famzangl.minecraft.aimbow.aiming.EnderPearl.EnderPearlColissionSolver;
import net.famzangl.minecraft.aimbow.aiming.Fishing.FishingColissionSolver;
import net.famzangl.minecraft.aimbow.aiming.Potion.PotionColissionSolver;
import net.famzangl.minecraft.aimbow.aiming.Throwables.ThrowableColissionSolver;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public abstract class ColissionSolver {
    private static final int MAX_TICKS = 200;
    protected final Minecraft minecraft;
    protected final EntityLivingBase shootingEntity;

    protected ColissionSolver(Minecraft mc, EntityLivingBase shooter) { minecraft=mc; shootingEntity=shooter; }
    protected abstract MovingObjectPosition computeHit(RayData ray, int tick);
    protected abstract RayData generateRayData();
    public abstract float getVelocity();

    public List<ColissionData> computeCurrentColissionPoints(List<Vec3> trajectory) {
        Entity view = minecraft.getRenderViewEntity();
        if (view == null || minecraft.theWorld == null) return Collections.emptyList();
        RayData ray = generateRayData();
        if (ray == null) return Collections.emptyList();
        ray.setTrajectorySink(trajectory); ray.shootFrom(view); return run(ray);
    }
    public List<ColissionData> computeCurrentColissionPoints() { return computeCurrentColissionPoints(new ArrayList<Vec3>()); }
    public List<ColissionData> computeColissionWithLook(Vec3 look) {
        if (shootingEntity == null || look == null || minecraft.theWorld == null) return Collections.emptyList();
        RayData ray=generateRayData(); if (ray==null) return Collections.emptyList();
        ray.shootFromTowards(shootingEntity, look); return run(ray);
    }
    private List<ColissionData> run(RayData ray) {
        List<ColissionData> hits=new ArrayList<ColissionData>(1);
        for(int tick=0; tick<MAX_TICKS && !ray.isDead(); tick++) {
            ray.moveTick();
            MovingObjectPosition hit=computeHit(ray,tick);
            if(hit!=null && hit.hitVec!=null) {
                hits.add(new ColissionData(hit.hitVec.xCoord,hit.hitVec.yCoord,hit.hitVec.zCoord,hit.entityHit,tick));
                ray.setDead(true);
            }
        }
        return hits;
    }
    public static ColissionSolver forItem(ItemStack stack, Minecraft mc) {
        if(stack==null || mc==null || !(mc.getRenderViewEntity() instanceof EntityLivingBase)) return null;
        EntityLivingBase shooter=(EntityLivingBase)mc.getRenderViewEntity();
        if(stack.getItem()==Items.snowball || stack.getItem()==Items.egg) return new ThrowableColissionSolver(mc,shooter);
        if(stack.getItem()==Items.ender_pearl) return new EnderPearlColissionSolver(mc,shooter);
        if(stack.getItem()==Items.experience_bottle || (stack.getItem()==Items.potionitem && ItemPotion.isSplash(stack.getMetadata()))) return new PotionColissionSolver(mc,shooter);
        if(stack.getItem()==Items.bow) return new BowColissionSolver(mc,shooter);
        if(stack.getItem()==Items.fishing_rod) return new FishingColissionSolver(mc,shooter);
        return null;
    }
    public float getGravity() { RayData ray=generateRayData(); return ray==null ? 0.03F : ray.getGravity(); }
}

package net.famzangl.minecraft.aimbow.aiming.Fishing;
import net.famzangl.minecraft.aimbow.aiming.CollisionSolver;
import net.famzangl.minecraft.aimbow.aiming.RayData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
public final class FishingCollisionSolver extends CollisionSolver {
    public FishingCollisionSolver(Minecraft mc,EntityLivingBase e){super(mc,e);}
    protected RayData generateRayData(){return new FishingRayData();}
    public float getVelocity(){return 0.9F;}
}

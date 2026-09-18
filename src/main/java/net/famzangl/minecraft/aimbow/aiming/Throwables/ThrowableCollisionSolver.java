package net.famzangl.minecraft.aimbow.aiming.Throwables;
import net.famzangl.minecraft.aimbow.aiming.CollisionSolver;
import net.famzangl.minecraft.aimbow.aiming.RayData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
public final class ThrowableCollisionSolver extends CollisionSolver {
    public ThrowableCollisionSolver(Minecraft mc,EntityLivingBase e){super(mc,e);}
    protected RayData generateRayData(){return new ThrowableRayData();}
    public float getVelocity(){return 1.5F;}
}

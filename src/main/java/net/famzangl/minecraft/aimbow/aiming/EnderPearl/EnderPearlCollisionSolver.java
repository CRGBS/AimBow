package net.famzangl.minecraft.aimbow.aiming.EnderPearl;
import net.famzangl.minecraft.aimbow.aiming.CollisionSolver;
import net.famzangl.minecraft.aimbow.aiming.RayData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
public final class EnderPearlCollisionSolver extends CollisionSolver {
    public EnderPearlCollisionSolver(Minecraft mc,EntityLivingBase e){super(mc,e);}
    protected RayData generateRayData(){return new EnderPearlRayData();}
    public float getVelocity(){return 1.5F;}
}

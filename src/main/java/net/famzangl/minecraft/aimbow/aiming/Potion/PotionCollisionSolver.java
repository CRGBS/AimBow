package net.famzangl.minecraft.aimbow.aiming.Potion;
import net.famzangl.minecraft.aimbow.aiming.CollisionSolver;
import net.famzangl.minecraft.aimbow.aiming.RayData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
public final class PotionCollisionSolver extends CollisionSolver {
    public PotionCollisionSolver(Minecraft mc,EntityLivingBase e){super(mc,e);}
    protected RayData generateRayData(){return new PotionRayData();}
    public float getVelocity(){return 0.7F;}
}

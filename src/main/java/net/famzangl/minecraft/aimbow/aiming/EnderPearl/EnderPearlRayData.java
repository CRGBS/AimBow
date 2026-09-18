package net.famzangl.minecraft.aimbow.aiming.EnderPearl;

import net.famzangl.minecraft.aimbow.aiming.RayData;
import net.minecraft.util.MathHelper;

public final class EnderPearlRayData extends RayData {
    @Override protected float getGravity() { return 0.03F; }
    @Override public void shoot() {
        posX -= MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * 0.16F;
        posY -= 0.10000000149D;
        posZ -= MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * 0.16F;
        setPosition(posX, posY, posZ);
        float base = 0.4F;
        motionX = -MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float)Math.PI) * base;
        motionZ = MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float)Math.PI) * base;
        motionY = -MathHelper.sin(rotationPitch / 180.0F * (float)Math.PI) * base;
        setThrowableHeading(motionX, motionY, motionZ, 1.5F, 0.0F);
    }
}

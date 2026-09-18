package net.famzangl.minecraft.aimbow.aiming.Bow;
import net.famzangl.minecraft.aimbow.aiming.RayData;
import net.minecraft.util.MathHelper;
public final class BowRayData extends RayData {private final float force;public BowRayData(float f){force=f;}public float getGravity(){return .05F;}public void shoot(){posX-=MathHelper.cos(rotationYaw/180F*(float)Math.PI)*.16F;posY-=.10000000149D;posZ-=MathHelper.sin(rotationYaw/180F*(float)Math.PI)*.16F;setPosition(posX,posY,posZ);double x=-MathHelper.sin(rotationYaw/180F*(float)Math.PI)*MathHelper.cos(rotationPitch/180F*(float)Math.PI);double z=MathHelper.cos(rotationYaw/180F*(float)Math.PI)*MathHelper.cos(rotationPitch/180F*(float)Math.PI);double y=-MathHelper.sin(rotationPitch/180F*(float)Math.PI);setThrowableHeading(x,y,z,force*1.5F,0);}}

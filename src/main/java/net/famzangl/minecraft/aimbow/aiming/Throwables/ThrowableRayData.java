package net.famzangl.minecraft.aimbow.aiming.Throwables;
import net.famzangl.minecraft.aimbow.aiming.RayData;
import net.minecraft.util.MathHelper;
public final class ThrowableRayData extends RayData {
    public float getGravity(){return 0.03F;}
    public void shoot(){posX-=MathHelper.cos(rotationYaw/180F*(float)Math.PI)*.16F;posY-=.10000000149D;posZ-=MathHelper.sin(rotationYaw/180F*(float)Math.PI)*.16F;setPosition(posX,posY,posZ);float f=.4F;double x=-MathHelper.sin(rotationYaw/180F*(float)Math.PI)*MathHelper.cos(rotationPitch/180F*(float)Math.PI)*f;double z=MathHelper.cos(rotationYaw/180F*(float)Math.PI)*MathHelper.cos(rotationPitch/180F*(float)Math.PI)*f;double y=-MathHelper.sin(rotationPitch/180F*(float)Math.PI)*f;setThrowableHeading(x,y,z,1.5F,0);}
}

package net.famzangl.minecraft.aimbow.aiming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public abstract class RayData extends TickingEntity {
    private static final Random RANDOM = new Random();
    protected boolean dead;
    protected float prevRotationYaw, prevRotationPitch;
    private List<Vec3> trajectorySink = Collections.emptyList();

    public void setTrajectorySink(List<Vec3> sink) { trajectorySink = sink == null ? Collections.<Vec3>emptyList() : sink; }
    protected final void recordPosition() { trajectorySink.add(new Vec3(posX, posY, posZ)); }

    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        rotationYaw = yaw; rotationPitch = pitch; setPosition(x, y, z);
    }
    public void shootFromTowards(Entity entity, Vec3 look) {
        float yaw = (float)Math.toDegrees(Math.atan2(look.zCoord, look.xCoord)) - 90.0F;
        float pitch = (float)-Math.toDegrees(Math.atan2(look.yCoord, Math.sqrt(look.xCoord * look.xCoord + look.zCoord * look.zCoord)));
        setLocationAndAngles(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ, yaw, pitch); shoot();
    }
    public void shootFrom(Entity entity) {
        setLocationAndAngles(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ, entity.rotationYaw, entity.rotationPitch); shoot();
    }
    @Override public void moveTick() {
        super.moveTick();
        float horizontal = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
        rotationYaw = (float)Math.toDegrees(Math.atan2(motionX, motionZ));
        rotationPitch = (float)Math.toDegrees(Math.atan2(motionY, horizontal));
        motionX *= 0.99F; motionY *= 0.99F; motionZ *= 0.99F; motionY -= getGravity();
        setPosition(posX, posY, posZ);
        recordPosition();
    }
    protected abstract float getGravity();
    public abstract void shoot();
    public void setThrowableHeading(double x, double y, double z, double force, float randomInfluence) {
        double length = Math.sqrt(x*x + y*y + z*z);
        if (length < 1.0E-7D) { motionX = motionY = motionZ = 0; return; }
        x /= length; y /= length; z /= length;
        if (randomInfluence != 0) {
            x += RANDOM.nextGaussian() * 0.0075D * randomInfluence;
            y += RANDOM.nextGaussian() * 0.0075D * randomInfluence;
            z += RANDOM.nextGaussian() * 0.0075D * randomInfluence;
        }
        motionX=x*force; motionY=y*force; motionZ=z*force;
        float horizontal = MathHelper.sqrt_double(motionX*motionX + motionZ*motionZ);
        prevRotationYaw = rotationYaw = (float)Math.toDegrees(Math.atan2(motionX, motionZ));
        prevRotationPitch = rotationPitch = (float)Math.toDegrees(Math.atan2(motionY, horizontal));
    }
    public boolean isDead() { return dead; }
    public void setDead(boolean value) { dead = value; }
}

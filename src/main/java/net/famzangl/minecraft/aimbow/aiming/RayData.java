package net.famzangl.minecraft.aimbow.aiming;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
public abstract class RayData extends TickingEntity {
    private static final Random RANDOM=new Random(); private boolean dead; private List<Vec3> sink=Collections.emptyList();
    protected float prevRotationYaw,prevRotationPitch;
    public void setTrajectorySink(List<Vec3> s){sink=s==null?Collections.<Vec3>emptyList():s;}
    public void setLocationAndAngles(double x,double y,double z,float yaw,float pitch){rotationYaw=yaw;rotationPitch=pitch;setPosition(x,y,z);}
    public void shootFrom(Entity e){setLocationAndAngles(e.posX,e.posY+e.getEyeHeight(),e.posZ,e.rotationYaw,e.rotationPitch);shoot();}
    public void shootFromTowards(Entity e,Vec3 look){float yaw=(float)Math.toDegrees(Math.atan2(look.zCoord,look.xCoord))-90F;float pitch=(float)-Math.toDegrees(Math.atan2(look.yCoord,Math.sqrt(look.xCoord*look.xCoord+look.zCoord*look.zCoord)));setLocationAndAngles(e.posX,e.posY+e.getEyeHeight(),e.posZ,yaw,pitch);shoot();}
    public void moveTick(){super.moveTick();prevRotationYaw=rotationYaw;prevRotationPitch=rotationPitch;float h=MathHelper.sqrt_double(motionX*motionX+motionZ*motionZ);rotationYaw=(float)Math.toDegrees(Math.atan2(motionX,motionZ));rotationPitch=(float)Math.toDegrees(Math.atan2(motionY,h));motionX*=.99F;motionY*=.99F;motionZ*=.99F;motionY-=getGravity();setPosition(posX,posY,posZ);sink.add(new Vec3(posX,posY,posZ));}
    public void setThrowableHeading(double x,double y,double z,double force,float random){double len=Math.sqrt(x*x+y*y+z*z);if(len<1E-7){dead=true;return;}x/=len;y/=len;z/=len;if(random!=0){x+=RANDOM.nextGaussian()*.0075*random;y+=RANDOM.nextGaussian()*.0075*random;z+=RANDOM.nextGaussian()*.0075*random;}motionX=x*force;motionY=y*force;motionZ=z*force;}
    public boolean isDead(){return dead;} public void setDead(boolean d){dead=d;} public abstract void shoot(); public abstract float getGravity();
}

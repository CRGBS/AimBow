package net.famzangl.minecraft.aimbow.aiming;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
public final class CollisionData {
    public final double x,y,z; public final Entity hitEntity; public final int hitStep;
    public final EnumFacing sideHit; public final MovingObjectPosition.MovingObjectType hitType;
    public CollisionData(double x,double y,double z,Entity entity,int step,EnumFacing side,MovingObjectPosition.MovingObjectType type){
        this.x=x;this.y=y;this.z=z;this.hitEntity=entity;this.hitStep=step;this.sideHit=side;this.hitType=type;
    }
    public String toString(){return "CollisionData[x="+x+", y="+y+", z="+z+", entity="+hitEntity+", step="+hitStep+", side="+sideHit+"]";}
}

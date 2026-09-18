package net.famzangl.minecraft.aimbow.aiming.Bow;
import net.famzangl.minecraft.aimbow.aiming.CollisionSolver;
import net.famzangl.minecraft.aimbow.aiming.RayData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
public final class BowCollisionSolver extends CollisionSolver {private float force;public BowCollisionSolver(Minecraft mc,EntityLivingBase e){super(mc,e);}protected RayData generateRayData(){ItemStack s=minecraft.thePlayer==null?null:minecraft.thePlayer.getItemInUse();if(s!=null&&s.getItem() instanceof ItemBow){float f=minecraft.thePlayer.getItemInUseDuration()/20F;f=(f*f+2F*f)/3F;force=Math.min(f,1F);}else force=1F;return new BowRayData(force);}public float getVelocity(){return force*1.5F;}public float getForce(){generateRayData();return force;}}

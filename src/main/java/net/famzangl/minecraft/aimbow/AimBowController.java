package net.famzangl.minecraft.aimbow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
public final class AimBowController {
    private final KeyBinding aim=new KeyBinding("Auto aim",Keyboard.KEY_Y,"AimBow");
    private final KeyBinding trajectory=new KeyBinding("Toggle trajectory",Keyboard.KEY_J,"AimBow");
    private final AimbowGui gui; private boolean initialized;
    public AimBowController(AimbowGui gui){this.gui=gui;}
    public void initialize(){if(initialized)return;ClientRegistry.registerKeyBinding(aim);ClientRegistry.registerKeyBinding(trajectory);FMLCommonHandler.instance().bus().register(this);initialized=true;}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){Minecraft mc=Minecraft.getMinecraft();if(e.phase!=TickEvent.Phase.END||mc.thePlayer==null||mc.currentScreen!=null)return;
        while(aim.isPressed()){if(!AimBowMod.trajectoryState)chat("Enable trajectory first");else{gui.setAutoAim(!gui.isAutoAim());chat("Auto aim: "+(gui.isAutoAim()?"On":"Off"));}}
        while(trajectory.isPressed()){AimBowMod.trajectoryState=!AimBowMod.trajectoryState;if(!AimBowMod.trajectoryState)gui.setAutoAim(false);AimBowMod.saveConfig();chat("Trajectory: "+(AimBowMod.trajectoryState?"On":"Off"));}}
    private void chat(String s){Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[AimBow] "+s));}
}

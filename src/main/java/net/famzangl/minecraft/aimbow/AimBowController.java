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
    private final KeyBinding autoAimKey = new KeyBinding("Auto aim", Keyboard.KEY_Y, "AimBow");
    private final KeyBinding trajectoryKey = new KeyBinding("Toggle trajectory", Keyboard.KEY_J, "AimBow");
    private final AimbowGui gui;
    private boolean initialized;

    public AimBowController(AimbowGui gui) { this.gui = gui; }

    public void initialize() {
        if (initialized) return;
        ClientRegistry.registerKeyBinding(autoAimKey);
        ClientRegistry.registerKeyBinding(trajectoryKey);
        FMLCommonHandler.instance().bus().register(this);
        initialized = true;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || mc.currentScreen != null) return;
        while (autoAimKey.isPressed()) {
            if (!AimBowMod.trajectoryState) chat("Enable trajectory first: /aimbow");
            else { gui.setAutoAim(!gui.isAutoAim()); chat("Auto aim: " + (gui.isAutoAim() ? "On" : "Off")); }
        }
        while (trajectoryKey.isPressed()) {
            AimBowMod.trajectoryState = !AimBowMod.trajectoryState;
            if (!AimBowMod.trajectoryState) gui.setAutoAim(false);
            AimBowMod.saveConfig();
            chat("Trajectory: " + (AimBowMod.trajectoryState ? "On" : "Off"));
        }
    }

    private void chat(String text) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[AimBow] " + text));
    }
}

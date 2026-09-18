package net.famzangl.minecraft.aimbow;

import java.io.File;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = AimBowMod.MOD_ID, name = "AimBow", version = AimBowMod.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public final class AimBowMod {
    public static final String MOD_ID = "AimBow";
    public static final String VERSION = "0.1.1";
    private static Configuration config;

    public static int red = 255, green = 255, blue = 255, alpha = 255, lineWidth = 3;
    public static boolean crossHairState, blockDistanceState, trajectoryState = true;
    public static AimbowGui gui;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        config = new Configuration(new File("config/AimBowColorGui.cfg"));
        loadConfig();
        gui = new AimbowGui();
        MinecraftForge.EVENT_BUS.register(gui);
        new AimBowController(gui).initialize();
        ClientCommandHandler.instance.registerCommand(new AimBowCommand());
    }

    private static void loadConfig() {
        config.load();
        red = config.getInt("Red", "Color", 255, 0, 255, "Red component");
        green = config.getInt("Green", "Color", 255, 0, 255, "Green component");
        blue = config.getInt("Blue", "Color", 255, 0, 255, "Blue component");
        alpha = config.getInt("Alpha", "Color", 255, 0, 255, "Alpha component");
        lineWidth = config.getInt("Width", "Color", 3, 1, 10, "Trajectory line width");
        crossHairState = config.getBoolean("CrossHairState", "General", false, "Draw custom hit crosshair");
        blockDistanceState = config.getBoolean("BlockDistance", "General", false, "Display hit distance");
        trajectoryState = config.getBoolean("Trajectory", "General", true, "Display projectile trajectory");
        if (config.hasChanged()) config.save();
    }

    public static void saveConfig() {
        if (config == null) return;
        config.get("Color", "Red", 255).set(red);
        config.get("Color", "Green", 255).set(green);
        config.get("Color", "Blue", 255).set(blue);
        config.get("Color", "Alpha", 255).set(alpha);
        config.get("Color", "Width", 3).set(lineWidth);
        config.get("General", "CrossHairState", false).set(crossHairState);
        config.get("General", "BlockDistance", false).set(blockDistanceState);
        config.get("General", "Trajectory", true).set(trajectoryState);
        config.save();
    }
}

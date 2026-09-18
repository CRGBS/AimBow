package net.famzangl.minecraft.aimbow;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public final class AimBowCommandGui extends GuiScreen {
    private GuiSlider redSlider, greenSlider, blueSlider, alphaSlider, widthSlider;
    private GuiButton trajectoryButton, crosshairButton, blockDistanceButton;
    private boolean trajectoryState, crosshairState, blockDistanceState;

    public AimBowCommandGui() {
        trajectoryState = AimBowMod.trajectoryState;
        crosshairState = AimBowMod.crossHairState;
        blockDistanceState = AimBowMod.blockDistanceState;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int x = width / 2 - 100;
        int y = height / 2 - 100;
        trajectoryButton = new GuiButton(7, x, y, 200, 20, trajectoryText());
        redSlider = slider(0, x, y + 25, "Red: ", 0, 255, AimBowMod.red);
        greenSlider = slider(1, x, y + 50, "Green: ", 0, 255, AimBowMod.green);
        blueSlider = slider(2, x, y + 75, "Blue: ", 0, 255, AimBowMod.blue);
        alphaSlider = slider(3, x, y + 100, "Alpha: ", 0, 255, AimBowMod.alpha);
        widthSlider = slider(4, x, y + 125, "Width: ", 1, 10, AimBowMod.lineWidth);
        crosshairButton = new GuiButton(5, x, y + 150, 200, 20, crosshairText());
        blockDistanceButton = new GuiButton(6, x, y + 175, 200, 20, distanceText());
        buttonList.add(trajectoryButton);
        buttonList.add(redSlider); buttonList.add(greenSlider); buttonList.add(blueSlider);
        buttonList.add(alphaSlider); buttonList.add(widthSlider);
        buttonList.add(crosshairButton); buttonList.add(blockDistanceButton);
    }

    private GuiSlider slider(int id, int x, int y, String prefix, double min, double max, double value) {
        return new GuiSlider(id, x, y, prefix, min, max, value, null);
    }

    private String trajectoryText() { return "Trajectory: " + onOff(trajectoryState); }
    private String crosshairText() { return "Custom crosshair: " + onOff(crosshairState); }
    private String distanceText() { return "Hit distance: " + onOff(blockDistanceState); }
    private String onOff(boolean value) { return value ? "On" : "Off"; }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 7) { trajectoryState = !trajectoryState; trajectoryButton.displayString = trajectoryText(); }
        else if (button.id == 5) { crosshairState = !crosshairState; crosshairButton.displayString = crosshairText(); }
        else if (button.id == 6) { blockDistanceState = !blockDistanceState; blockDistanceButton.displayString = distanceText(); }
        apply(false);
    }

    @Override
    public void onGuiClosed() { apply(true); }

    private void apply(boolean save) {
        if (redSlider == null) return;
        AimBowMod.red = redSlider.getValueInt();
        AimBowMod.green = greenSlider.getValueInt();
        AimBowMod.blue = blueSlider.getValueInt();
        AimBowMod.alpha = alphaSlider.getValueInt();
        AimBowMod.lineWidth = widthSlider.getValueInt();
        AimBowMod.crossHairState = crosshairState;
        AimBowMod.blockDistanceState = blockDistanceState;
        AimBowMod.trajectoryState = trajectoryState;
        if (save) AimBowMod.saveConfig();
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}

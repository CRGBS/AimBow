package net.famzangl.minecraft.aimbow;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;
public final class AimBowCommandGui extends GuiScreen {
    private GuiSlider red, green, blue, alpha, widthSlider;
    private boolean trajectory, crosshair, distance;
    public AimBowCommandGui() {
        trajectory=AimBowMod.trajectoryState; crosshair=AimBowMod.crossHairState; distance=AimBowMod.blockDistanceState;
    }
    public void initGui() {
        buttonList.clear(); int x=width/2-100, y=height/2-100;
        buttonList.add(new GuiButton(7,x,y,200,20,text("Trajectory",trajectory)));
        red=new GuiSlider(0,x,y+25,"Red: ",0,255,AimBowMod.red,null);
        green=new GuiSlider(1,x,y+50,"Green: ",0,255,AimBowMod.green,null);
        blue=new GuiSlider(2,x,y+75,"Blue: ",0,255,AimBowMod.blue,null);
        alpha=new GuiSlider(3,x,y+100,"Alpha: ",0,255,AimBowMod.alpha,null);
        widthSlider=new GuiSlider(4,x,y+125,"Width: ",1,10,AimBowMod.lineWidth,null);
        buttonList.add(red); buttonList.add(green); buttonList.add(blue); buttonList.add(alpha); buttonList.add(widthSlider);
        buttonList.add(new GuiButton(5,x,y+150,200,20,text("Custom crosshair",crosshair)));
        buttonList.add(new GuiButton(6,x,y+175,200,20,text("Hit distance",distance)));
    }
    protected void actionPerformed(GuiButton b) {
        if(b.id==7) trajectory=!trajectory; else if(b.id==5) crosshair=!crosshair; else if(b.id==6) distance=!distance;
        if(b.id==7||b.id==5||b.id==6) initGui();
        apply(false);
    }
    private String text(String n,boolean v){return n+": "+(v?"On":"Off");}
    private void apply(boolean save){
        if(red==null)return; AimBowMod.red=red.getValueInt(); AimBowMod.green=green.getValueInt(); AimBowMod.blue=blue.getValueInt();
        AimBowMod.alpha=alpha.getValueInt(); AimBowMod.lineWidth=widthSlider.getValueInt(); AimBowMod.trajectoryState=trajectory;
        AimBowMod.crossHairState=crosshair; AimBowMod.blockDistanceState=distance; if(save)AimBowMod.saveConfig();
    }
    public void onGuiClosed(){apply(true);} public boolean doesGuiPauseGame(){return false;}
}

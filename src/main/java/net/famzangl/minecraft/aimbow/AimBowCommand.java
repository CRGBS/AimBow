package net.famzangl.minecraft.aimbow;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
public final class AimBowCommand extends CommandBase {
    public String getCommandName() { return "aimbow"; }
    public String getCommandUsage(ICommandSender sender) { return "/aimbow"; }
    public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
    public void processCommand(ICommandSender sender, String[] args) {
        Minecraft.getMinecraft().displayGuiScreen(new AimBowCommandGui());
    }
}

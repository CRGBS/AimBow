package net.famzangl.minecraft.aimbow;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public final class AimBowCommand extends CommandBase {
    @Override
    public String getCommandName() { return "aimbow"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/aimbow"; }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // ClientCommandHandler invokes this on the client thread, so no temporary
        // event-bus registration is needed (which previously allowed duplicates).
        Minecraft.getMinecraft().displayGuiScreen(new AimBowCommandGui());
    }
}

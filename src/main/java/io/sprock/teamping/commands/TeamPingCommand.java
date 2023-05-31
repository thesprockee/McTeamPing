package io.sprock.teamping.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class TeamPingCommand extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "teamping";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return null;
	}

	private static void appendMessageWithStyle(IChatComponent message, String text, EnumChatFormatting color) {
		message.appendSibling(new ChatComponentText(text).setChatStyle(new ChatStyle().setColor(color)));
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		IChatComponent message = new ChatComponentText("");
		if (args.length == 0) {
			message.appendSibling(new ChatComponentText("--Showing help for TeamPing mod--")
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_GREEN)));
			message.appendSibling(new ChatComponentText("\n/teamping")
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE)));
			sender.addChatMessage(message);
			return;
		}
		switch (args[0]) {
		default:
			appendMessageWithStyle(message, "Unknown command. Try /teamping for a list of commands",
					EnumChatFormatting.RED);
			break;
		}
		sender.addChatMessage(message);
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {

		List<String> tabcomp = new ArrayList<>();
		return tabcomp;
	}
}

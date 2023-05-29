package io.sprock.teamping.commands;

import static io.sprock.teamping.TeamPing.isInParty;
import static io.sprock.teamping.TeamPing.partyName;
import static io.sprock.teamping.TeamPing.partyPlayers;
import static io.sprock.teamping.TeamPing.playerCount;
import static io.sprock.teamping.client.SendData.banFromParty;
import static io.sprock.teamping.client.SendData.joinParty;
import static io.sprock.teamping.client.SendData.kickFromParty;
import static io.sprock.teamping.client.SendData.leaveParty;
import static io.sprock.teamping.listeners.EventListener.openChat;
import static io.sprock.teamping.listeners.EventListener.openChatString;
import static io.sprock.teamping.listeners.EventListener.openChatTime;

import java.util.ArrayList;
import java.util.Arrays;
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
			message.appendSibling(new ChatComponentText("\n/teamping join <partyid>")
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE)));
			message.appendSibling(new ChatComponentText("\n/teamping leave")
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE)));
			message.appendSibling(new ChatComponentText("\n/teamping status")
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE)));
			message.appendSibling(new ChatComponentText("\n/teamping reconnect")
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE)));
			sender.addChatMessage(message);
			return;
		}
		switch (args[0]) {
		case "join":

			StringBuilder tempname = new StringBuilder();
			if (args.length >= 2) {
				for (int i = 0; i < args.length - 1; i++) {
					tempname.append(args[i + 1]).append(" ");
				}
				tempname.deleteCharAt(tempname.length() - 1);
			}
			if (args.length >= 2 && tempname.length() >= 3 && tempname.length() <= 32) {
				leaveParty();
				joinParty(tempname.toString());
				appendMessageWithStyle(message, "Joined the party", EnumChatFormatting.GREEN);
			} else {
				appendMessageWithStyle(message, "PartyID length must be in [3, 32] range", EnumChatFormatting.RED);
			}
			break;

		case "leave":
			if (partyPlayers.size() == 0) {
				appendMessageWithStyle(message, "You are not in a party", EnumChatFormatting.RED);
				break;
			}
			leaveParty();
			appendMessageWithStyle(message, "Left from the party", EnumChatFormatting.GREEN);
			break;

		case "list":

			appendMessageWithStyle(message, "--List of players in the current party--", EnumChatFormatting.DARK_GREEN);
			if (partyPlayers.size() != 0) {
				partyPlayers.forEach(name -> {
					appendMessageWithStyle(message, "\n" + name, EnumChatFormatting.WHITE);
					if (partyPlayers.get(0).equals(name)) {
						appendMessageWithStyle(message, " Leader", EnumChatFormatting.GOLD);
					}
				});
			} else {
				appendMessageWithStyle(message, "\nYou are not in a party", EnumChatFormatting.WHITE);
			}
		case "kick":

			break;
		case "ban":
			if (partyPlayers.size() == 0) {
				appendMessageWithStyle(message, "You are not in a party", EnumChatFormatting.RED);
			} else if (args.length != 2) {
				appendMessageWithStyle(message, "You need to type the player name", EnumChatFormatting.RED);
			} else if (!partyPlayers.get(0).equals(sender.getName())) {
				appendMessageWithStyle(message, "You are not the leader of the party", EnumChatFormatting.RED);
			} else if (!partyPlayers.contains(args[1])) {
				appendMessageWithStyle(message, "There's no player in a party with that name", EnumChatFormatting.RED);
			} else {
				banFromParty(args[1]);
				appendMessageWithStyle(message, "Player got banned from the party", EnumChatFormatting.GREEN);
			}
			break;
		case "promote":
			if (partyPlayers.size() == 0) {
				appendMessageWithStyle(message, "You are not in a party", EnumChatFormatting.RED);
			} else if (args.length != 2) {
				appendMessageWithStyle(message, "You need to type the player name", EnumChatFormatting.RED);
			} else if (!partyPlayers.get(0).equals(sender.getName())) {
				appendMessageWithStyle(message, "You are not the leader of the party", EnumChatFormatting.RED);
			} else if (!partyPlayers.contains(args[1])) {
				appendMessageWithStyle(message, "There's no player in a party with that name", EnumChatFormatting.RED);
			} else {
				kickFromParty(args[1]);
				appendMessageWithStyle(message, "Player got kicked from the party", EnumChatFormatting.GREEN);
			}
			break;
		case "status":
			appendMessageWithStyle(message, "--Connection status--", EnumChatFormatting.DARK_GREEN);
			appendMessageWithStyle(message, "\nClients connected: " + playerCount, EnumChatFormatting.WHITE);
			break;

		case "genInvText":
			if (isInParty) {
				appendMessageWithStyle(message, "Invitation text was put in your message box",
						EnumChatFormatting.GREEN);
				openChat = true;
				openChatTime = System.currentTimeMillis();
				openChatString = "teamping:" + partyName;
			} else {
				appendMessageWithStyle(message, "You are not in a party", EnumChatFormatting.RED);
			}
			break;

		default:
			appendMessageWithStyle(message, "Unknown command. Try /teamping for a list of commands",
					EnumChatFormatting.RED);
			break;
		}
		sender.addChatMessage(message);
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		System.out.println(Arrays.toString(args));
		List<String> tabcomp = new ArrayList<>();
		if (args.length == 1) {
			tabcomp.add("join");
			tabcomp.add("list");
			tabcomp.add("kick");
			tabcomp.add("ban");
			tabcomp.add("promote");
			tabcomp.add("leave");
			tabcomp.add("status");
			tabcomp.add("genInvText");
		}
		if (args.length == 2 && (args[0].equals("kick") || args[0].equals("ban") || args[0].equals("promote"))) {
			if (partyPlayers.size() != 0) {
				List<String> players = getListOfStringsMatchingLastWord(args, partyPlayers);
				players.remove(sender.getName());
				tabcomp.addAll(players);
			} else {
				tabcomp.add("You are not in the party");
			}
		}
		return tabcomp;
	}
}

package com.aqupd.teamping;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aqupd.teamping.commands.TeamPingCommand;
import com.aqupd.teamping.listeners.EventListener;
import com.aqupd.teamping.registrations.KeyBindings;
import com.aqupd.teamping.util.Configuration;
import com.google.gson.JsonObject;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = TeamPing.MOD_ID, name = TeamPing.MOD_NAME, version = TeamPing.VERSION, clientSideOnly = true)
public class TeamPing {

	public static final String MOD_ID = "teamping";
	public static final String MOD_NAME = "TeamPing";
	public static final String VERSION = "0.1.2";
	public static final Logger LOGGER = LogManager.getLogger("AqUpd's " + MOD_NAME);
	public static final String[] pingidnames = new String[]{"here", "notice", "question", "no", "yes", "defend", "attack", "mine"};
	public static List<JsonObject> pings = new ArrayList<>();
	public static String partyName = "default";
	public static String GitVersion = "N/A";
	public static int playerCount = 0;
	public static boolean hidetext = false;
	public static boolean isInParty = false;
	public static ArrayList<String> partyPlayers = new ArrayList<>();
	public static OutputStream outputStream;
	private final EventListener eventListener;

	public TeamPing() throws IOException {
		KeyBindings.initialize();
		this.eventListener = new EventListener();
		Configuration.loadOptions();
	}

	@EventHandler
	public void init(FMLInitializationEvent ev) {
		MinecraftForge.EVENT_BUS.register(eventListener);
		ClientCommandHandler.instance.registerCommand(new TeamPingCommand());
	}
}

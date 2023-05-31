package io.sprock.teamping;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import io.sprock.teamping.commands.TeamPingCommand;
import io.sprock.teamping.listeners.EventListener;
import io.sprock.teamping.registrations.KeyBindings;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = TeamPing.MOD_ID, name = TeamPing.MOD_NAME, version = TeamPing.VERSION, clientSideOnly = true)
public class TeamPing {

	public static final String MOD_ID = "teamping";
	public static final String MOD_NAME = "TeamPing";
	public static final String VERSION = "@VERSION@";
	public static Logger LOGGER;
	public static final String PING_HERE = "x";
	public static final String PING_NOTICE = "n";
	public static final String PING_QUESTION = "q";
	public static final String PING_NO = "N";
	public static final String PING_YES = "Y";
	public static final String PING_DEFEND = "d";
	public static final String PING_ATTACK = "a";
	public static final String PING_MINE = "m";

	public static final String[] pingIds = new String[] { PING_HERE, PING_NOTICE, PING_QUESTION, PING_NO, PING_YES,
			PING_DEFEND, PING_ATTACK, PING_MINE };

	public static List<JsonObject> pings = new ArrayList<>();

	public static String GitVersion = VERSION;
	public static int playerCount = 0;
	public static boolean hidetext = false;
	public static boolean isInParty = false;
	public static ArrayList<String> partyPlayers = new ArrayList<>();
	public static OutputStream outputStream;
	private final EventListener eventListener;

	public TeamPing() throws IOException {
		KeyBindings.initialize();
		this.eventListener = new EventListener();

	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		LOGGER = e.getModLog();
		LOGGER.info("babsld's " + MOD_NAME + "v" + VERSION);
	}

	@EventHandler
	public void init(FMLInitializationEvent ev) {

		MinecraftForge.EVENT_BUS.register(eventListener);
		ClientCommandHandler.instance.registerCommand(new TeamPingCommand());
	}
}

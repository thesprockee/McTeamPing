package io.sprock.teamping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import io.sprock.teamping.commands.TeamPingCommand;
import io.sprock.teamping.config.Config;
import io.sprock.teamping.listeners.EventListener;
import io.sprock.teamping.registrations.KeyBindings;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = TeamPing.MOD_ID, name = TeamPing.MOD_NAME, clientSideOnly = true, guiFactory = "io.sprock.teamping.config.ModGuiFactory", acceptedMinecraftVersions = "[1.8.9]")
public class TeamPing {

	public static final String MOD_ID = "teamping";
	public static final String MOD_NAME = "TeamPing";

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

	private final EventListener eventListener;


	public static String markerTexturePath = "textures/gui/markers.png";

	public TeamPing() throws IOException {
		KeyBindings.initialize();
		this.eventListener = new EventListener();

	}

	public static String getTitle() {
		return "babsld's TeamPing v" + Version.getVersion();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LOGGER = event.getModLog();

		Version.init(event.getVersionProperties());
		ModMetadata metadata = event.getModMetadata();
		metadata.version = Version.getVersion();

		LOGGER.info(getTitle());

		Config.preInit(event);

	}

	@EventHandler
	public void init(FMLInitializationEvent ev) {

		MinecraftForge.EVENT_BUS.register(eventListener);
		ClientCommandHandler.instance.registerCommand(new TeamPingCommand());
	}
}

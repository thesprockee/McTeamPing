package io.sprock.teamping.config;

import static io.sprock.teamping.TeamPing.LOGGER;
import static io.sprock.teamping.TeamPing.MOD_ID;

import java.io.File;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public final class Config {

	public static final String CATEGORY_CLIENT = "client";

	@Nullable
	private static Configuration config;

	@Nullable
	private static File TPConfigurationDir;

	private static final ConfigValues defaultValues = new ConfigValues();
	private static final ConfigValues values = new ConfigValues();

	private Config() {

	}

	public static boolean shouldFilterDataMessages() {
		return values.filterDataMessages;
	}

	public static String getPingMessagePrefix() {
		return values.dataMessagePrefix;
	}

	public static Boolean isPingSFXEnabled() {
		return values.enablePingSFX;
	}

	public static Boolean isSelectWheelEnabled() {
		return values.enableMarkerSelectGUI;
	}

	public static int getSonarRange() {
		return values.sonarRange;
	}

	public static int getAdditionalRenderDistanceChunks() {
		return values.extraRenderChunks;
	}

	@Nullable
	public static Configuration getConfig() {
		return config;
	}

	public static File getTPConfigurationDir() {
		Preconditions.checkState(TPConfigurationDir != null);
		return TPConfigurationDir;
	}

	public static void preInit(FMLPreInitializationEvent event) {

		TPConfigurationDir = new File(event.getModConfigurationDirectory(), MOD_ID);
		if (!TPConfigurationDir.exists()) {
			try {
				if (!TPConfigurationDir.mkdir()) {
					LOGGER.error("Could not create config directory {}", TPConfigurationDir);
					return;
				}
			} catch (SecurityException e) {
				LOGGER.error("Could not create config directory {}", TPConfigurationDir, e);
				return;
			}
		}

		final File configFile = new File(TPConfigurationDir, "teamping.cfg");
		config = new Configuration(configFile, "0.4.0");

		syncConfig();
	}

	public static boolean syncAllConfig() {
		boolean needsReload = false;
		if (syncConfig()) {
			needsReload = true;
		}

		return needsReload;
	}

	private static boolean syncConfig() {

		if (config == null) {
			return false;
		}

		boolean needsReload = false;

		values.filterDataMessages = config.getBoolean("filterDataMessages", CATEGORY_CLIENT,
				defaultValues.filterDataMessages, "Do not display data messages in chat");

		values.enablePingSFX = config.getBoolean("enablePingSFX", CATEGORY_CLIENT, defaultValues.enablePingSFX,
				"Enable Ping SFX");

		values.sonarRange = config.getInt("sonarChunkRange", CATEGORY_CLIENT, defaultValues.sonarRange, 0, 16,
				"Sonar maximum range, in chunks (chunk = 16 blocks)");

		values.enableMarkerSelectGUI = config.getBoolean("enableMarkerSelectGUI", CATEGORY_CLIENT,
				defaultValues.enableMarkerSelectGUI, "Use on-screen GUI to select marker type");

		values.dataMessagePrefix = config.getString("dataMessagePrefix", CATEGORY_CLIENT,
				defaultValues.dataMessagePrefix, "Prefix data messages with...");
		values.extraRenderChunks = config.getInt("extraRenderChunks", CATEGORY_CLIENT, defaultValues.extraRenderChunks,
				0, 4, "extra distance to render markers. (also disables alpha)");
		final boolean configChanged = config.hasChanged();

		if (configChanged) {
			config.save();
		}

		return needsReload;
	}

}
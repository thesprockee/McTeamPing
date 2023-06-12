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

	public static String getPingMessagePrefix() {
		return values.pingMessagePrefix;
	}

	public static Boolean isPingSFXEnabled() {
		return values.enablePingSFX;
	}

	public static Boolean useSelectWheel() {
		return values.useSelectWheel;
	}
	public static int getSonarRange() {
		return values.sonarRange;
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

		values.enablePingSFX = config.getBoolean("enablePingSFX", CATEGORY_CLIENT, defaultValues.enablePingSFX,
				"Enable Ping SFX");

		values.sonarRange = config.getInt("sonarRange", CATEGORY_CLIENT, defaultValues.sonarRange, 0, 16 * 16,
				"Maximum distance for sonar [0 = unlimited]");

		values.useSelectWheel = config.getBoolean("useSelectWheel", CATEGORY_CLIENT, defaultValues.useSelectWheel,
				"Use selection wheel for marker type");

		values.pingMessagePrefix = config.getString("pingMessagePrefix", CATEGORY_CLIENT,
				defaultValues.pingMessagePrefix, "Prefix ping messages with...");

		final boolean configChanged = config.hasChanged();

		if (configChanged) {
			config.save();
		}

		return needsReload;
	}

}
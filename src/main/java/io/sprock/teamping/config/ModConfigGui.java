package io.sprock.teamping.config;

import static io.sprock.teamping.TeamPing.MOD_ID;
import static io.sprock.teamping.TeamPing.MOD_TITLE;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class ModConfigGui extends GuiConfig {

	public ModConfigGui(GuiScreen parent) {
		super(getParent(parent), getConfigElements(), MOD_ID, false, false, getTitle(parent));
	}

	private static GuiScreen getParent(GuiScreen parent) {
		return parent;
	}

	private static List<IConfigElement> getConfigElements() {
		List<IConfigElement> configElements = new ArrayList<IConfigElement>();

		Configuration config = Config.getConfig();

		if (config != null) {
			ConfigCategory categoryAdvanced = config.getCategory(Config.CATEGORY_CLIENT);
			configElements.addAll(new ConfigElement(categoryAdvanced).getChildElements());
		}
		return configElements;
	}

	private static String getTitle(GuiScreen parent) {
		if (parent instanceof GuiModList) {
			Configuration config = Config.getConfig();
			if (config != null) {
				return GuiConfig.getAbridgedConfigPath(config.toString());
			}
		}
		return MOD_TITLE;
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);

		return;
	}
}
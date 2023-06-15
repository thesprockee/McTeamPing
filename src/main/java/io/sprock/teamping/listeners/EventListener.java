package io.sprock.teamping.listeners;

import static io.sprock.teamping.TeamPing.MOD_ID;
import static io.sprock.teamping.TeamPing.markerList;
import static io.sprock.teamping.client.SendData.getSonarId;
import static io.sprock.teamping.client.SendData.pingBlockUnderCursor;
import static io.sprock.teamping.client.SendData.sendSonar;
import static io.sprock.teamping.client.SendData.sendSonarReply;
import static io.sprock.teamping.registrations.KeyBindings.keyBindings;
import static io.sprock.teamping.util.UtilMethods.distanceTo2D;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;

import io.sprock.teamping.TeamPing;
import io.sprock.teamping.client.Marker;
import io.sprock.teamping.config.Config;
import io.sprock.teamping.render.MarkerSelectGuiRenderer;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventListener {

	public static float ticks;

	public static Integer[] sfxPosition = new Integer[3];

	private static final Minecraft minecraft = Minecraft.getMinecraft();

	public static long openChatTime = 0;
	public static boolean openChat = false;
	public static String openChatString = "";
	private static String commandRegex = "([psP]):([-0-9]{1,8})/([-0-9]{1,8})/([-0-9]{1,8}):([A-z0-9,]+)";
	private static Pattern commandPattern = Pattern.compile(commandRegex);

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderTickEvent(TickEvent.RenderTickEvent event) {
		ticks = event.renderTickTime;
	}

	private long lastjoineventusage = 0;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void someEvent(EntityJoinWorldEvent event) {
		if (event.entity instanceof EntityPlayerSP && (System.currentTimeMillis() - lastjoineventusage) > 250) {
			lastjoineventusage = System.currentTimeMillis();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
		if (event.player instanceof EntityPlayerSP) {

			if (openChat && System.currentTimeMillis() - openChatTime > 50) {
				Minecraft.getMinecraft().displayGuiScreen(new GuiChat(openChatString));
				openChatTime = 0;
				openChat = false;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientTickEvent(TickEvent.ClientTickEvent event) {

		if (Config.isPingSFXEnabled() && sfxPosition[0] != null && sfxPosition[1] != null && sfxPosition[2] != null) {

			Minecraft.getMinecraft().theWorld.playSound(sfxPosition[0], sfxPosition[1], sfxPosition[2],
					"minecraft:fireworks.blast_far", 0.5F, 1F, false);

			sfxPosition[0] = null;
			sfxPosition[1] = null;
			sfxPosition[2] = null;
		}

		Iterator<Marker> markerIter = markerList.iterator();
		while (markerIter.hasNext()) {
			Marker marker = markerIter.next();

			if ((System.currentTimeMillis() - marker.getTimestamp().getTime()) > 15000) {
				markerIter.remove();
			}
		}

		if (Config.isSelectWheelEnabled()) {
			if (MarkerSelectGuiRenderer.isActive()) {

				if (!keyBindings[0].isKeyDown() || minecraft.gameSettings.keyBindAttack.isKeyDown()) {
					MarkerSelectGuiRenderer.triggerSelection();
					MarkerSelectGuiRenderer.setActive(false);
				}
			} else if (keyBindings[0].isKeyDown()) {
				MarkerSelectGuiRenderer.setActive(true);
			}
		} else if (keyBindings[0].isPressed()) {
			pingBlockUnderCursor(Marker.getCode(Marker.Symbol.HERE));
		}
		if (keyBindings[1].isPressed()) {
			sendSonar(Config.getSonarRange());
		}

	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiRenderEvent(RenderGameOverlayEvent.Pre event) {
		if (event.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH)
			MarkerSelectGuiRenderer.onGuiRenderEvent(event);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		if (event.type == 0x2) {
			return; // ignore actionbar messages
		}

		Matcher matcher = commandPattern.matcher(event.message.getUnformattedText().trim());

		if (matcher.find()) {

			try {
				event.setCanceled(Config.shouldFilterDataMessages());

				String prefix = matcher.group(1);
				int x = Integer.parseInt(matcher.group(2));
				int y = Integer.parseInt(matcher.group(3));
				int z = Integer.parseInt(matcher.group(4));
				String suffix = matcher.group(5);
				String[] parts = suffix.split(",");

				switch (prefix) {
				case "p":
					markBlock(x, y, z, suffix.substring(0, 1));
					break;
				case "s":
					int range = Integer.parseInt(parts[0]);
					String sourceId = parts[1];
					int blockRange = (range > 0) ? range * 16 : 2048;
					Entity renderView = minecraft.getRenderViewEntity();
					if (!sourceId.equals(getSonarId()) && distanceTo2D(renderView, new BlockPos(x, y, z)) <= blockRange) {
						sendSonarReply(sourceId);
					}
					;
					break;
				case "P":
					if (suffix.equals(getSonarId())) {
						markBlock(x, y, z, Marker.getCode(Marker.Symbol.NOTICE));
					}

					break;
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				TeamPing.LOGGER.warn("Could not process:" + e.getStackTrace());
			}
		}
	}

	private void markBlock(int x, int y, int z, String type) {

		markerList.add(Marker.fromData(x,  y, z, type));

		BlockPos playerPos = minecraft.thePlayer.getPosition();
		Integer[] playerpos = new Integer[] { playerPos.getX(), playerPos.getY(), playerPos.getZ() };

		Integer[] blockps = new Integer[3];

		blockps[0] = Math.min(2, Math.max(-2, playerpos[0] - x));
		blockps[1] = Math.min(2, Math.max(-2, playerpos[1] - y));
		blockps[2] = Math.min(2, Math.max(-2, playerpos[2] - z));

		sfxPosition[0] = playerpos[0] - blockps[0];
		sfxPosition[1] = playerpos[1] - blockps[1];
		sfxPosition[2] = playerpos[2] - blockps[2];
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (!MOD_ID.equals(eventArgs.modID)) {
			return;
		}

		if (Config.syncAllConfig()) {
			// when config is reloaded
		}
	}
}

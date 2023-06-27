package io.sprock.teamping.listeners;

import static io.sprock.teamping.TeamPing.MOD_ID;
import static io.sprock.teamping.registrations.KeyBindings.keyBindings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;

import io.sprock.teamping.client.ChatProtocol;
import io.sprock.teamping.client.Marker;
import io.sprock.teamping.config.Config;
import io.sprock.teamping.handlers.MarkerHandler;
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

	private static final Minecraft minecraft = Minecraft.getMinecraft();

	public static long openChatTime = 0;
	public static boolean openChat = false;
	public static String openChatString = "";

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

		if (Config.isPingSFXEnabled())
			MarkerHandler.doSFX();

		MarkerHandler.expireMarkers();

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
			ChatProtocol.pingBlockUnderCursor(Marker.getCode(Marker.Symbol.HERE));
		}
		if (keyBindings[1].isPressed()) {
			ChatProtocol.sendSonar(Config.getSonarRange());
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

		if (event.type == 0x2) // ignore actionbar messages
			return;

		if (ChatProtocol.processChatMessage(event.message.getUnformattedText().trim()))
			event.setCanceled(Config.shouldFilterDataMessages());

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

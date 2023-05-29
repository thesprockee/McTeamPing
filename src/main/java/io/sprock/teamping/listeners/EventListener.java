package io.sprock.teamping.listeners;

import static io.sprock.teamping.TeamPing.LOGGER;
import static io.sprock.teamping.TeamPing.pings;
import static io.sprock.teamping.client.PingSelector.cX;
import static io.sprock.teamping.client.PingSelector.cY;
import static io.sprock.teamping.registrations.KeyBindings.keyBindings;
import static io.sprock.teamping.util.Configuration.debug;
import static io.sprock.teamping.util.UtilMethods.isValidJsonObject;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import io.sprock.teamping.client.PartyGUI;
import io.sprock.teamping.client.PingManager;
import io.sprock.teamping.client.PingSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventListener {
	public static boolean connectedtoserver = false;
	private boolean clearpings = false;

	public static boolean connecting = false;
	public static boolean guimenu = false;
	public static boolean stoppingmc = false;
	public static float ticks;
	public static Integer[] playsound = new Integer[3];
	public static int conattempts = 0;
	public static int timer = 0;

	public static long openChatTime = 0;
	public static boolean openChat = false;
	public static String openChatString = "";

	private static Pattern chatDataPattern = Pattern.compile("<([a-zA-Z0-9_]+)> (\\{.+\\})");


	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderTickEvent(TickEvent.RenderTickEvent event) {
		ticks = event.renderTickTime;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerJoinServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
		if (!event.isLocal || debug) {
			if (!connecting) connectedtoserver = true;
		}
	}

	private long lastjoineventusage = 0;
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void someEvent(EntityJoinWorldEvent event) {
		if (event.entity instanceof EntityPlayerSP && (System.currentTimeMillis() - lastjoineventusage) > 250) {
			clearpings = true;
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
		if (playsound[0] != null && playsound[1] != null && playsound[2] != null) {
			Minecraft.getMinecraft().theWorld.playSound(playsound[0], playsound[1], playsound[2],"minecraft:fireworks.blast_far", 0.5F, 1F, false);
			playsound[0] = null;
			playsound[1] = null;
			playsound[2] = null;
		}

		Iterator<JsonObject> pingsIter = pings.iterator();
		while (pingsIter.hasNext()) {
			JsonObject data = pingsIter.next();
			long time = data.get("time").getAsLong();
			if ((System.currentTimeMillis() - time) > 15000) {
				pingsIter.remove();
			}
		}

		if (guimenu && timer < 15) timer++;
		else if (!guimenu && timer > 0) {
			timer--;
			cX = 0;
			cY = 0;
		}

		guimenu = keyBindings[0].isKeyDown();
		if (keyBindings[1].isPressed() || clearpings) {
			PingManager.clear();
			clearpings = false;
		}

		if (keyBindings[2].isPressed()) Minecraft.getMinecraft().displayGuiScreen(new PartyGUI());
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiRenderEvent(RenderGameOverlayEvent.Pre event) {
		if (event.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH && (guimenu || timer > 0)) {
			PingSelector.render();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onChatMessage(ClientChatReceivedEvent event) {
		String chattext = event.message.getFormattedText();
		Matcher matcher = Pattern.compile("teamping:.{3,32}").matcher(chattext);
		if(matcher.find()){
			String partyid = matcher.group().substring(9);
			IChatComponent component = new ChatComponentText("\nClick to join party with " + partyid + " id");
			component.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teamping join " + partyid)).setColor(EnumChatFormatting.GRAY);
			event.message.appendSibling(component);
		}
	}


	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		if (event.type == 0x2) {
			return; // ignore actionbar messages
		}

		Matcher matcher = chatDataPattern.matcher(event.message.getUnformattedText().trim());

		if (matcher.find()) {

			String jsonPayload = matcher.group(2);
			if (!isValidJsonObject(jsonPayload)) {
				LOGGER.error("invalid json: " + jsonPayload);
				return;
			}

			LOGGER.info("JSON Payload: " + jsonPayload);

			event.setCanceled(true);

			JsonObject jo = new JsonParser().parse(jsonPayload).getAsJsonObject();
			switch (jo.get("datatype").getAsString()) {
			case "ping":
				jo.add("time", new JsonPrimitive(System.currentTimeMillis()));
				pings.add(jo);

				Integer[] playerpos = new Integer[3];
				playerpos[0] = Minecraft.getMinecraft().thePlayer.getPosition().getX();
				playerpos[1] = Minecraft.getMinecraft().thePlayer.getPosition().getY();
				playerpos[2] = Minecraft.getMinecraft().thePlayer.getPosition().getZ();

				Integer[] blockps = new Integer[3];
				blockps[0] = Math.min(2, Math.max(-2, playerpos[0] - jo.get("bp").getAsJsonArray().get(0).getAsInt()));
				blockps[1] = Math.min(2, Math.max(-2, playerpos[1] - jo.get("bp").getAsJsonArray().get(1).getAsInt()));
				blockps[2] = Math.min(2, Math.max(-2, playerpos[2] - jo.get("bp").getAsJsonArray().get(2).getAsInt()));

				playsound[0] = playerpos[0] - blockps[0];
				playsound[1] = playerpos[1] - blockps[1];
				playsound[2] = playerpos[2] - blockps[2];
				break;
			}

		}
	}
}

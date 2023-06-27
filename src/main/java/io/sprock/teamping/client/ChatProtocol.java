package io.sprock.teamping.client;

import static java.lang.Math.min;

import static io.sprock.teamping.util.UtilMethods.distanceTo2D;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import io.sprock.teamping.TeamPing;
import io.sprock.teamping.config.Config;
import io.sprock.teamping.handlers.MarkerHandler;
import io.sprock.teamping.hooks.NetworkPlayerInfoHook;
import io.sprock.teamping.render.MarkerRenderer;

public class ChatProtocol {
	public static enum Action {
		BLOCK_PING, SONAR_PING, SONAR_PONG
	}

	private static final EnumMap<Action, String> encodeMap = new EnumMap<>(Action.class);
	private static final Map<String, Action> decodeMap = new HashMap<String, Action>();

	private static String commandRegex = "([psP]):([-0-9]{1,8})/([-0-9]{1,8})/([-0-9]{1,8}):([A-z0-9,]+)";
	private static Pattern commandPattern = Pattern.compile(commandRegex);
	public static long lastpingtime = 0;
	private static final Minecraft minecraft = Minecraft.getMinecraft();

	static {

		putMaps(Action.BLOCK_PING, "p");
		putMaps(Action.SONAR_PING, "S");
		putMaps(Action.SONAR_PONG, "P");
	}

	private static MovingObjectPosition getMouseOverExtended(float dist) {
		Minecraft mc = Minecraft.getMinecraft();
		Entity theRenderViewEntity = mc.getRenderViewEntity();
		AxisAlignedBB theViewBoundingBox = new AxisAlignedBB(theRenderViewEntity.posX - 0.5D,
				theRenderViewEntity.posY - 0.0D, theRenderViewEntity.posZ - 0.5D, theRenderViewEntity.posX + 0.5D,
				theRenderViewEntity.posY + 1.5D, theRenderViewEntity.posZ + 0.5D);
		MovingObjectPosition returnMOP = null;
		if (mc.theWorld != null) {
			double var2 = dist;
			returnMOP = theRenderViewEntity.rayTrace(var2, 0);
			double calcdist = var2;
			Vec3 pos = theRenderViewEntity.getPositionEyes(0);
			var2 = calcdist;
			if (returnMOP != null) {
				calcdist = returnMOP.hitVec.distanceTo(pos);
			}

			Vec3 lookvec = theRenderViewEntity.getLook(0);
			Vec3 var8 = pos.addVector(lookvec.xCoord * var2, lookvec.yCoord * var2, lookvec.zCoord * var2);
			Entity pointedEntity = null;
			float var9 = 1.0F;
			List<Entity> list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(theRenderViewEntity,
					theViewBoundingBox.addCoord(lookvec.xCoord * var2, lookvec.yCoord * var2, lookvec.zCoord * var2)
							.expand(var9, var9, var9));
			double d = calcdist;
			for (Entity entity : list) {
				if (entity.canBeCollidedWith()) {
					float bordersize = entity.getCollisionBorderSize();
					AxisAlignedBB aabb = new AxisAlignedBB(entity.posX - entity.width / 2, entity.posY,
							entity.posZ - entity.width / 2, entity.posX + entity.width / 2, entity.posY + entity.height,
							entity.posZ + entity.width / 2);
					aabb.expand(bordersize, bordersize, bordersize);
					MovingObjectPosition mop0 = aabb.calculateIntercept(pos, var8);
					if (aabb.isVecInside(pos)) {
						if (0.0D < d || d == 0.0D) {
							pointedEntity = entity;
							d = 0.0D;
						}
					} else if (mop0 != null) {
						double d1 = pos.distanceTo(mop0.hitVec);
						if (d1 < d || d == 0.0D) {
							pointedEntity = entity;
							d = d1;
						}
					}
				}
			}
			if (pointedEntity != null && (d < calcdist || returnMOP == null)) {
				returnMOP = new MovingObjectPosition(pointedEntity);
			}
		}
		return returnMOP;
	}

	public static String getSonarId() {
		return String.valueOf(Math.abs(minecraft.thePlayer.getPersistentID().hashCode()));
	}

	public static void pingBlockUnderCursor(String type) {
		if ((System.currentTimeMillis() - lastpingtime) > 1000) {

			int distance = min(MarkerRenderer.getMarkerRenderDistanceChunks() * 16, 128);
			Entity e = getMouseOverExtended(distance).entityHit;
			BlockPos bp;

			if (e != null) {
				bp = e.getPosition();
			} else {
				bp = getMouseOverExtended(distance).getBlockPos();
			}

			pingCoordinates(bp.getX(), bp.getY(), bp.getZ(), type);

		}
	}

	public static void pingCoordinates(int x, int y, int z, String type) {
		sendCommand(Action.BLOCK_PING, x, y, z, type);

		lastpingtime = System.currentTimeMillis();
	}

	public static boolean processChatMessage(String chatMessage) {
		Matcher matcher = commandPattern.matcher(chatMessage);
		if (matcher.find()) {
			try {

				Action action = decodeMap.get(matcher.group(1));

				int x = Integer.parseInt(matcher.group(2));
				int y = Integer.parseInt(matcher.group(3));
				int z = Integer.parseInt(matcher.group(4));

				String suffix = matcher.group(5);
				String[] parts = suffix.split(",");

				switch (action) {

				case BLOCK_PING:
					MarkerHandler.markBlock(x, y, z, Marker.getSymbol(suffix.substring(0, 1)));
					break;

				case SONAR_PING:
					Iterable<NetworkPlayerInfo> players = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();
					for (NetworkPlayerInfo player : players) {
						NetworkPlayerInfoHook.putPlayerInMap(player);
					}

					int range = Integer.parseInt(parts[0]);
					String sourceId = parts[1];
					int blockRange = (range > 0) ? range * 16 : 2048;
					Entity renderView = minecraft.getRenderViewEntity();
					if (!sourceId.equals(getSonarId())
							&& distanceTo2D(renderView, new BlockPos(x, y, z)) <= blockRange) {
						sendSonarReply(sourceId);
					}
					break;

				case SONAR_PONG:
					if (suffix.equals(getSonarId())) {
						MarkerHandler.markBlock(x, y, z, Marker.Symbol.NOTICE);
					}

					break;
				}

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				TeamPing.LOGGER.warn("Could not process:" + e.getStackTrace());
			}

			return true;
		} else {

			return false;
		}
	}

	private static void putMaps(Action action, String code) {
		encodeMap.put(action, code);
		decodeMap.put(code, action);
	}

	public static void sendCommand(Action commandCode, int x, int y, int z, String suffix) {
		ArrayList<String> components = new ArrayList<String>();
		String messagePrefix = Config.getPingMessagePrefix();

		if (!messagePrefix.isEmpty()) {
			components.add(messagePrefix + " ");
		}

		components.add(encodeMap.get(commandCode));

		components.add(String.join("/", String.valueOf(x), String.valueOf(y), String.valueOf(z)));

		components.add(suffix);

		minecraft.thePlayer.sendChatMessage(String.join(":", components));
	}

	public static void sendSonar(int range) {
		BlockPos bp = minecraft.thePlayer.getPosition();
		ArrayList<String> suffix = new ArrayList<String>();
		suffix.add(String.valueOf(range));
		suffix.add(getSonarId());
		sendCommand(Action.SONAR_PING, bp.getX(), bp.getY(), bp.getZ(), String.join(",", suffix));

	}

	public static void sendSonarReply(String id) {
		BlockPos playerPos = minecraft.thePlayer.getPosition();
		sendCommand(Action.SONAR_PONG, playerPos.getX(), playerPos.getY(), playerPos.getZ(), id);
	}

}

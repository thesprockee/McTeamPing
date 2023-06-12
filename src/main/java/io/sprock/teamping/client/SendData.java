package io.sprock.teamping.client;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import io.sprock.teamping.config.Config;

public class SendData {
	public static long lastpingtime = 0;
	private static final Minecraft minecraft = Minecraft.getMinecraft();

	public static void pingBlockUnderCursor(String type) {
		if ((System.currentTimeMillis() - lastpingtime) > 1000) {

			int distance = min(Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16, 128);
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

	public static String getSonarId() {
		return String.valueOf(Math.abs(minecraft.thePlayer.getPersistentID().hashCode()));
	}

	public static void sendSonar(int range) {
		BlockPos bp = minecraft.thePlayer.getPosition();
		ArrayList<String> suffix = new ArrayList<String>();
		suffix.add(String.valueOf(range));
		suffix.add(getSonarId());
		sendCommand("s", bp.getX(), bp.getY(), bp.getZ(), String.join(",", suffix));
	}

	public static void sendSonarReply(String id) {
		BlockPos playerPos = minecraft.thePlayer.getPosition();
		sendCommand("P", playerPos.getX(), playerPos.getY(), playerPos.getZ(), id);
	}

	public static void pingCoordinates(int x, int y, int z, String type) {
		sendCommand("p", x, y, z, type);

		lastpingtime = System.currentTimeMillis();
	}

	public static void sendCommand(String prefix, int x, int y, int z, String suffix) {
		ArrayList<String> components = new ArrayList<String>();
		String messagePrefix = Config.getPingMessagePrefix();

		if (!messagePrefix.isEmpty()) {
			components.add(messagePrefix + " ");
		}

		components.add(prefix);

		components.add(String.join("/", String.valueOf(x), String.valueOf(y), String.valueOf(z)));

		components.add(suffix);

		minecraft.thePlayer.sendChatMessage(String.join(":", components));
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
}

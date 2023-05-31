package io.sprock.teamping.util;

import static io.sprock.teamping.listeners.EventListener.ticks;

import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;

public class UtilMethods {
	public static double distanceTo3D(Entity e, BlockPos bp) {
		return Math.sqrt(Math.pow(e.getPositionEyes(ticks).xCoord - bp.getX(), 2)
				+ Math.pow(e.getPositionEyes(ticks).yCoord - bp.getY(), 2)
				+ Math.pow(e.getPositionEyes(ticks).zCoord - bp.getZ(), 2));
	}

	public static double distanceTo2D(Entity e, BlockPos bp) {
		return Math.sqrt(Math.pow(e.getPositionEyes(ticks).xCoord - bp.getX(), 2)
				+ Math.pow(e.getPositionEyes(ticks).zCoord - bp.getZ(), 2));
	}

}

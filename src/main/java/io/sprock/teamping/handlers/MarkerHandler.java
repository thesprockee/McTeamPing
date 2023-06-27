package io.sprock.teamping.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

import io.sprock.teamping.client.Marker;
import io.sprock.teamping.client.Marker.Symbol;

public class MarkerHandler {
	private static List<Marker> markerList = new ArrayList<>();
	private static int sfxDistanceBlocks = 2;
	private static Integer[] sfxPosition = new Integer[3];

	private static final Minecraft minecraft = Minecraft.getMinecraft();

	public static void doSFX() {

		if (sfxPosition != null) {

			Minecraft.getMinecraft().theWorld.playSound(sfxPosition[0], sfxPosition[1], sfxPosition[2],
					"minecraft:fireworks.blast_far", 0.5F, 1F, false);

			sfxPosition = null;
		}
	}

	public static void expireMarkers() {
		Iterator<Marker> markerIter = markerList.iterator();
		while (markerIter.hasNext()) {
			Marker marker = markerIter.next();

			if ((System.currentTimeMillis() - marker.getTimestamp().getTime()) > 15000) {
				markerIter.remove();
			}
		}
	}

	public static List<Marker> getMarkers() {
		return markerList;
	}

	public static void markBlock(int x, int y, int z, Symbol symbol) {

		markerList.add(Marker.fromData(x, y, z, symbol));

		BlockPos playerPos = minecraft.thePlayer.getPosition();
		Integer[] playerpos = new Integer[] { playerPos.getX(), playerPos.getY(), playerPos.getZ() };

		Integer[] blockps = new Integer[3];

		blockps[0] = Math.min(sfxDistanceBlocks, Math.max(-sfxDistanceBlocks, playerpos[0] - x));
		blockps[1] = Math.min(sfxDistanceBlocks, Math.max(-sfxDistanceBlocks, playerpos[1] - y));
		blockps[2] = Math.min(sfxDistanceBlocks, Math.max(-sfxDistanceBlocks, playerpos[2] - z));

		sfxPosition[0] = playerpos[0] - blockps[0];
		sfxPosition[1] = playerpos[1] - blockps[1];
		sfxPosition[2] = playerpos[2] - blockps[2];
	}
}

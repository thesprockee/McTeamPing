package io.sprock.teamping.render;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

import static io.sprock.teamping.TeamPing.MOD_ID;
import static io.sprock.teamping.client.SendData.pingBlockUnderCursor;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import io.sprock.teamping.TeamPing;
import io.sprock.teamping.client.Marker;
import io.sprock.teamping.client.Marker.Symbol;

import net.minecraftforge.client.event.RenderGameOverlayEvent;

@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class MarkerSelectGuiRenderer {

	private static Minecraft mc = Minecraft.getMinecraft();
	private static Tessellator tes = Tessellator.getInstance();
	private static WorldRenderer wr = tes.getWorldRenderer();

	private static final double sensitivity = pow(mc.gameSettings.mouseSensitivity / 4 + 0.2, 3) * 8.0F;
	private static final int minimumSelectDistance = 4;

	private static final int defaultMarker = Marker.getTextureIndex(Symbol.HERE);
	private static boolean isActive = false;

	private static double cursorPosX = 0;
	private static double cursorPosY = 0;
	private static int previousSelectedSymbolIndex = -1;
	private static int selectedSymbolIndex = 0;

	public static boolean isActive() {
		return isActive;
	}

	public static void onGuiRenderEvent(RenderGameOverlayEvent.Pre event) {

		if (!MarkerSelectGuiRenderer.isActive) {
			// reset the cursor to the center (on cross-hair)
			cursorPosX = 0;
			cursorPosY = 0;
			return; // skip rendering
		}

		try {

			ScaledResolution sr = new ScaledResolution(mc);
			double screenWidth = sr.getScaledWidth_double();
			double screenHeight = sr.getScaledHeight_double();

			double spokeOffset = 10;
			double spokeLength = 16;

			double spokeEnd = spokeOffset + spokeLength;

			GlStateManager.enableBlend();
			GlStateManager.disableTexture2D();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			wr.setTranslation(screenWidth / 2, screenHeight / 2, 0);

			updateCursor();

			double[][] cardinalSpokes = { { 0.0, spokeOffset }, { 0.0, spokeEnd }, { spokeOffset, 0.0 },
					{ spokeEnd, 0.0 }, { 0.0, -spokeOffset }, { 0.0, -spokeEnd }, { -spokeOffset, 0.0 },
					{ -spokeEnd, 0.0 }, };

			wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			GL11.glLineWidth(2);
			for (double[] verts : cardinalSpokes)
				wr.pos(verts[0], verts[1], 0.0D).color(0, 0, 255, 127).endVertex();
			tes.draw();

			double startx = cos(PI / 4) * spokeOffset;
			double starty = sin(PI / 4) * spokeOffset;
			double endx = cos(PI / 4) * spokeEnd - 0.35;
			double endy = sin(PI / 4) * spokeEnd - 0.35;

			double[][] ordinalSpokes = { { startx, -starty }, { endx, -endy }, { startx, starty }, { endx, endy },
					{ -startx, starty }, { -endx, endy }, { -startx, -starty }, { -endx, -endy }, };

			wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			GL11.glLineWidth(3);
			for (double[] verts : ordinalSpokes)
				wr.pos(verts[0], verts[1], 0.0D).color(255, 0, 0, 127).endVertex();
			tes.draw();

			wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			GL11.glLineWidth(1);
			double somethings[][] = { { endx, -endy }, { endx, -endy + 0.75 }, { endx, endy }, { endx, endy - 0.75 },
					{ -endx, endy }, { -endx, endy - 0.75 }, { -endx, -endy }, { -endx, -endy + 0.75 }, };
			for (double[] verts : somethings)
				wr.pos(verts[0], verts[1], 0.0D).color(0, 255, 255, 127).endVertex();
			tes.draw();

			double[][] ordinalEnds = { { startx - 0.5, -starty }, { startx - 0.5, starty }, { -startx + 0.5, starty },
					{ -startx + 0.5, -starty }, };

			wr.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
			GL11.glPointSize(1);
			for (double[] verts : ordinalEnds)
				wr.pos(verts[0], verts[1], 0.0D).color(0, 255, 0, 127).endVertex();
			tes.draw();

			double mos = (sqrt(pow(16, 2) + pow(16, 2))) / 2;
			double midx = cos(PI / 4) * (spokeEnd + 8);
			double midy = sin(PI / 4) * (spokeEnd + 8);
			midx = midx - 0.25;
			midy = midy - 0.25;
			int alpha = 128;

			// background squares
			double[][][] squares = { {

					{ spokeEnd, -8, 2 }, { spokeEnd, 8, 2 }, { spokeEnd + 16, 8, 2 }, { spokeEnd + 16, -8, 2 },

					}, {

							{ -8, -spokeEnd - 16, 0 }, { -8, -spokeEnd, 0 }, { 8, -spokeEnd, 0 },
							{ 8, -spokeEnd - 16, 0 },

					}, {

							{ -spokeEnd - 16, -8, 6 }, { -spokeEnd - 16, 8, 6 }, { -spokeEnd, 8, 6 },
							{ -spokeEnd, -8, 6 },

					}, {

							{ -8, spokeEnd, 4 }, { -8, spokeEnd + 16, 4 }, { 8, spokeEnd + 16, 4 }, { 8, spokeEnd, 4 },

					}, {

							{ midx - mos, midy, 3 }, { midx, midy + mos, 3 }, { midx + mos, midy, 3 },
							{ midx, midy - mos, 3 },

					}, {

							{ -midx - mos, midy, 5 }, { -midx, midy + mos, 5 }, { -midx + mos, midy, 5 },
							{ -midx, midy - mos, 5 },

					}, {

							{ -midx - mos, -midy, 7 }, { -midx, -midy + mos, 7 }, { -midx + mos, -midy, 7 },
							{ -midx, -midy - mos, 7 },

					}, {

							{ midx - mos, -midy, 1 }, { midx, -midy + mos, 1 }, { midx + mos, -midy, 1 },
							{ midx, -midy - mos, 1 },

					},

			};

			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			for (double[][] square : squares) {
				for (double[] v : square)
					wr.pos(v[0], v[1], 0).color(32, 32, 32, (selectedSymbolIndex == v[2]) ? alpha + 128 : alpha)
							.endVertex();
			}
			tes.draw();

			double minU;
			double maxU;
			mc.renderEngine.bindTexture(new ResourceLocation(MOD_ID, TeamPing.markerTexturePath));
			GlStateManager.enableTexture2D();
			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

			minU = 0.125 * 0;
			maxU = minU + 0.125;
			wr.pos(-8, -spokeEnd - 16, 0.0D).tex(minU, 0).color(8, 202, 209, min(255, alpha)).endVertex();// Top
			wr.pos(-8, -spokeEnd, 0.0D).tex(minU, 1).color(8, 202, 209, min(255, alpha)).endVertex(); // Top
			wr.pos(8, -spokeEnd, 0.0D).tex(maxU, 1).color(8, 202, 209, min(255, alpha)).endVertex(); // Top
			wr.pos(8, -spokeEnd - 16, 0.0D).tex(maxU, 0).color(8, 202, 209, min(255, alpha)).endVertex(); // Top

			tes.draw();

			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			minU = 0.125 * 1;
			maxU = minU + 0.125;
			wr.pos(midx - 8, -midy - 8, 0).tex(minU, 0).color(199, 128, 232, min(255, alpha)).endVertex(); // Top-Right

			wr.pos(midx - 8, -midy + 8, 0).tex(minU, 1).color(199, 128, 232, min(255, alpha)).endVertex(); // Top-Right

			wr.pos(midx + 8, -midy + 8, 0).tex(maxU, 1).color(199, 128, 232, min(255, alpha)).endVertex(); // Top-Right

			wr.pos(midx + 8, -midy - 8, 0).tex(maxU, 0).color(199, 128, 232, min(255, alpha)).endVertex(); // Top-Right

			tes.draw();

			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			minU = 0.125 * 2;
			maxU = minU + 0.125;
			wr.pos(spokeEnd, -8, 0.0D).tex(minU, 0).color(255, 180, 128, min(255, alpha)).endVertex(); // Right

			wr.pos(spokeEnd, 8, 0.0D).tex(minU, 1).color(255, 180, 128, min(255, alpha)).endVertex(); // Right

			wr.pos(spokeEnd + 16, 8, 0.0D).tex(maxU, 1).color(255, 180, 128, min(255, alpha)).endVertex(); // Right

			wr.pos(spokeEnd + 16, -8, 0.0D).tex(maxU, 0).color(255, 180, 128, min(255, alpha)).endVertex(); // Right

			tes.draw();

			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			minU = 0.125 * 3;
			maxU = minU + 0.125;
			wr.pos(midx - 8, midy - 8, 0).tex(minU, 0).color(89, 173, 246, min(255, alpha)).endVertex(); // Bottom-Right

			wr.pos(midx - 8, midy + 8, 0).tex(minU, 1).color(89, 173, 246, min(255, alpha)).endVertex(); // Bottom-Right

			wr.pos(midx + 8, midy + 8, 0).tex(maxU, 1).color(89, 173, 246, min(255, alpha)).endVertex(); // Bottom-Right

			wr.pos(midx + 8, midy - 8, 0).tex(maxU, 0).color(89, 173, 246, min(255, alpha)).endVertex(); // Bottom-Right

			tes.draw();

			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			minU = 0.125 * 4;
			maxU = minU + 0.125;
			wr.pos(-8, spokeEnd, 0.0D).tex(minU, 0).color(255, 105, 97, min(255, alpha)).endVertex(); // Bottom

			wr.pos(-8, spokeEnd + 16, 0.0D).tex(minU, 1).color(255, 105, 97, min(255, alpha)).endVertex();// Bottom

			wr.pos(8, spokeEnd + 16, 0.0D).tex(maxU, 1).color(255, 105, 97, min(255, alpha)).endVertex(); // Bottom

			wr.pos(8, spokeEnd, 0.0D).tex(maxU, 0).color(255, 105, 97, min(255, alpha)).endVertex(); // Bottom

			tes.draw();

			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			minU = 0.125;
			maxU = minU + 0.125;
			wr.pos(-midx - 8, midy - 8, 0).tex(minU, 0).color(66, 214, 164, min(255, alpha)).endVertex(); // Bottom-Left

			wr.pos(-midx - 8, midy + 8, 0).tex(minU, 1).color(66, 214, 164, min(255, alpha)).endVertex(); // Bottom-Left

			wr.pos(-midx + 8, midy + 8, 0).tex(maxU, 1).color(66, 214, 164, min(255, alpha)).endVertex(); // Bottom-Left

			wr.pos(-midx + 8, midy - 8, 0).tex(maxU, 0).color(66, 214, 164, min(255, alpha)).endVertex(); // Bottom-Left

			tes.draw();

			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			minU = 0.125 * 6;
			maxU = minU + 0.125;
			wr.pos(-spokeEnd - 16, -8, 0.0D).tex(minU, 0).color(157, 148, 255, min(255, alpha)).endVertex();// Right

			wr.pos(-spokeEnd - 16, 8, 0.0D).tex(minU, 1).color(157, 148, 255, min(255, alpha)).endVertex(); // Right

			wr.pos(-spokeEnd, 8, 0.0D).tex(maxU, 1).color(157, 148, 255, min(255, alpha)).endVertex(); // Right

			wr.pos(-spokeEnd, -8, 0.0D).tex(maxU, 0).color(157, 148, 255, min(255, alpha)).endVertex(); // Right

			tes.draw();

			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			minU = 0.125 * 7;
			maxU = minU + 0.125;
			wr.pos(-midx - 8, -midy - 8, 0).tex(minU, 0).color(248, 243, 141, min(255, alpha)).endVertex(); // Top-Left

			wr.pos(-midx - 8, -midy + 8, 0).tex(minU, 1).color(248, 243, 141, min(255, alpha)).endVertex(); // Top-Left

			wr.pos(-midx + 8, -midy + 8, 0).tex(maxU, 1).color(248, 243, 141, min(255, alpha)).endVertex(); // Top-Left

			wr.pos(-midx + 8, -midy - 8, 0).tex(maxU, 0).color(248, 243, 141, min(255, alpha)).endVertex(); // Top-Left

			tes.draw();

			GlStateManager.disableTexture2D();

		} catch (Exception e) {
			try {
				wr.finishDrawing();
			} catch (IllegalStateException ignored) {
			}
		} finally {
			wr.setTranslation(0, 0, 0);
			GL11.glLineWidth(2);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
		}
	}

	public static void setActive(boolean isActive) {
		MarkerSelectGuiRenderer.isActive = isActive;
	}

	public static void triggerSelection() {
		pingBlockUnderCursor(Marker.getCode(selectedSymbolIndex));
	}

	private static void updateCursor() {

		cursorPosX = cursorPosX + mc.mouseHelper.deltaX * sensitivity;
		cursorPosY = cursorPosY - mc.mouseHelper.deltaY * sensitivity;
		cursorPosX = min(cursorPosX, 40);
		cursorPosX = max(cursorPosX, -40);
		cursorPosY = min(cursorPosY, 40);
		cursorPosY = max(cursorPosY, -40);
		double angle = toDegrees((atan2(-cursorPosX, cursorPosY) + PI));
		double cursorDistanceFromCenter = sqrt(pow(cursorPosX, 2) + pow(cursorPosY, 2));

		if (cursorDistanceFromCenter > minimumSelectDistance) {
			selectedSymbolIndex = (int) floor((angle - 22.5) / 45) + 1;

			if (selectedSymbolIndex == 8)
				selectedSymbolIndex = defaultMarker;
		} else {
			selectedSymbolIndex = defaultMarker;
		}

		wr.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
		GL11.glPointSize(8);
		wr.pos(cursorPosX, cursorPosY, 0).color(255, 255, 255, 127).endVertex();
		tes.draw();

		if (previousSelectedSymbolIndex != selectedSymbolIndex) {
			Minecraft.getMinecraft().thePlayer.playSound("minecraft:random.wood_click", 0.1F, 2);
			previousSelectedSymbolIndex = selectedSymbolIndex;
		}
	}

}
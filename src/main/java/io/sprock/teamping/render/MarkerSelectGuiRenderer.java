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

import net.minecraftforge.client.event.RenderGameOverlayEvent;

@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class MarkerSelectGuiRenderer {

	private static final int minimumSelectDistance = 4;

	private static boolean isActive = false;

	private static final int transitionDuration = 15;

	private static final int defaultMarkerType = 0;
	private static int transitionTickCounter = 0;
	private static double transitionProgress = 0.0;

	private static double cursorPosX = 0;
	private static double cursorPosY = 0;
	private static int previousMarkerTypeSelection = -1;
	private static int selectedMarkerType = 0;

	public static boolean isActive() {
		return isActive;
	}

	public static void setActive(boolean isActive) {
		MarkerSelectGuiRenderer.isActive = isActive;
	}

	public static void onGuiRenderEvent(RenderGameOverlayEvent.Pre event) {

		if (MarkerSelectGuiRenderer.isActive) {
			if (transitionTickCounter < transitionDuration) {
				transitionTickCounter++;
			}
		} else if (transitionTickCounter > 0) {
			transitionTickCounter--;
		} else {
			// reset the cursor to the center (on cross-hair)
			cursorPosX = 0;
			cursorPosY = 0;
			return; // skip rendering
		}

		transitionProgress = transitionTickCounter / transitionDuration;

		Minecraft mc = Minecraft.getMinecraft();
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer wr = tes.getWorldRenderer();

		try {
			ScaledResolution sr = new ScaledResolution(mc);
			double screenWidth = sr.getScaledWidth_double();
			double screenHeight = sr.getScaledHeight_double();

			double spokeOffset = 10;
			double spokeLength = 16;

			double spokeEnd = spokeOffset + spokeLength * transitionProgress;

			GlStateManager.enableBlend();
			GlStateManager.disableTexture2D();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			wr.setTranslation(screenWidth / 2, screenHeight / 2, 0);

			// spoke lines

			wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			GL11.glLineWidth(2);
			wr.pos(0, spokeOffset, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom
			wr.pos(0, spokeEnd, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom
			wr.pos(spokeOffset, 0, 0.0D).color(64, 64, 64, 127).endVertex(); // Right
			wr.pos(spokeEnd, 0, 0.0D).color(64, 64, 64, 127).endVertex(); // Right
			wr.pos(0, -spokeOffset, 0.0D).color(64, 64, 64, 127).endVertex(); // Top
			wr.pos(0, -spokeEnd, 0.0D).color(64, 64, 64, 127).endVertex(); // Top
			wr.pos(-spokeOffset, 0, 0.0D).color(64, 64, 64, 127).endVertex(); // Left
			wr.pos(-spokeEnd, 0, 0.0D).color(64, 64, 64, 127).endVertex(); // Left
			tes.draw();

			double startx = cos(PI / 4) * spokeOffset;
			double starty = sin(PI / 4) * spokeOffset;
			double endx = cos(PI / 4) * spokeEnd - 0.35;
			double endy = sin(PI / 4) * spokeEnd - 0.35;

			wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			GL11.glLineWidth(3);
			wr.pos(startx, -starty, 0.0D).color(64, 64, 64, 127).endVertex(); // Top-Right
			wr.pos(endx, -endy, 0.0D).color(64, 64, 64, 127).endVertex(); // Top-Right
			wr.pos(startx, starty, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom-Right
			wr.pos(endx, endy, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom-Right
			wr.pos(-startx, starty, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom-Left
			wr.pos(-endx, endy, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom-Left
			wr.pos(-startx, -starty, 0.0D).color(64, 64, 64, 127).endVertex();// Top-Left
			wr.pos(-endx, -endy, 0.0D).color(64, 64, 64, 127).endVertex();// Top-Left
			tes.draw();

			wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			GL11.glLineWidth(1);
			wr.pos(endx, -endy, 0.0D).color(64, 64, 64, 127).endVertex(); // Top-Right
			wr.pos(endx, -endy + 0.75, 0.0D).color(64, 64, 64, 127).endVertex(); // Top-Right
			wr.pos(endx, endy, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom-Right
			wr.pos(endx, endy - 0.75, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom-Right
			wr.pos(-endx, endy, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom-Left
			wr.pos(-endx, endy - 0.75, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom-Left
			wr.pos(-endx, -endy, 0.0D).color(64, 64, 64, 127).endVertex();// Top-Left
			wr.pos(-endx, -endy + 0.75, 0.0D).color(64, 64, 64, 127).endVertex();// Top-Left
			tes.draw();

			wr.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
			GL11.glPointSize(1);
			wr.pos(startx - 0.5, -starty, 0.0D).color(64, 64, 64, 127).endVertex(); // Top-Right
			wr.pos(startx - 0.5, starty, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom-Right
			wr.pos(-startx + 0.5, starty, 0.0D).color(64, 64, 64, 127).endVertex(); // Bottom-Left
			wr.pos(-startx + 0.5, -starty, 0.0D).color(64, 64, 64, 127).endVertex(); // Top-Left
			tes.draw();

			double sensitivity = pow(mc.gameSettings.mouseSensitivity / 4 + 0.2, 3) * 8.0F;

			if (transitionProgress == 1.0) {
				cursorPosX = cursorPosX + mc.mouseHelper.deltaX * sensitivity;
				cursorPosY = cursorPosY - mc.mouseHelper.deltaY * sensitivity;
				cursorPosX = min(cursorPosX, 40);
				cursorPosX = max(cursorPosX, -40);
				cursorPosY = min(cursorPosY, 40);
				cursorPosY = max(cursorPosY, -40);
				double angle = toDegrees((atan2(-cursorPosX, cursorPosY) + PI));
				double cursorDistanceFromCenter = sqrt(pow(cursorPosX, 2) + pow(cursorPosY, 2));

				if (cursorDistanceFromCenter > minimumSelectDistance) {
					selectedMarkerType = (int) floor((angle - 22.5) / 45) + 1;

					if (selectedMarkerType == 8)
						selectedMarkerType = defaultMarkerType;
				} else {
					selectedMarkerType = defaultMarkerType;
				}

				wr.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
				GL11.glPointSize(4);
				wr.pos(cursorPosX, cursorPosY, 0).color(25, 25, 25, 127).endVertex();
				tes.draw();

				if (previousMarkerTypeSelection != selectedMarkerType) {
					Minecraft.getMinecraft().thePlayer.playSound("minecraft:random.wood_click", 0.1F, 2);
					previousMarkerTypeSelection = selectedMarkerType;
				}

				double mos = (sqrt(pow(16, 2) + pow(16, 2))) / 2;
				double midx = cos(PI / 4) * (spokeEnd + 8);
				double midy = sin(PI / 4) * (spokeEnd + 8);

				int alpha = (int) Math.round(128 * transitionProgress);

				wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				wr.pos(spokeEnd, -8, 0.0D).color(32, 32, 32, (selectedMarkerType == 2) ? alpha + 128 : alpha)
						.endVertex(); // Right
				// 2
				wr.pos(spokeEnd, 8, 0.0D).color(32, 32, 32, (selectedMarkerType == 2) ? alpha + 128 : alpha)
						.endVertex(); // Right
				// 2
				wr.pos(spokeEnd + 16, 8, 0.0D).color(32, 32, 32, (selectedMarkerType == 2) ? alpha + 128 : alpha)
						.endVertex(); // Right
				// 2
				wr.pos(spokeEnd + 16, -8, 0.0D).color(32, 32, 32, (selectedMarkerType == 2) ? alpha + 128 : alpha)
						.endVertex(); // Right
				// 2
				wr.pos(-8, -spokeEnd - 16, 0.0D).color(32, 32, 32, (selectedMarkerType == 0) ? alpha + 128 : alpha)
						.endVertex(); // Top
				// 0
				wr.pos(-8, -spokeEnd, 0.0D).color(32, 32, 32, (selectedMarkerType == 0) ? alpha + 128 : alpha)
						.endVertex(); // Top
				// 0
				wr.pos(8, -spokeEnd, 0.0D).color(32, 32, 32, (selectedMarkerType == 0) ? alpha + 128 : alpha)
						.endVertex(); // Top 0
				wr.pos(8, -spokeEnd - 16, 0.0D).color(32, 32, 32, (selectedMarkerType == 0) ? alpha + 128 : alpha)
						.endVertex(); // Top
				// 0
				wr.pos(-spokeEnd - 16, -8, 0.0D).color(32, 32, 32, (selectedMarkerType == 6) ? alpha + 128 : alpha)
						.endVertex(); // Left
				// 6
				wr.pos(-spokeEnd - 16, 8, 0.0D).color(32, 32, 32, (selectedMarkerType == 6) ? alpha + 128 : alpha)
						.endVertex(); // Left
				// 6
				wr.pos(-spokeEnd, 8, 0.0D).color(32, 32, 32, (selectedMarkerType == 6) ? alpha + 128 : alpha)
						.endVertex(); // Left
				// 6
				wr.pos(-spokeEnd, -8, 0.0D).color(32, 32, 32, (selectedMarkerType == 6) ? alpha + 128 : alpha)
						.endVertex(); // Left
				// 6
				wr.pos(-8, spokeEnd, 0.0D).color(32, 32, 32, (selectedMarkerType == 4) ? alpha + 128 : alpha)
						.endVertex(); // Bottom
				// 4
				wr.pos(-8, spokeEnd + 16, 0.0D).color(32, 32, 32, (selectedMarkerType == 4) ? alpha + 128 : alpha)
						.endVertex(); // Bottom
				// 4
				wr.pos(8, spokeEnd + 16, 0.0D).color(32, 32, 32, (selectedMarkerType == 4) ? alpha + 128 : alpha)
						.endVertex(); // Bottom
				// 4
				wr.pos(8, spokeEnd, 0.0D).color(32, 32, 32, (selectedMarkerType == 4) ? alpha + 128 : alpha)
						.endVertex(); // Bottom
				// 4
				midx = midx - 0.25;
				midy = midy - 0.25;
				wr.pos(midx - mos, midy, 0).color(32, 32, 32, (selectedMarkerType == 3) ? alpha + 128 : alpha)
						.endVertex(); // Bottom-Right
				// 4
				wr.pos(midx, midy + mos, 0).color(32, 32, 32, (selectedMarkerType == 3) ? alpha + 128 : alpha)
						.endVertex(); // Bottom-Right
				// 4
				wr.pos(midx + mos, midy, 0).color(32, 32, 32, (selectedMarkerType == 3) ? alpha + 128 : alpha)
						.endVertex(); // Bottom-Right
				// 4
				wr.pos(midx, midy - mos, 0).color(32, 32, 32, (selectedMarkerType == 3) ? alpha + 128 : alpha)
						.endVertex(); // Bottom-Right
				// 4
				wr.pos(-midx - mos, midy, 0).color(32, 32, 32, (selectedMarkerType == 5) ? alpha + 128 : alpha)
						.endVertex(); // Bottom-Left
				// 6
				wr.pos(-midx, midy + mos, 0).color(32, 32, 32, (selectedMarkerType == 5) ? alpha + 128 : alpha)
						.endVertex(); // Bottom-Left
				// 6
				wr.pos(-midx + mos, midy, 0).color(32, 32, 32, (selectedMarkerType == 5) ? alpha + 128 : alpha)
						.endVertex(); // Bottom-Left
				// 6
				wr.pos(-midx, midy - mos, 0).color(32, 32, 32, (selectedMarkerType == 5) ? alpha + 128 : alpha)
						.endVertex(); // Bottom-Left
				// 6
				wr.pos(-midx - mos, -midy, 0).color(32, 32, 32, (selectedMarkerType == 7) ? alpha + 128 : alpha)
						.endVertex(); // Top-Left
				// 8
				wr.pos(-midx, -midy + mos, 0).color(32, 32, 32, (selectedMarkerType == 7) ? alpha + 128 : alpha)
						.endVertex(); // Top-Left
				// 8
				wr.pos(-midx + mos, -midy, 0).color(32, 32, 32, (selectedMarkerType == 7) ? alpha + 128 : alpha)
						.endVertex(); // Top-Left
				// 8
				wr.pos(-midx, -midy - mos, 0).color(32, 32, 32, (selectedMarkerType == 7) ? alpha + 128 : alpha)
						.endVertex(); // Top-Left
				// 8
				wr.pos(midx - mos, -midy, 0).color(32, 32, 32, (selectedMarkerType == 1) ? alpha + 128 : alpha)
						.endVertex(); // Top-Right
				// 2
				wr.pos(midx, -midy + mos, 0).color(32, 32, 32, (selectedMarkerType == 1) ? alpha + 128 : alpha)
						.endVertex(); // Top-Right
				// 2
				wr.pos(midx + mos, -midy, 0).color(32, 32, 32, (selectedMarkerType == 1) ? alpha + 128 : alpha)
						.endVertex(); // Top-Right
				// 2
				wr.pos(midx, -midy - mos, 0).color(32, 32, 32, (selectedMarkerType == 1) ? alpha + 128 : alpha)
						.endVertex(); // Top-Right
				// 2
				tes.draw();

				double minU;
				double maxU;
				mc.renderEngine.bindTexture(new ResourceLocation(MOD_ID, TeamPing.markerTexturePath));
				GlStateManager.enableTexture2D();
				wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

				minU = 0.125 * 0;
				maxU = minU + 0.125;
				wr.pos(-8, -spokeEnd - 16, 0.0D).tex(minU, 0).color(8, 202, 209, min(255, alpha * transitionDuration))
						.endVertex();// Top
				// 0
				wr.pos(-8, -spokeEnd, 0.0D).tex(minU, 1).color(8, 202, 209, min(255, alpha * transitionDuration))
						.endVertex(); // Top 0
				wr.pos(8, -spokeEnd, 0.0D).tex(maxU, 1).color(8, 202, 209, min(255, alpha * transitionDuration))
						.endVertex(); // Top 0
				wr.pos(8, -spokeEnd - 16, 0.0D).tex(maxU, 0).color(8, 202, 209, min(255, alpha * transitionDuration))
						.endVertex(); // Top
				// 0
				tes.draw();

				wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				minU = 0.125 * 1;
				maxU = minU + 0.125;
				wr.pos(midx - 8, -midy - 8, 0).tex(minU, 0).color(199, 128, 232, min(255, alpha * transitionDuration))
						.endVertex(); // Top-Right
				// 1
				wr.pos(midx - 8, -midy + 8, 0).tex(minU, 1).color(199, 128, 232, min(255, alpha * transitionDuration))
						.endVertex(); // Top-Right
				// 1
				wr.pos(midx + 8, -midy + 8, 0).tex(maxU, 1).color(199, 128, 232, min(255, alpha * transitionDuration))
						.endVertex(); // Top-Right
				// 1
				wr.pos(midx + 8, -midy - 8, 0).tex(maxU, 0).color(199, 128, 232, min(255, alpha * transitionDuration))
						.endVertex(); // Top-Right
				// 1
				tes.draw();

				wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				minU = 0.125 * 2;
				maxU = minU + 0.125;
				wr.pos(spokeEnd, -8, 0.0D).tex(minU, 0).color(255, 180, 128, min(255, alpha * transitionDuration))
						.endVertex(); // Right
				// 2
				wr.pos(spokeEnd, 8, 0.0D).tex(minU, 1).color(255, 180, 128, min(255, alpha * transitionDuration))
						.endVertex(); // Right
				// 2
				wr.pos(spokeEnd + 16, 8, 0.0D).tex(maxU, 1).color(255, 180, 128, min(255, alpha * transitionDuration))
						.endVertex(); // Right
				// 2
				wr.pos(spokeEnd + 16, -8, 0.0D).tex(maxU, 0).color(255, 180, 128, min(255, alpha * transitionDuration))
						.endVertex(); // Right
				// 2
				tes.draw();

				wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				minU = 0.125 * 3;
				maxU = minU + 0.125;
				wr.pos(midx - 8, midy - 8, 0).tex(minU, 0).color(89, 173, 246, min(255, alpha * transitionDuration))
						.endVertex(); // Bottom-Right
				// 3
				wr.pos(midx - 8, midy + 8, 0).tex(minU, 1).color(89, 173, 246, min(255, alpha * transitionDuration))
						.endVertex(); // Bottom-Right
				// 3
				wr.pos(midx + 8, midy + 8, 0).tex(maxU, 1).color(89, 173, 246, min(255, alpha * transitionDuration))
						.endVertex(); // Bottom-Right
				// 3
				wr.pos(midx + 8, midy - 8, 0).tex(maxU, 0).color(89, 173, 246, min(255, alpha * transitionDuration))
						.endVertex(); // Bottom-Right
				// 3
				tes.draw();

				wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				minU = 0.125 * 4;
				maxU = minU + 0.125;
				wr.pos(-8, spokeEnd, 0.0D).tex(minU, 0).color(255, 105, 97, min(255, alpha * transitionDuration))
						.endVertex(); // Bottom
				// 4
				wr.pos(-8, spokeEnd + 16, 0.0D).tex(minU, 1).color(255, 105, 97, min(255, alpha * transitionDuration))
						.endVertex();// Bottom
				// 4
				wr.pos(8, spokeEnd + 16, 0.0D).tex(maxU, 1).color(255, 105, 97, min(255, alpha * transitionDuration))
						.endVertex(); // Bottom
				// 4
				wr.pos(8, spokeEnd, 0.0D).tex(maxU, 0).color(255, 105, 97, min(255, alpha * transitionDuration))
						.endVertex(); // Bottom
				// 4
				tes.draw();

				wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				minU = 0.125 * transitionDuration;
				maxU = minU + 0.125;
				wr.pos(-midx - 8, midy - 8, 0).tex(minU, 0).color(66, 214, 164, min(255, alpha * transitionDuration))
						.endVertex(); // Bottom-Left
				// 5
				wr.pos(-midx - 8, midy + 8, 0).tex(minU, 1).color(66, 214, 164, min(255, alpha * transitionDuration))
						.endVertex(); // Bottom-Left
				// 5
				wr.pos(-midx + 8, midy + 8, 0).tex(maxU, 1).color(66, 214, 164, min(255, alpha * transitionDuration))
						.endVertex(); // Bottom-Left
				// 5
				wr.pos(-midx + 8, midy - 8, 0).tex(maxU, 0).color(66, 214, 164, min(255, alpha * transitionDuration))
						.endVertex(); // Bottom-Left
				// 5
				tes.draw();

				wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				minU = 0.125 * 6;
				maxU = minU + 0.125;
				wr.pos(-spokeEnd - 16, -8, 0.0D).tex(minU, 0).color(157, 148, 255, min(255, alpha * transitionDuration))
						.endVertex();// Right
				// 6
				wr.pos(-spokeEnd - 16, 8, 0.0D).tex(minU, 1).color(157, 148, 255, min(255, alpha * transitionDuration))
						.endVertex(); // Right
				// 6
				wr.pos(-spokeEnd, 8, 0.0D).tex(maxU, 1).color(157, 148, 255, min(255, alpha * transitionDuration))
						.endVertex(); // Right
				// 6
				wr.pos(-spokeEnd, -8, 0.0D).tex(maxU, 0).color(157, 148, 255, min(255, alpha * transitionDuration))
						.endVertex(); // Right
				// 6
				tes.draw();

				wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				minU = 0.125 * 7;
				maxU = minU + 0.125;
				wr.pos(-midx - 8, -midy - 8, 0).tex(minU, 0).color(248, 243, 141, min(255, alpha * transitionDuration))
						.endVertex(); // Top-Left
				// 7
				wr.pos(-midx - 8, -midy + 8, 0).tex(minU, 1).color(248, 243, 141, min(255, alpha * transitionDuration))
						.endVertex(); // Top-Left
				// 7
				wr.pos(-midx + 8, -midy + 8, 0).tex(maxU, 1).color(248, 243, 141, min(255, alpha * transitionDuration))
						.endVertex(); // Top-Left
				// 7
				wr.pos(-midx + 8, -midy - 8, 0).tex(maxU, 0).color(248, 243, 141, min(255, alpha * transitionDuration))
						.endVertex(); // Top-Left
				// 7
				tes.draw();

				GlStateManager.disableTexture2D();
			}
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

	public static void triggerSelection() {
		pingBlockUnderCursor(Marker.getCode(selectedMarkerType));
	}

}
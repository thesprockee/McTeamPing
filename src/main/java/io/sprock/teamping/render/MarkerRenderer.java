package io.sprock.teamping.render;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static net.minecraft.client.particle.EntityFX.interpPosX;
import static net.minecraft.client.particle.EntityFX.interpPosY;
import static net.minecraft.client.particle.EntityFX.interpPosZ;

import static io.sprock.teamping.TeamPing.MOD_ID;
import static io.sprock.teamping.TeamPing.markerList;
import static io.sprock.teamping.util.UtilMethods.distanceTo2D;
import static io.sprock.teamping.util.UtilMethods.distanceTo3D;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import io.sprock.teamping.TeamPing;
import io.sprock.teamping.client.Marker;
import io.sprock.teamping.config.Config;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MarkerRenderer {
	private static final double OUTLINE_OPACITY = 0.75;
	private static final double BOX_OPACITY = 0.15;
	private static int MARKER_TIMEOUT_MS = 15 * 1000;
	private static int MARKER_FADE_MS = MARKER_TIMEOUT_MS / 1;
	private static final int HIDE_PROXIMITY = 3; // blocks
	private static final int DIM_PROXIMITY = 6; // blocks
	private static final Color BLOCK_COLOR = new Color(255, 255, 255);

	private static final ResourceLocation markerTexture = new ResourceLocation(MOD_ID, TeamPing.markerTexturePath);

	private static final Minecraft minecraft = Minecraft.getMinecraft();
	private static final Tessellator tessellator = Tessellator.getInstance();
	private static final WorldRenderer wRenderer = tessellator.getWorldRenderer();
	private static Entity viewEntity = minecraft.getRenderViewEntity();

	private static double oldy = 0;
	private static double newy = 0;

	private static final int[] BLOCK_RGB = { BLOCK_COLOR.getRed(), BLOCK_COLOR.getGreen(), BLOCK_COLOR.getBlue() };

	public static int getMarkerRenderDistanceChunks() {
		return Minecraft.getMinecraft().gameSettings.renderDistanceChunks + Config.getAdditionalRenderDistanceChunks();
	}

	public static boolean isRenderDistanceExpanded() {
		return (Config.getAdditionalRenderDistanceChunks() > 0);
	}

	public static void onRenderWorldLast(float pticks) {
		try {
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();

			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableBlend();
			GlStateManager.disableDepth();
			GlStateManager.disableTexture2D();
			GlStateManager.disableAlpha();

			oldy = viewEntity.prevPosY;
			newy = viewEntity.posY;

			minecraft.renderEngine.bindTexture(markerTexture);

			if (markerList.size() != 0) {
				for (Marker marker : markerList) {

					BlockPos markerBlockPos = marker.getBlockPos();

					int markerAge = (int) (System.currentTimeMillis() - marker.getTimestamp().getTime());

					double proximity3D = distanceTo3D(viewEntity, markerBlockPos);
					double proximity2D = distanceTo2D(viewEntity, markerBlockPos);

					if (isRenderDistanceExpanded()) {
						GlStateManager.disableFog();
						// GlStateManager.disableLighting();
						// GlStateManager.disableFog();

						// GlStateManager.depthMask(true);
						// GlStateManager.disableDepth();
						// GlStateManager.enableBlend();
						// GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
						// GlStateManager.disableLighting();
						// GlStateManager.disableDepth();
						// GlStateManager.disableCull();
						// GlStateManager.disableAlpha();

					}

					if (proximity3D > HIDE_PROXIMITY && proximity2D < getMarkerRenderDistanceChunks() * 16) {

						wRenderer.setTranslation(-interpPosX, -interpPosY, -interpPosZ);
						GL11.glLineWidth((float) (2 / proximity3D));
						AxisAlignedBB aabb = new AxisAlignedBB(markerBlockPos, markerBlockPos.add(1, 1, 1));

						int lifefade = markerAge / 2;
						int lifefadeback = MARKER_FADE_MS - lifefade;
						int alphaValue = (markerAge > MARKER_TIMEOUT_MS) ? 0
								: min(255, (lifefade > 255) ? lifefadeback : lifefade);

						// Reduce the opacity of the marker when within 6 blocks
						if (proximity2D < DIM_PROXIMITY)
							alphaValue = alphaValue / 2;

						drawOutline(aabb, BLOCK_RGB[0], BLOCK_RGB[1], BLOCK_RGB[2],
								(int) (alphaValue * OUTLINE_OPACITY));
						drawBox(aabb, BLOCK_RGB[0], BLOCK_RGB[2], BLOCK_RGB[2], (int) (alphaValue * BOX_OPACITY));

						float bx = markerBlockPos.getX() + 0.5F;
						float by = markerBlockPos.getY() + 0.5F;
						float bz = markerBlockPos.getZ() + 0.5F;

						wRenderer.setTranslation(-interpPosX + bx, -interpPosY, -interpPosZ + bz);

						renderMarker(marker, alphaValue, pticks);

					}
				}
			}
		} catch (Exception e) {
			TeamPing.LOGGER.warn("Caught Exception:", e);
			try {
				Tessellator.getInstance().getWorldRenderer().finishDrawing();
			} catch (IllegalStateException ignored) {
			}
		} finally {
			wRenderer.setTranslation(0, 0, 0);
			GL11.glLineWidth(1);
			GlStateManager.enableTexture2D();
			GlStateManager.enableDepth();
			GlStateManager.enableAlpha();
			GlStateManager.enableFog();
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
		}
	}

	private static void drawOutline(AxisAlignedBB boundingBox, int red, int green, int blue, int alpha) {

		wRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		wRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
		wRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		wRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
		wRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		wRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
	}

	private static void drawBox(AxisAlignedBB boundingBox, int red, int green, int blue, int alpha) {

		// down
		wRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		wRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
		// north
		wRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		wRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
		// west
		wRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		wRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
		// east
		wRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		wRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
		// south
		wRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		wRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
		// up
		wRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		wRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		wRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
	}

	/*
	 * private static void renderMarker(int alpha, int texIndex, float bx, float by,
	 * float bz, int red, int green, int blue, float pticks, BlockPos bp) {
	 */
	private static void renderMarker(Marker marker, int alpha, float pticks) {

		BlockPos bp = marker.getBlockPos();
		Color color = marker.getColor();

		int red = color.getRed();
		int blue = color.getBlue();
		int green = color.getGreen();

		float bx = bp.getX() + 0.5F;
		float by = bp.getY() + 0.5F;
		float bz = bp.getZ() + 0.5F;

		Vec3 player = new Vec3(viewEntity.posX - bx, viewEntity.posY - by + viewEntity.getEyeHeight(),
				viewEntity.posZ - bz);

		double ypos = 1 + oldy + (newy - oldy) * pticks;

		double yaw = -atan2(player.zCoord, player.xCoord) - PI / 2;
		double sinyaw = sin(yaw);
		double cosyaw = cos(yaw);

		GL11.glLineWidth(4);
		wRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		wRenderer.pos(0, bp.getY() + 0.5, 0).color(red, green, blue, alpha / 2).endVertex();
		wRenderer.pos(0, ypos + 1.5, 0).color(red, green, blue, alpha / 2).endVertex();
		tessellator.draw();
		double minU = 0.125 * marker.getTextureIndex();
		double maxU = minU + 0.125;
		GlStateManager.enableTexture2D();
		wRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		if ((ypos - bp.getY()) >= -1) {
			wRenderer.pos(cosyaw, ypos + 0.5, -sinyaw).tex(minU, 1).color(red, green, blue, alpha).endVertex(); // Bottom-left
			wRenderer.pos(cosyaw, ypos + 2.5, -sinyaw).tex(minU, 0).color(red, green, blue, alpha).endVertex(); // Top-left
			wRenderer.pos(-cosyaw, ypos + 2.5, sinyaw).tex(maxU, 0).color(red, green, blue, alpha).endVertex(); // Top-right
			wRenderer.pos(-cosyaw, ypos + 0.5, sinyaw).tex(maxU, 1).color(red, green, blue, alpha).endVertex(); // Bottom-right
		} else {
			wRenderer.pos(cosyaw, ypos + 0.5, -sinyaw).tex(minU, 0).color(red, green, blue, alpha).endVertex(); // Bottom-left
			wRenderer.pos(cosyaw, ypos + 2.5, -sinyaw).tex(minU, 1).color(red, green, blue, alpha).endVertex(); // Top-left
			wRenderer.pos(-cosyaw, ypos + 2.5, sinyaw).tex(maxU, 1).color(red, green, blue, alpha).endVertex(); // Top-right
			wRenderer.pos(-cosyaw, ypos + 0.5, sinyaw).tex(maxU, 0).color(red, green, blue, alpha).endVertex(); // Bottom-right
		}
		tessellator.draw();
		GlStateManager.disableTexture2D();
	}
}
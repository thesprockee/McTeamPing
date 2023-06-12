package io.sprock.teamping.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import io.sprock.teamping.client.MarkerSelectGui;
import net.minecraft.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
public class GuiScreenMixin {
	@ModifyConstant(method = "updateCameraAndRender(FJ)V", constant = @Constant(floatValue = 8.0F, ordinal = 0))
	private float mixin(float constant) {
		if (MarkerSelectGui.isActive())
			return 0.0F;
		else
			return constant;
	}
}

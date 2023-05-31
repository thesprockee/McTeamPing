package io.sprock.teamping.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.sprock.teamping.client.RenderPingInWorld;
import net.minecraft.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
public class GlobalRendererMixin {
  @Inject(
    method = "renderWorldPass(IFJ)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;matrixMode(I)V", ordinal = 7)
  )
  private void mixin(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
		RenderPingInWorld.renderBlock(partialTicks);
	}
}

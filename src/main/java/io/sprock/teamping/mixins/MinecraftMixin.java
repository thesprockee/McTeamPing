package io.sprock.teamping.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.sprock.teamping.client.MarkerSelectGui;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Inject(method = "clickMouse()V", at = @At(value = "HEAD"), cancellable = true)
	private void mixin1(CallbackInfo ci) {
		if (MarkerSelectGui.isActive())
			ci.cancel();
	}

	@Inject(method = "sendClickBlockToController(Z)V", at = @At(value = "HEAD"), cancellable = true)
	private void mixin2(CallbackInfo ci) {
		if (MarkerSelectGui.isActive())
			ci.cancel();
	}

}

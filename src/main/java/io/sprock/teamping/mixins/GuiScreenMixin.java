package io.sprock.teamping.mixins;

import net.minecraft.client.renderer.EntityRenderer;

import static io.sprock.teamping.listeners.EventListener.guimenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityRenderer.class)
public class GuiScreenMixin {
  @ModifyConstant(
    method = "updateCameraAndRender(FJ)V",
    constant = @Constant(floatValue = 8.0F, ordinal = 0)
  )
  private float mixin(float constant) {
    if (guimenu) return 0.0F;
    else return constant;
  }
}

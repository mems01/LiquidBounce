package net.ccbluex.liquidbounce.injection.fabric.mixins.client;

import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class MixinGameOptions {

    @Shadow public int guiScale;

    /**
     * Defaults gui scale to 2
     *
     * @reason Most people use 2x gui scale, so we default to that and most UI elements are designed for it
     * @param callbackInfo Unused
     */
    @Inject(method = "<init>()V", at = @At("RETURN"))
    private void injectGuiScaleDefault(final CallbackInfo callbackInfo) {
        this.guiScale = 2;
    }

}

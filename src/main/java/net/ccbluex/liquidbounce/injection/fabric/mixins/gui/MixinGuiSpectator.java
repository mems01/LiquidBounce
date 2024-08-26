/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.fabric.mixins.gui;

import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpectatorHud.class)
public class MixinSpectatorHud {

    @Inject(method = "renderTooltip", at = @At("RETURN"))
    private void renderTooltipPost(Window p_175264_1_, float p_175264_2_, CallbackInfo callbackInfo) {
        EventManager.INSTANCE.callEvent(new Render2DEvent(p_175264_2_));
        AWTFontRenderer.Companion.garbageCollectionTick();
    }
}
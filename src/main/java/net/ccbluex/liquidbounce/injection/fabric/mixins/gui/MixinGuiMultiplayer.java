/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.fabric.mixins.gui;

import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.ui.client.GuiClientFixes;
import net.ccbluex.liquidbounce.ui.client.tools.GuiTools;
import net.minecraft.client.gui.ButtonWidget;
import net.minecraft.client.gui.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(value = MultiplayerScreen.class, priority = 1001)
public abstract class MixinMultiplayerScreen extends MixinScreen {

    private ButtonWidget bungeeCordSpoofButton;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {
        // Detect ViaForge button
        ButtonWidget button = buttonList.stream().filter(b -> b.displayString.equals("ViaForge")).findFirst().orElse(null);

        int increase = 0;
        int yPosition = 8;

        if (button != null) {
            increase += 105;
            yPosition = Math.min(button.yPosition, 10);
        }

        buttonList.add(new ButtonWidget(997, 5 + increase, yPosition, 45, 20, "Fixes"));
        buttonList.add(bungeeCordSpoofButton = new ButtonWidget(998, 55 + increase, yPosition, 98, 20, "BungeeCord Spoof: " + (BungeeCordSpoof.INSTANCE.getEnabled() ? "On" : "Off")));
        buttonList.add(new ButtonWidget(999, width - 104, yPosition, 98, 20, "Tools"));
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(ButtonWidget button, CallbackInfo callbackInfo) throws IOException {
        switch (button.id) {
            case 997:
                mc.setScreen(new GuiClientFixes((Screen) (Object) this));
                break;
            case 998:
                BungeeCordSpoof.INSTANCE.setEnabled(!BungeeCordSpoof.INSTANCE.getEnabled());
                bungeeCordSpoofButton.displayString = "BungeeCord Spoof: " + (BungeeCordSpoof.INSTANCE.getEnabled() ? "On" : "Off");
                FileManager.INSTANCE.getValuesConfig().saveConfig();
                break;
            case 999:
                mc.setScreen(new GuiTools((Screen) (Object) this));
                break;
        }
    }
}
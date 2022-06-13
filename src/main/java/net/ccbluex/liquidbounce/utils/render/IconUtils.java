/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.nio.ByteBuffer;

@SideOnly(Side.CLIENT)
public final class IconUtils extends MinecraftInstance {

    public static ByteBuffer[] getFavicon() {
        try {
            return new ByteBuffer[]{mc.readImageToBuffer(IconUtils.class.getResourceAsStream("/assets/minecraft/" + LiquidBounce.CLIENT_NAME.toLowerCase() + "/icon_16x16.png")), mc.readImageToBuffer(IconUtils.class.getResourceAsStream("/assets/minecraft/" + LiquidBounce.CLIENT_NAME.toLowerCase() + "/icon_32x32.png"))};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

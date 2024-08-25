/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.minecraft.network.packet.c2s.play.C03PacketPlayer

/*
* Working on Watchdog
* Tested on: mc.hypixel.net
* Credit: @localpthebest / HypixelPacket
*/
object HypixelTimer : NoFallMode("HypixelTimer") {

    override fun onPacket(event: PacketEvent) {
        val player = mc.player ?: return
        val packet = event.packet

        val fallingPlayer = FallingPlayer()

        if (packet is C03PacketPlayer) {
            if (fallingPlayer.findCollision(500) != null && player.fallDistance - player.velocityY >= 3.3) {
                mc.ticker.timerSpeed = 0.5f

                packet.onGround = true
                player.fallDistance = 0f

                WaitTickUtils.scheduleTicks(1) {
                    mc.ticker.timerSpeed = 1f
                }
            }
        }
    }
}
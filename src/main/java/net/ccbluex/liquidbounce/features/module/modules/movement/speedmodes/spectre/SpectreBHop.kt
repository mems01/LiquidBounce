/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.spectre

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe

object SpectreBHop : SpeedMode("SpectreBHop") {
    override fun onMotion() {
        if (!isMoving || mc.player.input.jump) return
        if (mc.player.onGround) {
            strafe(1.1f)
            mc.player.velocityY = 0.44
            return
        }
        strafe()
    }

}
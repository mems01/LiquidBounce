/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.toRadians
import kotlin.math.cos
import kotlin.math.sin

object NCPYPort : SpeedMode("NCPYPort") {
    private var jumps = 0
    override fun onMotion() {
        if (mc.player.isOnLadder || mc.player.isInWater || mc.player.isInLava || mc.player.isInWeb || !isMoving || mc.player.isInWater) return
        if (jumps >= 4 && mc.player.onGround) jumps = 0
        if (mc.player.onGround) {
            mc.player.motionY = if (jumps <= 1) 0.42 else 0.4
            val f = mc.player.rotationYaw.toRadians()
            mc.player.motionX -= sin(f) * 0.2f
            mc.player.motionZ += cos(f) * 0.2f
            jumps++
        } else if (jumps <= 1) mc.player.motionY = -5.0
        strafe()
    }

}
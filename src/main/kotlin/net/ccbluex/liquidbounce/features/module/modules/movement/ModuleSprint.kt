package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.minecraft.util.math.MathHelper

/**
 * Sprint module
 *
 * Sprints automatically.
 */

object ModuleSprint : Module("Sprint", Category.MOVEMENT) {

    val allDirections by boolean("AllDirections", false)
    val blindness by boolean("Blindness", false)
    val hunger by boolean("Hunger", false)
    val stopOnGround by boolean("StopOnGround", true)
    val stopOnAir by boolean("StopOnAir", true)

    fun shouldSprintOmnidirectionally() = enabled && allDirections

    fun shouldIgnoreBlindness() = enabled && blindness

    fun shouldIgnoreHunger() = enabled && hunger

    fun shouldPreventSprint(): Boolean {
        val player = mc.player ?: return false

        val deltaYaw = player.yaw - (RotationManager.currentRotation ?: return false).yaw
        val (forward, sideways) = Pair(player.input.movementForward, player.input.movementSideways)

        val hasForwardMovement =
            forward * MathHelper.cos(deltaYaw * 0.017453292f) + sideways * MathHelper.sin(deltaYaw * 0.017453292f) > 1.0E-5

        val preventSprint =
            (if (player.isOnGround) stopOnGround else stopOnAir) && !shouldSprintOmnidirectionally() && RotationManager.activeConfigurable?.fixVelocity == false && !hasForwardMovement

        return enabled && preventSprint
    }
}
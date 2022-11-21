package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.entity.rotation

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

        val stopSprint = if (player.isOnGround) stopOnGround else stopOnAir

        val preventSprint = stopSprint && !shouldSprintOmnidirectionally() && RotationManager.rotationDifference(
            player.rotation, RotationManager.currentRotation ?: return false
        ) > 30 && RotationManager.activeConfigurable?.fixVelocity == false

        return enabled && preventSprint
    }
}
/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.extensions.stopXZ
import net.ccbluex.liquidbounce.utils.extensions.toRadiansD
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MovementUtils : MinecraftInstance(), Listenable {

    var speed
        get() = mc.player?.run { sqrt(velocityX * velocityX + velocityZ * velocityZ).toFloat() } ?: .0f
        set(value) { strafe(value) }

    val isMoving
        get() = mc.player?.movementInput?.run { moveForward != 0f || moveStrafe != 0f } ?: false

    val hasMotion
        get() = mc.player?.run { velocityX != .0 || velocityY != .0 || velocityZ != .0 } ?: false

    @JvmOverloads
    fun strafe(speed: Float = this.speed, stopWhenNoInput: Boolean = false, moveEvent: MoveEvent? = null) =
        mc.player?.run {
            if (!isMoving) {
                if (stopWhenNoInput) {
                    moveEvent?.zeroXZ()
                    stopXZ()
                }

                return@run
            }

            val yaw = direction
            val x = -sin(yaw) * speed
            val z = cos(yaw) * speed

            if (moveEvent != null) {
                moveEvent.x = x
                moveEvent.z = z
            }

            velocityX = x
            velocityZ = z
        }

    fun forward(distance: Double) =
        mc.player?.run {
            val yaw = rotationYaw.toRadiansD()
            setPosition(posX - sin(yaw) * distance, posY, posZ + cos(yaw) * distance)
        }

    val direction
        get() = mc.player?.run {
                var yaw = rotationYaw
                var forward = 1f

                if (moveForward < 0f) {
                    yaw += 180f
                    forward = -0.5f
                } else if (moveForward > 0f)
                    forward = 0.5f

                if (moveStrafing < 0f) yaw += 90f * forward
                else if (moveStrafing > 0f) yaw -= 90f * forward

                yaw.toRadiansD()
            } ?: 0.0

    fun isOnGround(height: Double) =
        mc.world != null && mc.player != null &&
        mc.world.doesBoxCollide(mc.player, mc.player.boundingBox.offset(0.0, -height, 0.0)).isNotEmpty()

    var serverOnGround = false

    var serverX = .0
    var serverY = .0
    var serverZ = .0

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.isCancelled)
            return

        val packet = event.packet

        if (packet is PlayerMoveC2SPacket) {
            serverOnGround = packet.onGround

            if (packet.isMoving) {
                serverX = packet.x
                serverY = packet.y
                serverZ = packet.z
            }
        }
    }

    override fun handleEvents() = true
}
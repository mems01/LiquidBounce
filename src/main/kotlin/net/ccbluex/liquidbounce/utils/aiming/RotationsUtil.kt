/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2016 - 2022 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */

package net.ccbluex.liquidbounce.utils.aiming

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.entity.rotation
import net.ccbluex.liquidbounce.utils.kotlin.random
import net.ccbluex.liquidbounce.utils.kotlin.step
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * Configurable to configure the dynamic rotation engine
 */
class RotationsConfigurable : Configurable("Rotations") {
    val angleLimit by floatRange("AngleLimit", 5f..10f, 180f..180f)
    val speed by floatRange("Speed", 0.3f..0.6f, 0.05f..1f)
    val fixVelocity by boolean("FixVelocity", true)
    val strict by boolean("Strict", false)
    val horizontalPatternSpeed by float("HorizontalPatternSpeed", 0.01283f, 0.0f..0.09f)
    val verticalPatternSpeed by float("VerticalPatternSpeed", 0.038f, 0.0f..0.09f)
    val movingChance by float("MovingChance", 0.31f, 0f..1f)
    val standingChance by float("StandingChance", 0.91f, 0f..1f)
}

/**
 * A rotation manager
 */
object RotationManager : Listenable {

    var targetRotation: Rotation? = null
    var serverRotation: Rotation? = null

    // Current rotation
    var currentRotation: Rotation? = null
    var ticksUntilReset: Int = 0

    // Active configurable
    var activeConfigurable: RotationsConfigurable? = null

    // useful for something like autopot
    var deactivateManipulation = false

    fun raytraceBlock(eyes: Vec3d, pos: BlockPos, state: BlockState, range: Double, wallsRange: Double): VecRotation? {
        val offset = Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
        val shape = state.getOutlineShape(mc.world, pos, ShapeContext.of(mc.player))

        for (box in shape.boundingBoxes.sortedBy { -(it.maxX - it.minX) * (it.maxY - it.minY) * (it.maxZ - it.minZ) }) {
            return raytraceBox(eyes, box.offset(offset), range, wallsRange, expectedTarget = pos) ?: continue
        }

        return null
    }

    /**
     * Find the best spot of a box to aim at.
     */
    fun raytraceBox(
        eyes: Vec3d,
        box: Box,
        range: Double,
        wallsRange: Double,
        expectedTarget: BlockPos? = null,
        pattern: Pattern = GaussianPattern,
    ): VecRotation? {
        val preferredSpot = pattern.spot(box)
        val preferredRotation = makeRotation(preferredSpot, eyes)

        val rangeSquared = range * range
        val wallsRangeSquared = wallsRange * wallsRange

        var visibleRot: VecRotation? = null
        var notVisibleRot: VecRotation? = null

        for (x in 0.0..1.0 step 0.1) {
            for (y in 0.0..1.0 step 0.1) {
                for (z in 0.0..1.0 step 0.1) {
                    val vec3 = Vec3d(
                        box.minX + (box.maxX - box.minX) * x,
                        box.minY + (box.maxY - box.minY) * y,
                        box.minZ + (box.maxZ - box.minZ) * z
                    )

                    // skip because of out of range
                    val distance = eyes.squaredDistanceTo(vec3)

                    if (distance > rangeSquared) {
                        continue
                    }

                    // check if target is visible to eyes
                    val visible = if (expectedTarget != null) {
                        facingBlock(eyes, vec3, expectedTarget)
                    } else {
                        isVisible(eyes, vec3)
                    }

                    // skip because not visible in range
                    if (!visible && distance > wallsRangeSquared) {
                        continue
                    }

                    val rotation = makeRotation(vec3, eyes)

                    if (visible) {
                        // Calculate next spot to preferred spot
                        if (visibleRot == null || rotationDifference(rotation, preferredRotation) < rotationDifference(
                                visibleRot.rotation, preferredRotation
                            )
                        ) {
                            visibleRot = VecRotation(rotation, vec3)
                        }
                    } else {
                        // Calculate next spot to preferred spot
                        if (notVisibleRot == null || rotationDifference(
                                rotation, preferredRotation
                            ) < rotationDifference(notVisibleRot.rotation, preferredRotation)
                        ) {
                            notVisibleRot = VecRotation(rotation, vec3)
                        }
                    }
                }
            }
        }

        return visibleRot ?: notVisibleRot
    }

    fun aimAt(vec: Vec3d, eyes: Vec3d, ticks: Int = 5, configurable: RotationsConfigurable) =
        aimAt(makeRotation(vec, eyes), ticks, configurable)

    fun aimAt(rotation: Rotation, ticks: Int = 5, configurable: RotationsConfigurable) {
        activeConfigurable = configurable
        targetRotation = rotation
        ticksUntilReset = ticks
    }

    fun makeRotation(vec: Vec3d, eyes: Vec3d): Rotation {
        val diffX = vec.x - eyes.x
        val diffY = vec.y - eyes.y
        val diffZ = vec.z - eyes.z

        return Rotation(
            MathHelper.wrapDegrees(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
            MathHelper.wrapDegrees((-Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))).toFloat())
        )
    }

    /**
     * Update current rotation to new rotation step
     */
    fun update() {
        // Update patterns
        for (pattern in AIMING_PATTERNS) {
            pattern.update()
        }

        // Update rotations
        val angleLimit = this.activeConfigurable?.angleLimit?.random() ?: return
        val speed = this.activeConfigurable?.speed?.random() ?: return

        val playerRotation = mc.player?.rotation ?: return

        if (ticksUntilReset == 0) {
            if (rotationDifference(currentRotation ?: serverRotation ?: return, playerRotation) <= angleLimit) {
                ticksUntilReset = -1

                targetRotation = null
                currentRotation?.let { rotation ->
                    mc.player?.let { player ->
                        player.yaw = rotation.yaw + angleDifference(player.yaw, rotation.yaw)
                        player.renderYaw = player.yaw
                        player.lastRenderYaw = player.yaw
                    }
                }
                currentRotation = null
                return
            }
            currentRotation = limitAngleChange(
                currentRotation ?: serverRotation ?: return, playerRotation, angleLimit, speed
            ).fixedSensitivity()
            return
        }
        targetRotation?.let { targetRotation ->
            currentRotation = limitAngleChange(
                currentRotation ?: playerRotation, targetRotation, angleLimit, speed
            ).fixedSensitivity()
        }
    }

    fun shouldUpdate() = !deactivateManipulation

    /**
     * Calculate difference between the server rotation and your rotation
     */
    fun rotationDifference(rotation: Rotation): Double {
        return rotationDifference(rotation, serverRotation ?: return 0.0)
    }

    /**
     * Calculate difference between two rotations
     */
    fun rotationDifference(a: Rotation, b: Rotation) =
        hypot(angleDifference(a.yaw, b.yaw).toDouble(), (a.pitch - b.pitch).toDouble())

    /**
     * Limit your rotations
     */
    fun limitAngleChange(
        currentRotation: Rotation,
        targetRotation: Rotation,
        angleLimit: Float,
        speed: Float,
    ): Rotation {
        var yawDifference = angleDifference(targetRotation.yaw, currentRotation.yaw)
        var pitchDifference = angleDifference(targetRotation.pitch, currentRotation.pitch)

        yawDifference = (yawDifference - yawDifference * (1 - speed)).coerceIn(-angleLimit, angleLimit)
        pitchDifference = (pitchDifference - pitchDifference * (1 - speed)).coerceIn(-angleLimit, angleLimit)

        return Rotation(currentRotation.yaw + yawDifference, currentRotation.pitch + pitchDifference)
    }

    /**
     * Calculate difference between two angle points
     */
    private fun angleDifference(a: Float, b: Float) = MathHelper.wrapDegrees(a - b)

    /**
     * Modify server-side rotations
     */

    val strafeHandler = handler<PlayerVelocityStrafe> { event ->
        if (activeConfigurable?.fixVelocity == true) {
            event.velocity = fixVelocity(event.velocity, event.movementInput, event.speed)
        }
    }

    val rotationUpdateHandler = handler<ClientRenderEvent> {
        if (targetRotation == null) {
            return@handler
        }

        update()
    }

    val tickHandler = handler<GameTickEvent> {
        // Update reset ticks
        if (ticksUntilReset > 0) {
            ticksUntilReset--
        }
    }

    /**
     * Update server rotations
     */
    val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet

        if (packet !is PlayerMoveC2SPacket || !packet.changesLook()) {
            return@handler
        }

        serverRotation = Rotation(packet.yaw, packet.pitch)

        // serverRotation must always match currentRotation post update
        currentRotation?.let {
            if (rotationDifference(it) != 0.0) {
                serverRotation = it
            }
        }
    }

    /**
     * Fix velocity
     */
    private fun fixVelocity(currVelocity: Vec3d, movementInput: Vec3d, speed: Float): Vec3d {
        currentRotation?.let { rotation ->
            val yaw = rotation.yaw
            val d = movementInput.lengthSquared()

            return if (d < 1.0E-7) {
                Vec3d.ZERO
            } else {
                val vec3d = (if (d > 1.0) movementInput.normalize() else movementInput).multiply(speed.toDouble())

                val f = MathHelper.sin(yaw * 0.017453292f)
                val g = MathHelper.cos(yaw * 0.017453292f)

                Vec3d(
                    vec3d.x * g.toDouble() - vec3d.z * f.toDouble(),
                    vec3d.y,
                    vec3d.z * g.toDouble() + vec3d.x * f.toDouble()
                )
            }
        }

        return currVelocity
    }

}

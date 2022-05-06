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

import net.ccbluex.liquidbounce.features.module.modules.movement.ModuleNoJumpDelay
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

data class Rotation(var yaw: Float, var pitch: Float) {

    val rotationVec: Vec3d
        get() {
            val yawCos = MathHelper.cos(-yaw * 0.017453292f)
            val yawSin = MathHelper.sin(-yaw * 0.017453292f)
            val pitchCos = MathHelper.cos(pitch * 0.017453292f)
            val pitchSin = MathHelper.sin(pitch * 0.017453292f)
            return Vec3d((yawSin * pitchCos).toDouble(), (-pitchSin).toDouble(), (yawCos * pitchCos).toDouble())
        }

    /**
     * Fix rotation based on sensitivity
     */
    fun fixedSensitivity(): Rotation? {
        // gcd might need a fix
        val sensitivity = mc.options.mouseSensitivity
        val f = sensitivity * 0.6000000238418579 + 0.20000000298023224
        val gcd = f * f * f * 1.2
        if (ModuleNoJumpDelay.enabled) {
            chat(gcd.toString())
        }

        // get previous rotation
        val rotation = RotationManager.serverRotation ?: return null

        // fix yaw
        var deltaYaw = (yaw - rotation.yaw).toDouble()
        deltaYaw -= deltaYaw % gcd
        val yaw = (rotation.yaw + deltaYaw).toFloat()

        // fix pitch
        var deltaPitch = (pitch - rotation.pitch).toDouble()
        deltaPitch -= deltaPitch % gcd
        val pitch = (rotation.pitch + deltaPitch).toFloat()
        return Rotation(yaw, pitch)
    }

}

data class VecRotation(val rotation: Rotation, val vec: Vec3d)

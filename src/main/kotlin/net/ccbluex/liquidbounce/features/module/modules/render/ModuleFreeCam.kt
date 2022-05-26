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

package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.ModuleBlink.createClone
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.entity.strafe
import net.ccbluex.liquidbounce.utils.math.times
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.minecraft.util.math.Vec3d

/**
 * FreeCam module
 *
 * Allows you to move out of your body.
 */

object ModuleFreeCam : Module("FreeCam", Category.RENDER) {

    private val speed by float("Speed", 1f, 0.1f..2f)
    private val fly by boolean("Fly", true)
    private val collision by boolean("Collision", false)
    private val resetMotion by boolean("ResetMotion", true)

    private var fakePlayer: OtherClientPlayerEntity? = null
    private var velocity = Vec3d.ZERO
    private var pos = Vec3d.ZERO
    private var ground = false

    override fun enable() {
        if (resetMotion) {
            player.velocity.times(0.0)
        }

        velocity = Vec3d.ZERO
        pos = player.pos
        ground = player.isOnGround

        fakePlayer = createClone() ?: return
        world.addEntity((fakePlayer ?: return).id, fakePlayer)

        if (!collision) {
            player.noClip = true
        }
    }

    override fun disable() {
        player.velocity = velocity

        fakePlayer?.let {
            player.updatePositionAndAngles(it.x, it.y, it.z, player.yaw, player.pitch)
            world.removeEntity(it.id, Entity.RemovalReason.DISCARDED)
        }

        fakePlayer = null
    }

    val repeatable = repeatable {
        // Just to make sure it stays enabled
        if (!collision) {
            player.noClip = true
            player.fallDistance = 0f
            player.isOnGround = false
        }

        if (fly) {
            val speed = speed.toDouble()
            player.strafe(speed = speed)

            player.velocity.y = when {
                mc.options.jumpKey.isPressed -> speed
                mc.options.sneakKey.isPressed -> -speed
                else -> 0.0
            }
        }
    }

    val packetHandler = handler<PacketEvent> { event ->
        val clone = fakePlayer ?: return@handler

        when (val packet = event.packet) {
            // For better FreeCam detecting AntiCheats, we need to prove to them that the player's moving
            is PlayerMoveC2SPacket -> {
                chat(packet.toString())
                if (packet.changePosition) {
                    packet.x = pos.x
                    packet.y = pos.y
                    packet.z = pos.z
                }
                if (packet.onGround != ground) {
                    packet.onGround = ground
                }
                if (packet.changeLook) {
                    packet.yaw = player.yaw
                    packet.pitch = player.pitch
                }
            }
            is PlayerActionC2SPacket -> event.cancelEvent()
            // In case of a teleport
            is PlayerPositionLookS2CPacket -> {
                clone.updatePosition(packet.x, packet.y, packet.z)
                pos = Vec3d(packet.x, packet.y, packet.z)
                // Reset the motion
                event.cancelEvent()
            }
        }
    }
}
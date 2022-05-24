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
import net.ccbluex.liquidbounce.utils.entity.strafe
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket

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
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0
    private var posX = 0.0
    private var posY = 0.0
    private var posZ = 0.0
    private var ground = false

    override fun enable() {
        if (resetMotion) {
            player.setVelocity(0.0, 0.0, 0.0)
        }
        x = 0.0
        y = 0.0
        z = 0.0
        posX = player.x
        posY = player.y
        posZ = player.z
        ground = player.isOnGround
        if (!collision) {
            player.noClip = true
        }

        fakePlayer = createClone() ?: return
        world.addEntity(fakePlayer!!.id, fakePlayer)
    }

    override fun disable() {
        player.setVelocity(x, y, z)

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
        if (fakePlayer == null) {
            return@handler
        }

        when (val packet = event.packet) {
            // For better FreeCam detecting AntiCheats, we need to prove to them that the player's moving
            is PlayerMoveC2SPacket -> {
                if (packet.changePosition) {
                    packet.x = posX
                    packet.y = posY
                    packet.z = posZ
                }
                packet.onGround = ground
                if (packet.changeLook) {
                    packet.yaw = player.yaw
                    packet.pitch = player.pitch
                }
            }
            is PlayerActionC2SPacket -> event.cancelEvent()
            // In case of a teleport
            is PlayerPositionLookS2CPacket -> {
                fakePlayer?.updatePosition(packet.x, packet.y, packet.z)
                // Reset the motion
                event.cancelEvent()
            }
        }
    }
}
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
import net.ccbluex.liquidbounce.utils.block.canStandOn
import net.ccbluex.liquidbounce.utils.entity.strafe
import net.ccbluex.liquidbounce.utils.math.times
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.minecraft.util.math.Direction
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
    private val visuals by boolean("Visuals", false)

    private var fakePlayer: OtherClientPlayerEntity? = null
    private var velocity = Vec3d.ZERO
    private var pos = Vec3d.ZERO
    private var ground = false

    override fun enable() {
        /**
         * Velocity Y must always be set to player's velocity Y, so we don't trigger [PlayerMoveC2SPacket.OnGroundOnly] on [disable]
         */
        velocity = if (resetMotion) {
            player.velocity.times(0.0)
            Vec3d.ZERO
        } else {
            player.velocity
        }.withAxis(Direction.Axis.Y, player.velocity.y)

        pos = player.pos
        ground = player.isOnGround

        fakePlayer = createClone(player, world).also {
            world.addEntity(it.id, it)
        }
    }

    override fun disable() {
        player.velocity = velocity
        player.updatePosition(pos.x, pos.y, pos.z)

        fakePlayer?.let {
            world.removeEntity(it.id, Entity.RemovalReason.DISCARDED)
        }

        /**
         * Prevents the client from sending [PlayerMoveC2SPacket.OnGroundOnly], so it's not detected for ground spoof.
         */
        player.isOnGround = ground
        player.lastOnGround = ground

        fakePlayer = null
    }

    val repeatable = repeatable {
        fakePlayer?.let {
            if (!ground && it.blockPos.add(0.0, -0.01, 0.0).canStandOn()) {
                ground = true
                network.connection.send(PlayerMoveC2SPacket.OnGroundOnly(true))
            }
        }

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
        when (val packet = event.packet) {
            /**
             * For 1:1 simulation detecting AntiCheats such as Grim, Karhu, Intave or FreeCam detecting AntiCheats like BAC we must somewhat simulate a player who doesn't move but rotates
             * and sends [PlayerMoveC2SPacket.PositionAndOnGround] packets every 20 ticks, just like an AFK player, with the help of the [applyOnlyTicksSinceLastPosition] function.
             * @see net.minecraft.client.network.ClientPlayerEntity.sendMovementPackets, bl3 variable
             */
            is PlayerMoveC2SPacket -> {
                if (packet.changesPosition()) {
                    packet.x = pos.x
                    packet.y = pos.y
                    packet.z = pos.z
                }

                packet.onGround = ground

                if (packet.changesLook()) {
                    val yaw = player.yaw
                    val pitch = player.pitch

                    if (visuals) {
                        fakePlayer?.let {
                            it.yaw = yaw
                            it.headYaw = yaw
                            it.pitch = pitch
                        }
                    }

                    packet.yaw = yaw
                    packet.pitch = pitch
                }
            }
            is PlayerPositionLookS2CPacket -> {
                fakePlayer?.updatePosition(packet.x, packet.y, packet.z)

                network.connection.send(TeleportConfirmC2SPacket(packet.teleportId))
                network.connection.send(
                    PlayerMoveC2SPacket.Full(
                        packet.x, packet.y, packet.z, packet.yaw, packet.pitch, false
                    )
                )

                pos = Vec3d(packet.x, packet.y, packet.z)
                ground = false

                event.cancelEvent()
            }
            is ClientCommandC2SPacket -> {
                val allowedPackets = arrayOf(
                    ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY,
                    ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY,
                    ClientCommandC2SPacket.Mode.OPEN_INVENTORY
                )

                if (packet.mode !in allowedPackets) {
                    event.cancelEvent()
                }
            }
            is PlayerActionC2SPacket -> event.cancelEvent()
        }
    }

    fun applyOnlyTicksSinceLastPosition(original: Boolean): Boolean {
        val player = mc.player ?: return original

        if (!enabled) {
            return original
        }

        return player.ticksSinceLastPositionPacketSent >= 20
    }

    fun preventSendingOnGroundPacket(original: Boolean): Boolean {
        val player = mc.player ?: return original

        if (!enabled) {
            return original
        }

        return player.lastOnGround
    }

}
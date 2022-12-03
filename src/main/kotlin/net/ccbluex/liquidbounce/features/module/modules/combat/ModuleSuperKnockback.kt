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

package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.sequenceHandler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.util.InputUtil
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket

/**
 * SuperKnockback module
 *
 * Increases knockback dealt to other entities.
 */
object ModuleSuperKnockback : Module("SuperKnockback", Category.COMBAT) {

    val pressingSprintKey: Boolean
        get() = InputUtil.isKeyPressed(mc.window.handle, mc.options.sprintKey.boundKey.code)

    val attackHandler = handler<AttackEvent> {
        if (player.isSprinting && pressingSprintKey) {
            mc.options.sprintKey.isPressed = false
        }
    }

    val postAttackHandler = sequenceHandler<PacketEvent> {
        // type.type typical mojang
        if (it.packet !is PlayerInteractEntityC2SPacket || it.packet.type.type != PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
            return@sequenceHandler
        }

        waitUntil { !player.isSprinting && player.isSprinting == player.lastSprinting }

        if (!mc.options.sprintKey.isPressed && pressingSprintKey) {
            mc.options.sprintKey.isPressed = true
        }
    }

}
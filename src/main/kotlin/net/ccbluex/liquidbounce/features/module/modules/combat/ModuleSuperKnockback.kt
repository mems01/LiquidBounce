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

import net.ccbluex.liquidbounce.event.PostAttackEvent
import net.ccbluex.liquidbounce.event.sequenceHandler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.option.KeyBinding

/**
 * SuperKnockback module
 *
 * Increases knockback dealt to other entities.
 */
object ModuleSuperKnockback : Module("SuperKnockback", Category.COMBAT) {

    val attackHandler = sequenceHandler<PostAttackEvent> { event ->
        // If player was not sprinting, then why bother?
        if (!player.lastSprinting) {
            return@sequenceHandler
        }

        // Wait until player's sprint state is updated server-side
        waitUntil { player.isSprinting == player.lastSprinting }

        // This gets called if player either hit an animal/mob or the sprint button was pressed
        if (player.isSprinting) {
            mc.options.sprintKey.isPressed = false

            player.isSprinting = false

            waitUntil { player.isSprinting == player.lastSprinting }
        }
        // If player was not holding the sprint button but was sprinting, then start sprinting
        mc.options.sprintKey.isPressed = true

        // Wait until player is sprinting and ticks since sprint is more than 1 to avoid constant re-sprinting
        waitUntil { player.isSprinting && player.ticksSinceSprintingChanged > 1 }

        KeyBinding.updatePressedStates()
    }


}
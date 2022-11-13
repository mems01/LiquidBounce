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
        val wasSprinting = player.lastSprinting

        if (!wasSprinting) {
            return@sequenceHandler
        }

        waitUntil { player.isSprinting == player.lastSprinting }

        val ticks = if (player.isSprinting) 1 else 0

        if (player.isSprinting) {
            mc.options.sprintKey.isPressed = false

            player.isSprinting = false

            waitUntil { player.isSprinting == player.lastSprinting }
        }
        
        mc.options.sprintKey.isPressed = true

        waitUntil { player.isSprinting && player.ticksSinceSprintingChanged > ticks }

        KeyBinding.updatePressedStates()
    }


}
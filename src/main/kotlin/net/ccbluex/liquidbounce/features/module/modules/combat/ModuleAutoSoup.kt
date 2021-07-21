/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2016 - 2021 CCBlueX
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

import net.ccbluex.liquidbounce.config.NamedChoice
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.item.convertClientSlotToServerSlot
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.*
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object ModuleAutoSoup : Module("AutoSoup", Category.COMBAT) {


    private val bowl by enumChoice("Bowl", BowlMode.DROP, BowlMode.values())
    val health by int("Health", 18, 1..20)

    val repeatable = repeatable {
        val hotBarSlot = (0..8).firstOrNull {
            player.inventory.getStack(it).item == Items.MUSHROOM_STEW
        }

        val invSlot = (0..40).find {
            !player.inventory.isEmpty && player.inventory.getStack(it).item == Items.MUSHROOM_STEW
        }

        if (hotBarSlot == null && invSlot == null) {
            return@repeatable
        }

        if (player.isDead) {
            return@repeatable
        }

        if (player.health < health) {
            if (hotBarSlot != null) {
                if (hotBarSlot != player.inventory.selectedSlot) {
                    network.sendPacket(UpdateSelectedSlotC2SPacket(hotBarSlot))
                }
                network.sendPacket(PlayerInteractItemC2SPacket(Hand.MAIN_HAND))
                if (player.inventory.getStack(hotBarSlot).item == Items.BOWL) {
                    // If the user chose the Drop mode
                    if (bowl == BowlMode.DROP) {
                        network.sendPacket(
                            PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.DROP_ITEM,
                                BlockPos.ORIGIN,
                                Direction.DOWN
                            )
                        )
                    } else {
                        // If the user chose the Move mode
                        utilizeInventory(hotBarSlot, 0, SlotActionType.QUICK_MOVE)
                    }
                }
                if (hotBarSlot != player.inventory.selectedSlot) {
                    network.sendPacket(UpdateSelectedSlotC2SPacket(player.inventory.selectedSlot))
                }
            } else {
                // Drag the item from the inventory slot to the hotbar slot
                utilizeInventory(invSlot!!, 0, SlotActionType.QUICK_MOVE)
                return@repeatable
            }
        }
    }

    fun utilizeInventory(slot: Int, button: Int, slotActionType: SlotActionType) {
        val serverSlot = convertClientSlotToServerSlot(slot)

        val openInventory = mc.currentScreen !is InventoryScreen

        if (openInventory) {
            network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.OPEN_INVENTORY))
        }

        interaction.clickSlot(0, serverSlot, button, slotActionType, player)

        if (openInventory) {
            network.sendPacket(CloseHandledScreenC2SPacket(0))
        }
    }

    private enum class BowlMode(override val choiceName: String) : NamedChoice {
        DROP("Drop"), MOVE("Move")
    }
}

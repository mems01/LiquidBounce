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
import net.ccbluex.liquidbounce.utils.entity.moving
import net.ccbluex.liquidbounce.utils.item.InventoryConstraintsConfigurable
import net.ccbluex.liquidbounce.utils.item.convertClientSlotToServerSlot
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Hand

object ModuleAutoSoup : Module("AutoSoup", Category.COMBAT) {

    private val bowl by enumChoice("Bowl", BowlMode.DROP, BowlMode.values())
    val health by int("Health", 18, 1..20)
    val b by boolean("b", false)
    val a by boolean("a", false)
    val inventoryConstraints = InventoryConstraintsConfigurable()

    init {
        tree(inventoryConstraints)
    }

    val repeatable = repeatable {
        val hotBarSlot = (0..8).find {
            player.inventory.getStack(it).item == Items.MUSHROOM_STEW
        }

        val invSlot = (0..40).find {
            player.inventory.getStack(it).item == Items.MUSHROOM_STEW
        }

        val bowlSlot = (0..8).find {
            player.inventory.getStack(it).item == Items.BOWL
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

                if (bowlSlot != null) {
                    when (bowl) {
                        BowlMode.DROP -> {
                            utilizeInventory(bowlSlot, 1, SlotActionType.THROW, true)
                        }
                        BowlMode.MOVE -> {
                            utilizeInventory(bowlSlot, 0, SlotActionType.PICKUP_ALL, false)
                            if (b) if (invSlot == null) interaction.clickSlot(0, bowlSlot, 0, SlotActionType.PICKUP, player)
                            if (a) network.sendPacket(CloseHandledScreenC2SPacket(0))
                        }
                    }
                }

                if (hotBarSlot != player.inventory.selectedSlot) {
                    network.sendPacket(UpdateSelectedSlotC2SPacket(player.inventory.selectedSlot))
                }

                wait(inventoryConstraints.delay.random())
                return@repeatable
            } else {
                // Search for the specific item in inventory and quick move it to hotbar
                if (invSlot != null) {
                    utilizeInventory(invSlot, 0, SlotActionType.QUICK_MOVE, true)
                }
                return@repeatable
            }
        }
    }

    private fun utilizeInventory(slot: Int, button: Int, slotActionType: SlotActionType, close: Boolean) {
        val serverSlot = convertClientSlotToServerSlot(slot)
        val isInInventoryScreen = mc.currentScreen is InventoryScreen

        if (!(inventoryConstraints.noMove && player.moving) && (!inventoryConstraints.invOpen || isInInventoryScreen)) {
            val openInventory = inventoryConstraints.simulateInventory && !isInInventoryScreen

            if (openInventory) {
                network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.OPEN_INVENTORY))
            }

            interaction.clickSlot(0, serverSlot, button, slotActionType, player)

            if (close) {
                if (openInventory) {
                    network.sendPacket(CloseHandledScreenC2SPacket(0))
                }
            }
        }
    }

    private enum class BowlMode(override val choiceName: String) : NamedChoice {
        DROP("Drop"), MOVE("Move")
    }
}

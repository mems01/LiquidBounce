package net.ccbluex.liquidbounce.features.module.modules.combat

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
                network.sendPacket(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.DROP_ITEM, BlockPos.ORIGIN, Direction.DOWN))

                if (hotBarSlot != player.inventory.selectedSlot) {
                    network.sendPacket(UpdateSelectedSlotC2SPacket(player.inventory.selectedSlot))
                }
            } else {
                val serverSlot = convertClientSlotToServerSlot(invSlot!!)

                val openInventory = mc.currentScreen !is InventoryScreen

                if (openInventory) {
                    network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.OPEN_INVENTORY))
                }

                interaction.clickSlot(0, serverSlot, 0, SlotActionType.QUICK_MOVE, player)

                if (openInventory) {
                    network.sendPacket(CloseHandledScreenC2SPacket(0))
                }

                return@repeatable
            }
        }
    }
}

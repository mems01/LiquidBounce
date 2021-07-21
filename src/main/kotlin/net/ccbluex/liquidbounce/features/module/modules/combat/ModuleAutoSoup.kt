package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.item.convertClientSlotToServerSlot
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.item.Items
import net.minecraft.item.MushroomStewItem
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object ModuleAutoSoup : Module("AutoSoup", Category.COMBAT) {

    val health by int("Health", 18, 1..20)

    var lastSlot = -1

    val repeatable = repeatable {
        val hotBarSlot = (0..8).firstOrNull {
            player.inventory.getStack(it).item is MushroomStewItem
        }

        val invSlot = (0..40).find {
            !player.inventory.isEmpty && player.inventory.getStack(it).item is MushroomStewItem
        }

        if (hotBarSlot == null && invSlot == null) {
            return@repeatable
        }

        if (player.isDead) {
            return@repeatable
        }

        if (player.health < health) {
            if (hotBarSlot != null) {
                lastSlot = player.inventory.selectedSlot
                player.inventory.selectedSlot = hotBarSlot

                network.sendPacket(PlayerInteractItemC2SPacket(Hand.MAIN_HAND))

                if (player.inventory.getStack(hotBarSlot).item !is MushroomStewItem) {
                    network.sendPacket(
                        PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.DROP_ITEM,
                            BlockPos.ORIGIN,
                            Direction.DOWN
                        )
                    )
                }

                player.inventory.selectedSlot = lastSlot
                lastSlot = -1
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

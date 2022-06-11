package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.TransferOrigin
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.entity.boxedDistanceTo
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket

object ModuleTest : net.ccbluex.liquidbounce.features.module.Module("Test", Category.MISC) {
    val packets by boolean("Packets", false)

    val packetHandler = handler<PacketEvent> { event ->
        if (event.origin != TransferOrigin.SEND || !packets) {
            return@handler
        }

        if (event.packet is PlayerMoveC2SPacket || event.packet is PlayPongC2SPacket) {
            return@handler
        }

        val packet = event.packet
        var clazz: Class<*> = packet::class.java

        chat(" ")
        chat(clazz.simpleName)

        if (clazz.isMemberClass) {
            clazz = clazz.declaringClass
        }

        clazz.declaredFields.forEach {
            it.isAccessible = true
            chat(" -> ${it.name} = ${it.get(packet)}")
        }
    }

    val attack = handler<AttackEvent> {
        val entity = it.enemy

        player.yaw = 180f
        player.pitch = 0f

        chat(entity.boxedDistanceTo(player).toString().plus(", SYSTEM MS: ${System.currentTimeMillis()}"))
        chat(entity.distanceTo(player).toString().plus((0.1+0.2).toString()))
    }
}
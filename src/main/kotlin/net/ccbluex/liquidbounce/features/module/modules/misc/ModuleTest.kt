package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket

object ModuleTest : net.ccbluex.liquidbounce.features.module.Module("Test", Category.MISC) {
    val packets by boolean("Packets", false)
    val packetName by text("Name For Packet", "Any")

    val packetHandler = handler<PacketEvent> { event ->
        if (!packets) {
            return@handler
        }

        if (packetName != "Any" && !event.packet.javaClass.simpleName.lowercase()
                .contains(packetName.lowercase()) || event.packet !is PlayerMoveC2SPacket.Full || event.packet is PlayPongC2SPacket
        ) {
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
}
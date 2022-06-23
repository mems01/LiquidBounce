package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.TransferOrigin
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket

object ModuleTest : net.ccbluex.liquidbounce.features.module.Module("Test", Category.MISC) {
    val packets by boolean("Packets", false)

    val packetHandler = handler<PacketEvent> { event ->
        if (event.origin != TransferOrigin.RECEIVE || !packets) {
            return@handler
        }

        if (event.packet is ChunkDeltaUpdateS2CPacket || event.packet is WorldTimeUpdateS2CPacket || event.packet is ChunkDataS2CPacket || event.packet is UnloadChunkS2CPacket || event.packet is EntityTrackerUpdateS2CPacket || event.packet is LightUpdateS2CPacket || event.packet is CommandSuggestionsS2CPacket || event.packet is ChunkRenderDistanceCenterS2CPacket || event.packet is BlockUpdateS2CPacket) {
            event.cancelEvent()
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
/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.spartan

import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

object Spartan : FlyMode("Spartan") {
	private val timer = TickTimer()

	override fun onEnable() {
		timer.reset()
	}

	override fun onUpdate() {
		mc.player.velocityY = 0.0

		timer.update()
		if (timer.hasTimePassed(12)) {
			sendPackets(
				C04PacketPlayerPosition(mc.player.posX, mc.player.posY + 8, mc.player.posZ, true),
				C04PacketPlayerPosition(mc.player.posX, mc.player.posY - 8, mc.player.posZ, true)
			)
			timer.reset()
		}
	}
}

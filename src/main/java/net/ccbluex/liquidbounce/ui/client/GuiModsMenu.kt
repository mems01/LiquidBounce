/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce.clientRichPresence
import net.ccbluex.liquidbounce.lang.translationMenu
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.minecraft.client.gui.ButtonWidget
import net.minecraft.client.gui.screen.Screen
import net.minecraftforge.fml.client.GuiModList
import org.lwjgl.input.Keyboard
import kotlin.concurrent.thread

class GuiModsMenu(private val prevGui: Screen) : Screen() {

    override fun initGui() {
        buttonList.run {
            add(ButtonWidget(0, width / 2 - 100, height / 4 + 48, "Forge Mods"))
            add(ButtonWidget(1, width / 2 - 100, height / 4 + 48 + 25, "Scripts"))
            add(ButtonWidget(2, width / 2 - 100, height / 4 + 48 + 50, "Rich Presence: ${if (clientRichPresence.showRichPresenceValue) "§aON" else "§cOFF"}"))
            add(ButtonWidget(3, width / 2 - 100, height / 4 + 48 + 75, "Back"))
        }
    }

    override fun actionPerformed(button: ButtonWidget) {
        when (val id = button.id) {
            0 -> mc.setScreen(GuiModList(this))
            1 -> mc.setScreen(GuiScripts(this))
            2 -> {
                val rpc = clientRichPresence
                rpc.showRichPresenceValue = when (val state = !rpc.showRichPresenceValue) {
                    false -> {
                        rpc.shutdown()
                        changeDisplayState(id, state)
                        false
                    }
                    true -> {
                        var value = true
                        thread {
                            value = try {
                                rpc.setup()
                                true
                            } catch (throwable: Throwable) {
                                LOGGER.error("Failed to setup Discord RPC.", throwable)
                                false
                            }
                        }
                        changeDisplayState(id, value)
                        value
                    }
                }
            }
            3 -> mc.setScreen(prevGui)
        }
    }

    private fun changeDisplayState(buttonId: Int, state: Boolean) {
        val button = buttonList[buttonId]
        val displayName = button.displayString
        button.displayString = when (state) {
            false -> displayName.replace("§aON", "§cOFF")
            true -> displayName.replace("§cOFF", "§aON")
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        Fonts.fontBold180.drawCenteredString(translationMenu("mods"), width / 2F, height / 8F + 5F, 4673984, true)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            mc.setScreen(prevGui)
            return
        }

        super.keyTyped(typedChar, keyCode)
    }
}
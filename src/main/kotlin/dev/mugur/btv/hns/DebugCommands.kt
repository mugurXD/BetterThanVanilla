package dev.mugur.btv.hns

import dev.mugur.btv.Main
import dev.mugur.btv.utils.ChatCommand
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class DebugCommands {
    companion object {
        val playerHidingKey = NamespacedKey(Main.Companion.instance!!, "player-hiding")

        fun toggleHide(): ChatCommand {
            return ChatCommand("debug_hide")
                .requirePlayerSender()
                .requireOp()
                .executes { ctx ->
                    val player = ctx.source.sender as Player
                    val pdc = player.persistentDataContainer
                    val wasHiding =
                        if(pdc.has(playerHidingKey))
                            pdc.get(playerHidingKey, PersistentDataType.BOOLEAN)!!
                        else
                            false
                    val isHiding = !wasHiding

                    if(isHiding) HiderController.instance.enableHidingEffect(player)
                    else HiderController.instance.removeHidingEffect(player)

                    pdc.set(playerHidingKey, PersistentDataType.BOOLEAN, isHiding)
                    return@executes 1
                }
        }
    }
}
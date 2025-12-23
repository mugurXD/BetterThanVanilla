package dev.mugur.btv.minigames

import dev.mugur.btv.Main
import dev.mugur.btv.utils.ChatCommand
import dev.mugur.btv.utils.ChatHelper
import org.bukkit.entity.Player

class MinigameJoinCommands {
    companion object {
        fun joinCommand(): ChatCommand {
            return ChatCommand("join")
                .requirePlayerSender()
                .executes { ctx ->
                    val player = ctx.source.sender as Player

                    val pluginManager = Main.instance!!.server.pluginManager
                    val mg = TestMinigame(player.uniqueId)
                    pluginManager.registerEvents(mg, Main.instance!!)
                    mg.participants.add(player.uniqueId)
                    mg.beginStartCountdown()
                    Minigame.running = mg

                    return@executes 1
                }
        }
    }
}
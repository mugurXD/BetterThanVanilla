package dev.mugur.btv.hns

import dev.mugur.btv.Main
import dev.mugur.btv.minigames.Minigame
import dev.mugur.btv.utils.ChatCommand
import org.bukkit.entity.Player

class HnsCommand {
    companion object {
        fun command(): ChatCommand {
            return ChatCommand("hns")
                .requirePlayerSender()
                .executes { ctx ->
                    val player = ctx.source.sender as Player
                    if(Minigame.running == null)
                    {
                        val mg = HideAndSeek(player.uniqueId)
                        val pluginManager = Main.instance!!.server.pluginManager
                        pluginManager.registerEvents(mg, Main.instance!!)
                        Minigame.running = mg
                    }
                    else if(Minigame.running !is HideAndSeek)
                    {
                        return@executes 1
                    }

                    val mg = Minigame.running as HideAndSeek
                    mg.participants.add(player.uniqueId)

                    if(mg.shouldBegin())
                        mg.beginStartCountdown()

                    return@executes 1
                }
        }
    }
}
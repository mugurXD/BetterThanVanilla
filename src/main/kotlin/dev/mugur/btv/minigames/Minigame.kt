package dev.mugur.btv.minigames

import dev.mugur.btv.Main
import dev.mugur.btv.minigames.events.MinigameFinishEvent
import dev.mugur.btv.minigames.events.MinigamePlayerEliminatedEvent
import dev.mugur.btv.minigames.events.MinigameStartEvent
import dev.mugur.btv.utils.ChatHelper
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

abstract class Minigame(val initiator: UUID) : Listener {
    abstract fun getDisplayName(): String
    abstract fun getMinimumParticipants(): Int

    val participants = mutableListOf<UUID>()
    var state = MinigameState.WAITING
        private set

    open fun beginStartCountdown() {
        var step = 3
        Bukkit.getScheduler().runTaskTimer(Main.instance!!, Runnable {
            if(step > 0) {
                val title = ChatHelper.getMessageWithCustomTag("minigame.start_countdown", "title", getDisplayName(), step)
                val subtitle = ChatHelper.getMessageWithCustomTag("minigame.start_countdown", "subtitle")
                broadcastTitle(title, subtitle)

                participants.forEach {
                    val player = Bukkit.getPlayer(it)!!
                    player.playSound(Sound.sound(Key.key("block.note_block.pling"), Source.MASTER, 1f, 1f))
                }
            } else {
                if(!shouldBegin())
                    return@Runnable

                participants.forEach {
                    val player = Bukkit.getPlayer(it)!!
                    player.playSound(Sound.sound(Key.key("item.goat_horn.sound.1"), Source.MASTER, 1f, 1f))
                }

                start()
            }

            step--
        }, 0L, 20L)
    }

    open fun start() {
        state = MinigameState.STARTED

        PlayerStateRestorer.instance.savePlayerStates(participants.mapNotNull { Bukkit.getPlayer(it) })

        val startedEvent = MinigameStartEvent(this)
        startedEvent.callEvent()
    }

    open fun finish() {
        val winnerId = determineWinner()
        val winner = Bukkit.getPlayer(winnerId)

        val finishedEvent = MinigameFinishEvent(this, winner)
        finishedEvent.callEvent()

        if(winner == null) {
            ChatHelper.broadcastTitle("minigame.finished.no_winner")
            Bukkit.getOnlinePlayers().forEach { it.playSound(Sound.sound(Key.key("item.goat_horn.sound.7"), Source.MASTER, 1f, 1f)) }
        }
        else {
            ChatHelper.broadcastTitle("minigame.finished.winner", winner.name)
            Bukkit.getOnlinePlayers().forEach { it.playSound(Sound.sound(Key.key("ui.toast.challenge_complete"), Source.MASTER, 1f, 1f)) }
        }

        participants.forEach { PlayerStateRestorer.instance.restorePlayerState(it) }
        participants.clear()
        state = MinigameState.ENDED
    }

    open fun determineWinner(): UUID {
        if(participants.size == 1)
            return participants[0]
        else
            return UUID(0, 0)
    }

    open fun eliminatePlayer(player: Player) {
        val eliminatedEvent = MinigamePlayerEliminatedEvent(this, player)
        eliminatedEvent.callEvent()

        participants.remove(player.uniqueId)

        val eliminatedMessage = ChatHelper.getMessage("minigame.player_eliminated", player.name)
        broadcast(eliminatedMessage)

        ChatHelper.showTitle(player, "minigame.eliminated_title")
        player.playSound(Sound.sound(Key.key("event.mob_effect.raid_omen"), Sound.Source.MASTER, 1f, 1f))
        if(shouldEnd())
            finish()
    }

    fun broadcast(message: Component) {
        for(uuid in participants) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            player.sendMessage(message)
        }
    }

    fun broadcastTitle(title: Component, subtitle: Component) {
        for(uuid in participants) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            player.showTitle(Title.title(title, subtitle))
        }
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) { if(participants.contains(e.player.uniqueId)) eliminatePlayer(e.player) }

    open fun shouldEnd(): Boolean { return state == MinigameState.STARTED && participants.size < getMinimumParticipants() }
    fun shouldBegin(): Boolean { return state == MinigameState.WAITING && participants.size >= getMinimumParticipants() }

    companion object { var running: Minigame? = null }
}

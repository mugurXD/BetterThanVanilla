package dev.mugur.btv.minigames

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

class PlayerStateRestorer : Listener {
    val playerStates = mutableMapOf<UUID, PlayerPreviousState>()

    fun restorePlayerState(playerId: UUID) {
        val player = Bukkit.getPlayer(playerId) ?: return
        val previousState = playerStates[player.uniqueId] ?: return
        val inventory = player.inventory
        inventory.setItemInOffHand(previousState.itemInOffHand)
        inventory.armorContents = previousState.armorContents.toTypedArray()
        inventory.contents = previousState.inventory.toTypedArray()

        player.exp = previousState.exp
        player.teleport(previousState.location)
        player.gameMode = previousState.gameMode
        playerStates.remove(player.uniqueId)
    }

    fun savePlayerState(player: Player) {
        playerStates[player.uniqueId] = PlayerPreviousState(
            location = player.location,
            inventory = player.inventory.contents.filterNotNull(),
            armorContents = player.inventory.armorContents.filterNotNull(),
            itemInOffHand = player.inventory.itemInOffHand,
            exp = player.exp,
            gameMode = player.gameMode
        )

        player.inventory.clear()
        player.exp = 0f
    }

    fun savePlayerStates(players: List<Player>) {
        players.forEach { savePlayerState(it) }
    }

    fun restorePlayerStates() {
        playerStates.forEach { restorePlayerState(it.key) }
    }

    init { instance = this}
    companion object { lateinit var instance: PlayerStateRestorer }

    /* Priority is set to LOWEST in order to ensure it is called last;
    you wouldn't want to restore the player's state and then another event to
    clear their inventory, for example
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val player = e.player
        if(playerStates.contains(player.uniqueId))
            restorePlayerState(player.uniqueId)
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val player = e.player
        if(playerStates.contains(player.uniqueId))
            restorePlayerState(player.uniqueId)
    }
}
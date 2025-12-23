package dev.mugur.btv.hns

import dev.mugur.btv.minigames.Minigame
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SeekerController : Listener {
    @EventHandler
    fun onPlayerHitPlayer(e: PrePlayerAttackEntityEvent) {
        val attacker = e.player
        val attackedPlayer = if(e.attacked is Player) e.attacked as Player else return
        if(Minigame.running !is HideAndSeek) return

        val hns = Minigame.running as HideAndSeek
        /* Make sure the attacker is the seeker and the attacked player is a participant */
        if(attacker.uniqueId != hns.seekerId) return
        if(!hns.participants.contains(attackedPlayer.uniqueId)) return

        attackedPlayer.health -= 10
    }

}
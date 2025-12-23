package dev.mugur.btv.hns

import dev.mugur.btv.minigames.Minigame
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

class HideAndSeek(override val initiator: UUID) : Minigame(initiator) {
    override fun getDisplayName(): String = "Hide and Seek"
    override fun getMinimumParticipants(): Int = 2

    lateinit var seekerId: UUID
        private set

    fun getHiders(): List<Player> {
        return participants
            .filterNot { it == seekerId }
            .mapNotNull { Bukkit.getPlayer(it) }
    }

    override fun start() {
        super.start()

        seekerId = participants.random()

        val seeker = Bukkit.getPlayer(seekerId)
        seeker?.sendMessage("TU CAUTI")

        participants
            .filterNot { it == seekerId }
            .mapNotNull { Bukkit.getPlayer(it) }
            .forEach {
                it.sendMessage("TU TE ASCUNZI")
                HiderController.instance.enableHidingEffect(it)
            }

    }

    override fun shouldEnd(): Boolean {
        return super.shouldEnd() ||
                Bukkit.getPlayer(seekerId) == null ||
                getHiders().isEmpty()

    }
}
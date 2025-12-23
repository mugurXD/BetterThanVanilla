package dev.mugur.btv.minigames

import java.util.UUID

class TestMinigame(override val initiator: UUID): Minigame(initiator) {
    override fun getDisplayName(): String = "Test Minigame"
    override fun getMinimumParticipants(): Int = 1
}
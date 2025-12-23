package dev.mugur.btv.minigames.events

import dev.mugur.btv.minigames.Minigame
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class MinigameStartEvent(val minigame: Minigame) : Event() {
    companion object {
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        @Override
        @Suppress("ACCIDENTAL_OVERRIDE")
        fun getHandlerList(): HandlerList { return HANDLER_LIST }
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}

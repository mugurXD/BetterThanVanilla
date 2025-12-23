package dev.mugur.btv.minigames

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

data class PlayerPreviousState(
    val location: Location,
    val inventory: List<ItemStack>,
    val armorContents: List<ItemStack>,
    val itemInOffHand: ItemStack?,
    val exp: Float,
    val gameMode: GameMode
)

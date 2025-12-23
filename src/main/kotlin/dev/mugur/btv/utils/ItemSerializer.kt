package dev.mugur.btv.utils

import org.bukkit.inventory.ItemStack
import java.util.Base64

class ItemSerializer {
    companion object {
        fun serializeItems(items: Array<ItemStack?>): List<String> {
            return items.map {
                if(it == null) return@map null
                val bytes = it.serializeAsBytes()
                Base64.getEncoder().encodeToString(bytes)
            }.filterNotNull()
        }

        fun deserializeItems(items: List<String>): Array<ItemStack> {
            return items.map {
                if(it.isEmpty()) return@map null
                val bytes = Base64.getDecoder().decode(it)
                ItemStack.deserializeBytes(bytes)
            }
                .filterNotNull()
                .toTypedArray()
        }
    }
}
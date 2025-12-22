package dev.mugur.btv.graveyard

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.yaml.snakeyaml.Yaml
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Base64
import java.util.UUID

class GraveSerializer {
    companion object {
        fun serializeGrave(plugin: JavaPlugin, id: UUID, inventory: Inventory) {
            /* Serialize items from the inventory first */
            val items = inventory.contents.map {
                if(it == null) return@map null

                val bytes = it.serializeAsBytes()
                Base64.getEncoder().encodeToString(bytes)
            }.filterNotNull()

            val folder = File(plugin.dataFolder, "graves")
            if(!folder.exists()) folder.mkdirs()

            val data = mapOf("items" to items)
            val file = File(folder, "$id.yml")
            val yaml = Yaml()
            FileWriter(file).use { writer -> yaml.dump(data, writer) }
        }

        fun deserializeGrave(file: File): Pair<UUID, Inventory> {
            val yaml = Yaml()
            val data = yaml.load<Map<String, Any>>(file.inputStream())
            val uuid = UUID.fromString(file.nameWithoutExtension)
            val itemsList = data["items"] as List<String>
            val inventory = Bukkit.createInventory(null, 54, "Grave")
            inventory.contents = itemsList.map {
                if(data.isEmpty()) return@map null

                val bytes = Base64.getDecoder().decode(it)
                ItemStack.deserializeBytes(bytes)
            }.toTypedArray()
            return uuid to inventory
        }

        fun loadAllGraves(plugin: JavaPlugin): MutableMap<UUID, Inventory> {
            val folder = File(plugin.dataFolder, "graves")
            if(!folder.exists()) folder.mkdirs()

            val inventories = mutableMapOf<UUID, Inventory>()
            folder.listFiles()?.forEach { file ->
                deserializeGrave(file).let { (uuid, inv) -> inventories[uuid] = inv }
            }
            return inventories
        }
    }
}
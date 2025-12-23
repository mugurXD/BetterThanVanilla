package dev.mugur.btv.hns

import dev.mugur.btv.Main
import dev.mugur.btv.utils.ChatHelper
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.UUID

class HiderController : Listener {
    val hiders = mutableMapOf<UUID, HiderData>()
    val BLOCK_CHANGE_COOLDOWN = 4_000L
    val FIREWORK_LAUNCH_SECONDS = 10

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val player = e.player
        val hider = hiders[player.uniqueId] ?: return
        val clickedBlock = e.clickedBlock ?: return

        /*
            Cancel the event early: don't allow the user to break/place blocks
            if he is currently hiding
         */
        e.isCancelled = true

        if(e.action != Action.RIGHT_CLICK_BLOCK && e.action != Action.LEFT_CLICK_BLOCK) return

        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - hider.lastBlockChange

        // Apparently onPlayerInteract() is called each tick the button is held so I added a 200ms lower bound so it doesn't spam you
        if(deltaTime in 201..<BLOCK_CHANGE_COOLDOWN) {
            ChatHelper.sendActionBar(player, "hns.hide.switch_block_cooldown", (BLOCK_CHANGE_COOLDOWN - deltaTime) / 1000f)
            return
        }

        hider.blockEntity.block = clickedBlock.blockData
        hider.lastBlockChange = currentTime

        ChatHelper.sendActionBar(player, "hns.hide.block_switched")
        player.playSound(Sound.sound(Key.key("item.trident.riptide_1"), Sound.Source.MASTER, 1f, 1f))
    }

    fun startBlockFollowing(player: Player) {
        Bukkit.getScheduler().runTaskTimer(Main.instance!!, Runnable {
            val data = hiders[player.uniqueId] ?: return@Runnable
            val display = data.blockEntity

            if(!player.isOnline || display.isDead) {
                display.remove()
                hiders.remove(player.uniqueId)
                return@Runnable
            }

            val loc = player.location.clone()
            loc.yaw = 0f
            loc.pitch = 0f

            display.teleport(loc)
        }, 0L, 1L)
    }

    fun startFireworkTimer(player: Player) {
        Bukkit.getScheduler().runTaskTimer(Main.instance!!, Runnable {
            if(!player.isOnline) return@Runnable

            val loc = player.location
            val firework = player.world.spawn(loc, Firework::class.java)
            val meta = firework.fireworkMeta
            meta.addEffect(
                FireworkEffect.builder()
                    .withColor(Color.RED, Color.YELLOW)
                    .with(FireworkEffect.Type.BALL)
                    .flicker(true)
                    .trail(true)
                    .build()
            )
            meta.power = 1
            firework.fireworkMeta = meta
        }, 0L, FIREWORK_LAUNCH_SECONDS * 20L)
    }

    fun removeHidingEffect(player: Player) {
        hiders[player.uniqueId]?.blockEntity?.remove()
        hiders.remove(player.uniqueId)
        player.removePotionEffect(PotionEffectType.INVISIBILITY)
    }

    fun enableHidingEffect(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 255))
        player.world.spawn(player.location, BlockDisplay::class.java) { entity ->
            entity.block = Material.GRASS_BLOCK.createBlockData()
            entity.transformation = Transformation(
                Vector3f(-0.5f, 0f, -0.5f),
                AxisAngle4f(0f, 0f, 0f, 1f),
                Vector3f(1f, 1f, 1f),
                AxisAngle4f(0f, 0f, 0f, 1f)
            )
            entity.teleportDuration = 1
            entity.viewRange = 1f
            entity.shadowRadius = 0f
            entity.shadowStrength = 0f

            hiders[player.uniqueId] = HiderData(entity, System.currentTimeMillis())
        }
        startBlockFollowing(player)
        startFireworkTimer(player)
    }

    init { instance = this }

    companion object {
        lateinit var instance: HiderController
    }
}
package com.khillynn;

import net.minecraft.server.v1_8_R3.EntityArrow;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class Gadgets implements Listener{
    int taskId;
    boolean hasGrapple = false;

    @EventHandler
    public void playerClickGadget(PlayerInteractEvent e){
        final Player player = e.getPlayer();

        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack gadget = player.getItemInHand();

            if(gadget.getType() == Material.STICK){
                int maxDist = 100;
                final Block block = player.getTargetBlock((HashSet<Byte>) null, maxDist);
                if(block.getType() != Material.AIR && hasGrapple == false) {
                    launchGrapple(player, block.getLocation());
                }
            }
        }
    }

    public void launchGrapple(final Player player, final Location loc){
        Bat bat = (Bat) Bukkit.getWorld("hub").spawnEntity(loc, EntityType.BAT);
        bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1));
        WorldServer world =  ((CraftWorld)(bat.getWorld())).getHandle();
        final EntityArrow arrow = new EntityArrow(world);
        BukkitScheduler scheduler = Bukkit.getScheduler();
        hasGrapple = true;

        arrow.getBukkitEntity().setPassenger(bat);

        bat.setLeashHolder(player);

        scheduler.scheduleSyncDelayedTask((Plugin) HubServ.getPlugin(), new Runnable() {
            public void run() {
                pullTo(player, loc);
            }
        }, 10L);

        taskId = scheduler.scheduleSyncRepeatingTask((Plugin) HubServ.getPlugin(), new Runnable() {
            public void run() {
                if (player.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR) {
                    try {
                        Bukkit.getScheduler().cancelTask(taskId);
                        removeGrapple(arrow);
                    }catch (Exception ex){
                        System.out.println(ex);
                    }
                }
            }
        }, 20l, 10L);

    }

    public void removeGrapple(EntityArrow arrow){
        if(arrow != null) {
            hasGrapple = false;
            arrow.getBukkitEntity().getPassenger().remove();
            arrow.getBukkitEntity().remove();
        }

    }

    public static void pullTo(Entity e, Location loc) {
        // This code written by [USER=90696604]SnowGears[/USER]
        Location l = e.getLocation();

        if (l.distanceSquared(loc) < 9) {
            if (loc.getY() > l.getY()) {
                e.setVelocity(new Vector(0, 0.25, 0));
                return;
            }
            Vector v = loc.toVector().subtract(l.toVector());
            e.setVelocity(v);
            return;
        }

        l.setY(l.getY() + 0.5);
        e.teleport(l);

        double d = loc.distance(l);
        double g = -0.08;
        double x = (1.0 + 0.07 * d) * (loc.getX() - l.getX()) / d;
        double y = (1.0 + 0.03 * d) * (loc.getY() - l.getY()) / d - 0.5 * g * d;
        double z = (1.0 + 0.07 * d) * (loc.getZ() - l.getZ()) / d;

        Vector v = e.getVelocity();
        v.setX(x);
        v.setY(y);
        v.setZ(z);
        e.setVelocity(v);
    }
}

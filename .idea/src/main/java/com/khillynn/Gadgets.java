package com.khillynn;

import net.minecraft.server.v1_8_R3.EntityArrow;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class Gadgets implements Listener{
    //using a global variable for handling hasGrapple will likely cause problems with multiple users
    int taskId;
    boolean hasGrapple = false;
    int seconds = 0;

    @EventHandler
    public void playerClickGadget(PlayerInteractEvent e){
        final Player player = e.getPlayer();

        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack gadget = player.getItemInHand();

            if(gadget.getType() == Material.STICK){
                final Block block = player.getTargetBlock((HashSet<Byte>) null, 100);
                final Entity entity = targetIsEntity(block);
                if((entity == null)) {
                    if (block.getType() != Material.AIR && !hasGrapple) {
                        launchGrapple(player, block.getLocation(), player);
                    }
                }
                else{
                    launchGrapple(entity, player.getLocation(), player);
                }
            }
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent e){
        if(hasGrapple){
            hasGrapple = false;
        }
    }

    // basically allows for never ending leashes
    @EventHandler
    public void leashBreak(PlayerUnleashEntityEvent e){
        if(e.getReason() == EntityUnleashEvent.UnleashReason.HOLDER_GONE){
            e.setCancelled(true);
        }
    }

    // prevents leash items from spawning
    @EventHandler
    public void itemSpawn(ItemSpawnEvent e){
        if(e.getEntity().getName().toString().equals("item.item.leash")){
            e.setCancelled(true);
        }
    }

    // Used for setting up the variables necessary to grapple to blocks and grapple entities towards the player
    public void launchGrapple(final Entity entity, final Location loc, final Player player){
        BukkitScheduler scheduler = Bukkit.getScheduler();
        hasGrapple = true;
        WorldServer world = ((CraftWorld) (entity.getWorld())).getHandle();
        final EntityArrow arrow = new EntityArrow(world);

        // If the player is grappling onto block or player
        if(entity instanceof Player) {
            // If the player is grappling onto a block and not another player
            if(entity == player) {
                Bat bat = (Bat) Bukkit.getWorld("hub").spawnEntity(loc, EntityType.BAT);
                //bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1));

                arrow.getBukkitEntity().setPassenger(bat);

                bat.setLeashHolder(player);

                scheduler.scheduleSyncDelayedTask((Plugin) HubServ.getPlugin(), new Runnable() {
                    public void run() {
                        pullTo(entity, loc);
                    }
                }, 10L);

                taskId = scheduler.scheduleSyncRepeatingTask((Plugin) HubServ.getPlugin(), new Runnable() {
                    public void run() {
                        if (player.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR || !player.isOnline() || seconds > 5) {
                            try {
                                removeGrapple(arrow);
                            } catch (Exception ex) {
                                Bukkit.getScheduler().cancelTask(taskId);
                                System.out.println(ex);
                            }
                        }
                        seconds++;
                    }
                }, 20l, 10L);
            }
            // Else they must be grappling a player
            else{
                pullTo(entity, player.getLocation());
            }
        }
        // If the entity they are grappling is spawnable (Typically this is used for mobs and prevents the following code to run if the entity is an item
        else if(entity.getType().isSpawnable()){
            final LivingEntity entity1 = (LivingEntity) entity;
            arrow.getBukkitEntity().setPassenger(entity);
            entity1.setLeashHolder(player);
            stubbornPull(scheduler, entity, loc);

            taskId = scheduler.scheduleSyncRepeatingTask((Plugin) HubServ.getPlugin(), new Runnable() {
                public void run() {
                    if (entity.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR) {
                        hasGrapple = false;
                        entity1.setLeashHolder(null);
                        arrow.getBukkitEntity().remove();
                    }
                }
            }, 40l, 20L);
        }

        // Else this entity is most likely an item
        else{
            stubbornPull(scheduler, entity, loc);
        }
    }

    // Checks if the player is trying to grapple an entity instead of a block
    public Entity targetIsEntity(Block block){
        for(Entity entity : block.getChunk().getEntities()){
            if(block.getLocation().distance(entity.getLocation()) < 2){
                return entity;
            }
        }
        return null;
    }

    // This won't create a visual grapple but it will still pull the intended entity to the intended location
    public void stubbornPull(BukkitScheduler scheduler, final Entity entity, final Location loc){
        scheduler.scheduleSyncDelayedTask((Plugin) HubServ.getPlugin(), new Runnable() {
            public void run() {
                hasGrapple = false;
                pullTo(entity, loc);
            }
        }, 10L);
    }

    // Used to remove the visual grapple
    public void removeGrapple(EntityArrow arrow){
        if(arrow != null && arrow.getBukkitEntity().getPassenger() != null) {
            hasGrapple = false;
            arrow.getBukkitEntity().getPassenger().remove();
            arrow.getBukkitEntity().remove();
        }

    }

    // This is code used to actually move the entities being grappled/or player grappling onto a block
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

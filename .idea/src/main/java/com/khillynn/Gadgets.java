package com.khillynn;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;

public class Gadgets implements Listener{
    //using a global variable for handling hasGrapple will likely cause problems with multiple users
    static boolean hasGrapple = false;

    @EventHandler
    public void playerClickGadget(PlayerInteractEvent e){
        final Player player = e.getPlayer();

        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack gadget = player.getItemInHand();

            if(gadget.getType() == Material.STICK){
                final Block block = player.getTargetBlock((HashSet<Byte>) null, 100);
                final Entity entity = GadgetsMethods.targetIsEntity(block);
                if((entity == null)) {
                    if (block.getType() != Material.AIR && !hasGrapple) {
                        GadgetsMethods.launchGrapple(player, block.getLocation(), player);
                    }
                }
                else{
                    GadgetsMethods.launchGrapple(entity, player.getLocation(), player);
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

    public static void setGrapple(Boolean grapple){
        hasGrapple = grapple;
    }

    public boolean getGrapple(){
        return hasGrapple;
    }
}

package com.khillynn;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class RankAbilities implements Listener{
    public static String getRank(Player player){
        MongoDB mdb = Core.getMongoDB();
        String rank = (String) mdb.getUser(player).get("rank");

        return rank;
    }

    @EventHandler
    public void playerMovement(PlayerMoveEvent e){
        Player player = e.getPlayer();

        //only allow admins to double jump
        if(!getRank(player).equals("Admin")){
            return;
        }

        if(player.getGameMode() != GameMode.CREATIVE && player.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR && !player.isFlying()){
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void toggleFlight(PlayerToggleFlightEvent e){
        Player player = e.getPlayer();

        if(player.getGameMode() == GameMode.CREATIVE){
            return;
        }

        e.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(1));
    }
}

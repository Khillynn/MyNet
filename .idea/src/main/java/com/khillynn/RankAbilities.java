package com.khillynn;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;

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

        Location from = e.getFrom();
        Location to = e.getTo();

        if(from.getY() > to.getY()){
            return;
        }

        if(from.getX() != to.getX() || from.getZ() != to.getZ()) {

            if (player.getGameMode() != GameMode.CREATIVE && player.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR && !player.isFlying() && player.getAllowFlight() == false) {
                player.setAllowFlight(true);
            }

            //The following acts as a kind of "high step" like horses/endermen have
            /*ArrayList<Block> blocks = new ArrayList<>();
            blocks.add(player.getLocation().getBlock().getRelative(BlockFace.NORTH));
            blocks.add(player.getLocation().getBlock().getRelative(BlockFace.WEST));
            blocks.add(player.getLocation().getBlock().getRelative(BlockFace.SOUTH));
            blocks.add(player.getLocation().getBlock().getRelative(BlockFace.EAST));

            for(Block block : blocks) {*/
            if (whichFaceIsClosest( to) != null) {
                ArrayList<BlockFace> blocks = new ArrayList<>();
                blocks = whichFaceIsClosest(to);
                for(BlockFace blockFace : blocks) {
                    Block block = player.getLocation().getBlock().getRelative(blockFace);
                    Block upBlock = block.getRelative(BlockFace.UP);
                    if ((block.getType().isSolid()) && (!upBlock.getType().isSolid())) {
                        player.setVelocity(player.getVelocity().setY(.4));
                    }
                }
            }
        }

            //blocks.clear();
        //}
    }

    //this determines if the player is on an edge of a block and what blockface is on that edge
    public ArrayList<BlockFace> whichFaceIsClosest(Location to) {
        ArrayList<BlockFace> blockFaces = new ArrayList<>();
        double changeInX = Math.abs(to.getX() - to.getBlockX());
        double changeInZ = Math.abs(to.getZ() - to.getBlockZ());

        if (changeInX > 0.5) {
            if (changeInZ > 0.5) {
                blockFaces.add(BlockFace.EAST);
            } else if (changeInZ < 0.5) {
                blockFaces.add(BlockFace.WEST);
            }
            blockFaces.add(BlockFace.NORTH);
        } else if (changeInX < 0.5) {
            if (changeInZ > 0.5) {
                blockFaces.add(BlockFace.EAST);
            } else if (changeInZ < 0.5) {
                blockFaces.add(BlockFace.WEST);
            }
            blockFaces.add(BlockFace.SOUTH);
        }
        else if(changeInZ > 0.5){
            blockFaces.add(BlockFace.EAST);
        }
        else{
            blockFaces.add(BlockFace.WEST);
        }
        return blockFaces;
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

    @EventHandler
    public void toggleSneak(PlayerToggleSneakEvent e){
        Player player = e.getPlayer();

        if(!getRank(player).equals("Admin")){
            return;
        }

        player.playEffect(player.getLocation(), Effect.EXPLOSION, 2);
        player.setVelocity(new Vector(0, 2, 0));
    }
}

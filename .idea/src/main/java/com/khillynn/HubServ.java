package com.khillynn;

//Main Hub Rules and such will be made here

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

public class HubServ extends JavaPlugin implements Listener, PluginMessageListener{

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("HubServ is Enabled! =D");

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BugeeCord", this);
    }


    @EventHandler
    public void clickInsideInventory(InventoryClickEvent e){
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        if(clickedItem.getType() == Material.DIAMOND && (e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT)){
            player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);
            tpPlayersToGame(player);
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void diamondSelectedInHand(PlayerInteractEvent e){
        Player player = e.getPlayer();

        if(player.getItemInHand().getType() == Material.DIAMOND && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)){
            player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);
            tpPlayersToGame(player);
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();

        if(player.getItemInHand().getType() != Material.DIAMOND){
            player.setItemInHand(new ItemStack(Material.DIAMOND));
        }
    }

    @EventHandler
    public void playerDied (PlayerDeathEvent e){
        e.getDrops().clear();
    }

    @EventHandler
    public void itemDrop (PlayerDropItemEvent e){
        e.setCancelled(true);
    }

    //prevents players from picking up items
    @EventHandler
    public void itemPickUp (PlayerPickupItemEvent e){
        e.setCancelled(true);
    }

    //No entities on the server can be damaged
    @EventHandler
    public void noHurt(EntityDamageEvent e){
        e.setCancelled(true);
    }

    //No one becomes hungry on the sersver
    @EventHandler
    public void noStarve(FoodLevelChangeEvent e){
        e.setCancelled(true);
    }

    private void tpPlayersToGame(Player player) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try{
                out.writeUTF("Connect");
                out.writeUTF("Game");
            } catch(Exception exception){
                exception.printStackTrace();
            }
            player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        player.sendMessage("recieved");
        try{
            String msg = in.readUTF();
            Bukkit.broadcastMessage(msg);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}

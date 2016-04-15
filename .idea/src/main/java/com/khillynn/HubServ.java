package com.khillynn;

//Main Hub Rules and such will be made here

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.*;

import java.io.*;

public class HubServ extends JavaPlugin implements Listener, PluginMessageListener{

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new PlayerLoginEventListener(), this);
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

        setupScoreBoard(player);
    }

    private void setupScoreBoard(Player player) {
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        Scoreboard onJoin = sm.getNewScoreboard();
        Objective o = onJoin.registerNewObjective("dash", "dummy");

        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        //if Hypixel can name his server after himself then so can I =P
        o.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Khillynn_Thyme");

        Score spacer = null;

        Score gameTitle = null;
        Score points = null;

        Score spacer2 = null;

        try{
            MongoDB mdb = new MongoDB(MongoDBD.username, MongoDBD.password, MongoDBD.database, MongoDBD.host, MongoDBD.port);
            spacer = o.getScore(ChatColor.AQUA + "");
            spacer.setScore(4);

            gameTitle = o.getScore(ChatColor.GOLD + "[BoomRoulette]");
            gameTitle.setScore(3);

            points = o.getScore(ChatColor.WHITE + "Points: " + ChatColor.GREEN + mdb.getUserPoints(player));
            points.setScore(2);

            spacer2 = o.getScore(ChatColor.BLACK + "");
            spacer2.setScore(1);

            for (Player all : Bukkit.getOnlinePlayers()) {
                showRankName(all, onJoin);
            }

            player.setScoreboard(onJoin);

            mdb.closeConnection();
        }catch (Exception ex){
            System.out.println(ex);
        }
    }

    public void showRankName(Player player, Scoreboard sb){
        Team t = sb.registerNewTeam(player.getName());

        try {
            MongoDB mdb = new MongoDB(MongoDBD.username, MongoDBD.password, MongoDBD.database, MongoDBD.host, MongoDBD.port);
            String rank = (String) mdb.getUser(player).get("rank");

            if (rank.equals("Admin")) {
                String newName = ChatColor.RED + "[ADMIN] " + player.getName() + ChatColor.RESET;

                player.setDisplayName(newName);
                player.setPlayerListName(newName);
                t.setPrefix(ChatColor.RED + "[ADMIN] ");
                t.addPlayer(player);
            } else if (rank.equals("Guest")) {
                String newName = ChatColor.GRAY + player.getName() + ChatColor.RESET;

                player.setDisplayName(newName);
                player.setPlayerListName(newName);
                t.setPrefix(ChatColor.GRAY + "");
                t.addPlayer(player);
            }

            mdb.closeConnection();
        }catch (Exception ex){
            System.out.println(ex);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        String message = e.getMessage();

        e.setFormat(player.getDisplayName() + ": " + message);
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

    @EventHandler
    public void itemMove(InventoryClickEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void handChange(PlayerItemHeldEvent e){
        e.setCancelled(true);
    }

    //prevents blocks from breaking
    @EventHandler
    public void blockDamaged (BlockDamageEvent e){
        e.setCancelled(true);
    }

   //prevents weather changes
    @EventHandler
    public void weatherChange(WeatherChangeEvent e){
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

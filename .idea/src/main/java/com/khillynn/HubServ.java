package com.khillynn;

//Main Hub Rules and such will be made here

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.io.*;

public class HubServ extends JavaPlugin implements Listener, PluginMessageListener{

    protected static HubServ plugin;
    protected static PluginManager pm;

    public void onEnable() {
        setPlugin(this);
        setPluginManager(Bukkit.getPluginManager());
        registerListeners();

        getLogger().info("HubServ is Enabled! =D");

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
    }

    public void setPlugin(HubServ hs){
        plugin = hs;
    }

    public static HubServ getPlugin(){
        return plugin;
    }

    public void setPluginManager(PluginManager p){
        pm = p;
    }

    public static PluginManager getPluginManager(){
        return pm;
    }

    void registerListeners(){
        getPluginManager().registerEvents(this, this);
        getPluginManager().registerEvents(new PlayerLoginEventListener(), this);
        getPluginManager().registerEvents(new RankAbilities(), this);
        getPluginManager().registerEvents(new Gadgets(), this);
    }

    @EventHandler
    public void clickItem(PlayerInteractEvent e){
        Player player = e.getPlayer();

        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
            ItemStack clickedItem = player.getItemInHand();

            if(clickedItem.getType() == Material.DIAMOND){
                tpPlayersToGame(player);
            }
        }
    }

    @EventHandler
    public void clickInsideInventory(InventoryClickEvent e){
        Player player = (Player) e.getWhoClicked();

        if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT){
            ItemStack clickedItem = e.getCurrentItem();

            if(clickedItem.getType() == Material.DIAMOND) {
                tpPlayersToGame(player);
            }
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();

        if(player.getInventory().getItem(0) == null || player.getInventory().getItem(0).getType() != Material.DIAMOND){
            player.getInventory().setItem(0, new ItemStack(Material.DIAMOND));
        }
        if(player.getInventory().getItem(1) == null || player.getInventory().getItem(1).getType() != Material.STICK){
            player.getInventory().setItem(1, new ItemStack(Material.STICK));
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
            MongoDB mdb = Core.getMongoDB();
            spacer = o.getScore(ChatColor.AQUA + "");
            spacer.setScore(4);

            gameTitle = o.getScore(ChatColor.GOLD + "[BoomRoulette]");
            gameTitle.setScore(3);

            points = o.getScore(ChatColor.WHITE + "Points: " + ChatColor.GREEN + mdb.getUserPoints(player));
            points.setScore(2);

            spacer2 = o.getScore(ChatColor.BLACK + "");
            spacer2.setScore(1);

            for (Player all : Bukkit.getOnlinePlayers()) {
                String rank = getRankName(all);
                showRankName(all, rank, onJoin);
            }
            player.setScoreboard(onJoin);

        }catch (Exception ex){
            System.out.println(ex);
        }
    }

    public String getRankName(Player player) {
        try {
            String rank = RankAbilities.getRank(player);

            if (rank.equals("Admin")) {
                String newName = ChatColor.RED + "[ADMIN] " + player.getName() + ChatColor.RESET;

                player.setDisplayName(newName);
                player.setPlayerListName(newName);
                return ChatColor.RED + "[ADMIN] ";
            } else if (rank.equals("Guest")) {
                String newName = ChatColor.GRAY + player.getName() + ChatColor.RESET;

                player.setDisplayName(newName);
                player.setPlayerListName(newName);
                return ChatColor.GRAY.toString();
            }
            return ChatColor.WHITE.toString();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void showRankName(Player player, String rank, Scoreboard sb){
        Team team = sb.getTeam(rank);
        if(team == null) {
            team = sb.registerNewTeam(rank);
            team.setPrefix(rank);
        }
        team.addPlayer(player);
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
        if(!RankAbilities.getRank(e.getPlayer()).equals("Admin")) {
            e.setCancelled(true);
        }
    }

    //prevents players from picking up items
    @EventHandler
    public void itemPickUp (PlayerPickupItemEvent e){
        if(!RankAbilities.getRank(e.getPlayer()).equals("Admin")){
            e.setCancelled(true);
        }
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

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e){
        for(Block block : e.blockList()){
            float x = (float) -2 + (float) (Math.random() * ((2- -2) + 1));
            float y = (float) -3 + (float) (Math.random() * ((3- -3) + 1));
            float z = (float) -2 + (float) (Math.random() * ((2- -2) + 1));

            FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getType(), block.getData());
            fallingBlock.setDropItem(false);
            fallingBlock.setVelocity(new Vector(x, y, z));
            block.setType(Material.AIR);
        }
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

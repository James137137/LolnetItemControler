/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.LolnetItemControler;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;

/**
 *
 * @author James
 */
public class LolnetItemControler extends JavaPlugin {

    /**
     * @param args the command line arguments
     */
    private static final Logger log = Bukkit.getLogger();
    private static FileConfiguration config;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new myListener(this), this);
        String version = Bukkit.getServer().getPluginManager().getPlugin(this.getName()).getDescription().getVersion();
        saveDefaultConfig();
        config = getConfig();
        log.log(Level.INFO, "{0}: Version: {1} Enabled.", new Object[]{this.getName(), version});
    }

    @Override
    public void onDisable() {
        log.log(Level.INFO, "{0}: disabled", this.getName());
    }

    public static void main(String[] args) {
        // TODO code application logic here
    }

    private static class myListener implements Listener {

        LolnetItemControler plugin;

        public myListener(LolnetItemControler aThis) {
            plugin = aThis;
        }

        @EventHandler
        protected void onPlayerPlaceBlock(BlockPlaceEvent event) {
            if (event.isCancelled()) {
                return;
            }
            Player player = event.getPlayer();
            Block block = event.getBlock();
            if (player == null || block == null) {
                return;
            }
            int itemID = block.getTypeId();
            int meta = (int) block.getData();
            if (hasBypass(player, itemID, meta, "placeByPlayer")) {
                return;
            }
            List<String> gBanList = (List<String>) config.getList("placeByPlayer.Global");
            List<String> banList = (List<String>) config.getList("placeByPlayer." + player.getWorld().getName());
            if (banList == null || banList.isEmpty()) {
                return;
            }
            if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                player.sendMessage(ChatColor.RED + "you can't place with that in this world");
                event.setCancelled(true);
            } else {
                return;
            }
        }
        
        @EventHandler
        protected void onPlayerPlaceBlockOnTop(BlockPlaceEvent event) {
            if (event.isCancelled()) {
                return;
            }
            Player player = event.getPlayer();
            Block topBlock = event.getBlock();
            if (player == null || topBlock == null) {
                return;
            }
            Location location = topBlock.getLocation();
            int newY = location.getBlockY()-1;
            Block bottomBlock = new Location(location.getWorld(), location.getBlockX(), newY, location.getBlockZ()).getBlock();
            if (bottomBlock == null)
            {
                return;
            }
            if (player.isOp())
            {
                return;
            }
            /*if (hasBypass(player, topItemID, topMeta, "placeByPlayerDirectlyBelow")) {
            return;
            }*/
            List<String> gBanList = (List<String>) config.getList("placeByPlayerDirectlyBelow.Global");
            List<String> banList = (List<String>) config.getList("placeByPlayerDirectlyBelow." + player.getWorld().getName());
            if (isBannedTopPlace(gBanList, topBlock, bottomBlock) || isBannedTopPlace(banList, topBlock, bottomBlock)) {
                player.sendMessage(ChatColor.RED + "you can't place that block above of the block below in this world");
                event.setCancelled(true);
                log.info("[LolnetItemControler]: "+ player.getName() + "tried to place item: " + topBlock.getTypeId() + ":" + ((int) topBlock.getData()) + "ontop of " + bottomBlock.getTypeId() + ":" + ((int) bottomBlock.getData()));
            } else 
            {
                bottomBlock = topBlock;
                location = topBlock.getLocation();
                newY = location.getBlockY()+1;
                topBlock = new Location(location.getWorld(), location.getBlockX(), newY, location.getBlockZ()).getBlock();
                if (isBannedTopPlace(gBanList, topBlock, bottomBlock) || isBannedTopPlace(banList, topBlock, bottomBlock)) {
                    player.sendMessage(ChatColor.RED + "you can't place that block below of the block above in this world");
                    event.setCancelled(true);
                    log.info("[LolnetItemControler]: "+ player.getName() + "tried to place item: " + bottomBlock.getTypeId() + ":" + ((int) bottomBlock.getData()) + "below of " + topBlock.getTypeId() + ":" + ((int) topBlock.getData()));
                }
                if (isBannedTopPlace(gBanList,bottomBlock,topBlock) || isBannedTopPlace(banList, bottomBlock,topBlock)) {
                    
                }
            }
        }

        @EventHandler
        public void onPrepareCraftItemEvent(PrepareItemCraftEvent event) {
            Recipe r = event.getRecipe();
            int itemID;
            int meta;
            try {
                itemID = r.getResult().getTypeId();
                meta = (int) r.getResult().getData().getData();
            } catch (Exception e) {
                return;
            }
            String worldName = null;
            Player player = null;
            for (HumanEntity humanEntity : event.getViewers()) {
                worldName = humanEntity.getWorld().getName();
                player = (Player) humanEntity;
                break;
            }
            if (player == null) {
                return;
            }
            if (hasBypass(player, itemID, meta, "craftByPlayer")) {
                return;
            }
            List<String> gBanList = (List<String>) config.getList("craftByPlayer.Global");
            List<String> banList = (List<String>) config.getList("craftByPlayer." + worldName);
            if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                player.sendMessage(ChatColor.RED + "you can't craft with that item in this world");
                event.getInventory().setResult(new ItemStack(Material.AIR));
            } else {
                return;
            }

        }

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.isCancelled() || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            int itemID;
            int meta;
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            itemID = block.getTypeId();
            meta = (int) block.getData();
            if (hasBypass(player, itemID, meta, "playerBlockInteract")) {
                return;
            }

            List<String> gBanList = (List<String>) config.getList("playerBlockInteract.Global");
            List<String> banList = (List<String>) config.getList("playerBlockInteract." + player.getWorld().getName());

            if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                player.sendMessage(ChatColor.RED + "you can't interact with that item in this world");
                event.setCancelled(true);
            } else {
                return;
            }
        }

        //InventoryClickEvent
        @EventHandler
        public void onPlayerInventoryClickEvent(InventoryClickEvent event) {
            if (event.isCancelled()) {
                return;
            }
            int itemID;
            int meta;
            Player player = (Player) event.getWhoClicked();
            
            List<String> gBanList = (List<String>) config.getList("playerClickItemInventory.Global");
            List<String> banList = (List<String>) config.getList("playerClickItemInventory." + player.getWorld().getName());
            ItemStack[] contents = player.getInventory().getContents();
            ItemStack[] ChestContents;
            try {
                ChestContents = event.getView().getTopInventory().getContents();
            } catch (Exception e) {
                ChestContents = null;
            }
            
            for (int i = 0; i < contents.length; i++) {
                if (contents[i]!= null)
                {
                    itemID = contents[i].getTypeId();
                    meta = (int) contents[i].getData().getData();
                    boolean placedInChest = false;
                    if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                        if (!hasBypass(player, itemID, meta, "playerClickItemInventory")) {
                            if (ChestContents != null) {
                                for (int j = 0; j < ChestContents.length; j++) {
                                    if (ChestContents[j] == null) {
                                        ChestContents[j] = contents[i];
                                        event.getView().getTopInventory().setContents(ChestContents);
                                        placedInChest = true;
                                        break;
                                    }
                                }
                            }
                            
                            contents[i] = null;
                            player.sendMessage(ChatColor.RED + "That Item is banned");
                            if (placedInChest)
                            {
                                log.info("removed item:" + itemID + ":" + meta + " from player: " + player.getName()+ " and placed in chest viewed by player.");
                            }
                            {
                                log.info("removed item: " + itemID + ":" + meta + " from player: " + player.getName()+ " gone forever.");
                            }
                        }
                    } 
                }
                
            }
            player.getInventory().setContents(contents);
            ItemStack item = event.getCurrentItem();
            if (item==null)
            {
                return;
            }
            itemID = item.getTypeId();
            meta = (int)item.getData().getData();
            if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                if (!hasBypass(player, itemID, meta, "playerClickItemInventory")) {
                    event.setCursor(null);
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                }
            }
        }
        //PlayerPickup
        @EventHandler
        public void onPlayerPickupItem(PlayerPickupItemEvent event) {
            if (event.isCancelled())
            {
                return;
            }
            
            Player player = event.getPlayer();
            ItemStack itemStack = event.getItem().getItemStack();
            int itemID = itemStack.getTypeId();
            int meta = (int) itemStack.getData().getData();
            
            List<String> gBanList = (List<String>) config.getList("playerPickupItem.Global");
            List<String> banList = (List<String>) config.getList("playerPickupItem." + player.getWorld().getName());
            if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                if (!hasBypass(player, itemID, meta, "playerPickupItem")) {
                    event.setCancelled(true);
                }
            }
            
        }

        private boolean hasBypass(Player player, int itemID, int meta, String eventName) {
            String worldName = player.getWorld().getName();
            if (player.isOp()
                    || player.hasPermission("ItemControler.fullbypass")
                    || player.hasPermission("ItemControler.bypass." + eventName)
                    || player.hasPermission("ItemControler.bypass." + eventName + "." + worldName)
                    || player.hasPermission("ItemControler.bypass." + eventName + "." + itemID)
                    || player.hasPermission("ItemControler.bypass." + eventName + "." + itemID + ":" + meta)
                    || player.hasPermission("ItemControler.bypass." + eventName + "." + worldName + "." + itemID)
                    || player.hasPermission("ItemControler.bypass." + eventName + "." + worldName + "." + itemID + ":" + meta)) {
                return true;
            }

            return false;
        }

        
    }
    
    public static boolean isBannedTopPlace(List<String> banList, Block topBlock, Block bottomBlock) {
            if (banList == null) {
            return false;
            }
            for (String string : banList) {
                if (string.contains(","))
                {
                    
                    String[] split = string.split(",");
                    int topID = topBlock.getTypeId();
                    int bottomID = bottomBlock.getTypeId();
                    int topMeta = (int) topBlock.getData();
                    int bottomMeta = (int) bottomBlock.getData();
                    if (split[0].contains(":") || split[1].contains(":"))
                    {
                        String[] split1, split2;
                        if(split[0].contains(":"))
                        {
                            split1 = split[0].split(":");
                            if (split1[0].equals("" + topID) && split1[1].equals("" + topMeta))
                            {
                                if (split[1].contains(":"))
                                {
                                    split2 = split[1].split(":");
                                    if (split2[0].equals("" + bottomID) && split2[1].equals("" + bottomMeta))
                                    {
                                      return true;  
                                    }
                                }
                                else
                                {
                                    if (split[1].equals("" + bottomID))
                                    {
                                        return true;
                                    }
                                }
                            }
                        }
                        else
                        {
                            split2 = split[1].split(":");
                            if (split2[0].equals("" + bottomID) && split2[1].equals("" + bottomMeta))
                            {
                                
                                    if (split[0].equals("" +topID))
                                    {
                                        return true;
                                    }
                                
                            }
                        }
                        
                        
                        
                        
                    }
                    else
                    {
                        if (split[0].equals("" + topID) && split[1].equals("" + bottomID))
                        {
                            return true;
                        }
                    }
                }
            }
            
            return false;
        }

    public static boolean isBanned(List<String> banList, int itemID, int meta) {
        //System.out.println(itemID+":"+meta);
        if (banList == null) {
            return false;
        }
        for (String string : banList) {
            //System.out.println(string);
            if (string.contains(":")) {

                List<String> items = Arrays.asList(string.split("\\s*:\\s*"));
                if (items.get(0).equals("" + itemID)) {

                    if (items.get(1).equals("" + meta)) {
                        //System.out.println(""+true);
                        return true;
                    }
                }
            }

            if (string.equals("" + itemID)) {
                //  System.out.println(""+true);
                return true;
            }
        }

        return false;
    }
}

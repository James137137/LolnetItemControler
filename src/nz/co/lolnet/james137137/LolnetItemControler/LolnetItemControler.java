package nz.co.lolnet.james137137.LolnetItemControler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author James
 */
public class LolnetItemControler extends JavaPlugin {

    /**
     * @param args the command line arguments
     */
    private static Logger log;
    private static FileConfiguration config;
    private static HashMap<String, Boolean> DebugMode;
    
    @Override
    public void onEnable() {
        log = Bukkit.getLogger();
        String version = Bukkit.getServer().getPluginManager().getPlugin(this.getName()).getDescription().getVersion();
        log.log(Level.INFO, "{0}: Version: {1} Enabled.", new Object[]{this.getName(), version});
        
        getServer().getPluginManager().registerEvents(new myListener(this), this);
        saveDefaultConfig();
        config = getConfig();
        DebugMode = new HashMap<>();
        removeRecipes();
        
    }
    
    @Override
    public void onDisable() {
        log.log(Level.INFO, "{0}: disabled", this.getName());
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName();
        if (commandName.equalsIgnoreCase("LolnetItemControlerDebug") && sender.hasPermission("LolnetItemControler.debug")) {
            Boolean get = DebugMode.get(sender.getName());
            if (get == null || get == false) {
                DebugMode.put(sender.getName(), true);
                sender.sendMessage("on");
            } else {
                DebugMode.put(sender.getName(), false);
                sender.sendMessage("off");
            }
            return true;
        } else if (commandName.equalsIgnoreCase("LolnetItemControlerReload") && sender.hasPermission("LolnetItemControler.reload")) {
            config = getConfig();
            removeRecipes();
            String version = Bukkit.getServer().getPluginManager().getPlugin(this.getName()).getDescription().getVersion();
            log.log(Level.INFO, "{0}: Version: {1} config reloaded.", new Object[]{this.getName(), version});
        }
        return false;
    }
    
    private static class myListener implements Listener {
        
        LolnetItemControler plugin;
        
        public myListener(LolnetItemControler aThis) {
            plugin = aThis;
        }
        
        @EventHandler
        public void onPlayerBreakBlock(BlockBreakEvent event) {
            if (event.isCancelled()) {
                return;
            }
            Player player = event.getPlayer();
            Block block = event.getBlock();
            if (block == null) {
                return;
            }
            if (config.getBoolean("BlockAllBreakingBlocks") && !player.isOp()) {
                event.setCancelled(true);
            }
        }
        
        @EventHandler
        public void onPlayerPlaceBlockToBlock(BlockPlaceEvent event) {
            if (event.getPlayer().isOp() && event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                return;
            }
            String itemID = event.getBlock().getType().toString();
            List<String> globalList = config.getStringList("BlockToBlock.Global");
            List<String> localList = config.getStringList("BlockToBlock." + event.getBlock().getLocation().getWorld().getName());
            globalList.addAll(localList);
            for (String type : globalList) {
                if (type.split("~")[0].equalsIgnoreCase(itemID)) {
                    Material material = Material.getMaterial(type.split("~")[1]);
                    if (material != null) {
                        event.getBlock().setType(material);
                    } else {
                        Bukkit.getLogger().info("[LIC] got a null for " + type.split("~")[1] + " for BlockToBlock event." + "When placing " + type.split("~")[0]);
                    }
                    return;
                }
            }
            
        }
        
        @EventHandler
        public void onPlayerPlaceBlock(BlockPlaceEvent event) {
            
            if (event.isCancelled()) {
                return;
            }
            Player player = event.getPlayer();
            Block block = event.getBlock();
            if (block == null) {
                return;
            }
            String itemID = block.getType().toString();
            String meta = Integer.toString((int) block.getData());
            if (player == null) {
                
                List<String> gBanList = (List<String>) config.getList("placeByPlayer.Global");
                List<String> banList = (List<String>) config.getList("placeByPlayer." + block.getWorld().getName());
                if ((gBanList == null || gBanList.isEmpty()) && (banList == null || banList.isEmpty())) {
                    return;
                }
                if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                    event.setCancelled(true);
                    return;
                }
                
            }
            Boolean debugMode = DebugMode.get(player.getName());
            if (debugMode == null) {
                debugMode = false;
            }
            
            if (debugMode) {
                debug(player, debugMode, "" + itemID + "~" + meta);
                debug(player, debugMode, "has bypass " + hasBypass(player, itemID, meta, "placeByPlayer"));
                debug(player, debugMode, "World = " + player.getWorld().getName());
            }
            if (!hasBypass(player, itemID, meta, "placeByPlayer")) {
                List<String> gBanList = (List<String>) config.getList("placeByPlayer.Global");
                List<String> banList = (List<String>) config.getList("placeByPlayer." + player.getWorld().getName());
                if ((gBanList == null || gBanList.isEmpty()) && (banList == null || banList.isEmpty())) {
                    
                } else {
                    if (debugMode) {
                        debug(player, debugMode, "Gban = " + isBanned(gBanList, itemID, meta));
                        debug(player, debugMode, "world Ban = " + isBanned(banList, itemID, meta));
                    }
                    
                    if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                        player.sendMessage(ChatColor.RED + "you can't place with that in this world");
                        event.setCancelled(true);
                        return;
                    }
                }
                
            }
            
        }
        
        @EventHandler
        protected void onPlayerPlaceBlockOnTop(BlockPlaceEvent event) {
            if (event.isCancelled()) {
                return;
            }
            
            Player player = event.getPlayer();
            Block topBlock = event.getBlock();
            Boolean debugMode = DebugMode.get(player.getName());
            if (debugMode == null) {
                debugMode = false;
            }
            if (debugMode) {
                player.sendMessage("onPlayerPlaceBlockOnTop Event");
            }
            if (player == null || topBlock == null) {
                if (debugMode) {
                    player.sendMessage("player == null || topBlock == null = return;");
                }
                return;
            }
            Location location = topBlock.getLocation();
            if (player.isOp()) {
                if (debugMode) {
                    player.sendMessage("player.isOp() = return;");
                }
                return;
            }
            /*if (hasBypass(player, topItemID, topMeta, "placeByPlayerDirectlyBelow")) {
             return;
             }*/
            List<String> gBanList = (List<String>) config.getList("placeByPlayerDirectlyBelow.Global");
            List<String> banList = (List<String>) config.getList("placeByPlayerDirectlyBelow." + player.getWorld().getName());
            if ((gBanList == null || gBanList.isEmpty()) && (banList == null || banList.isEmpty())) {
                if (debugMode) {
                    player.sendMessage("(gBanList == null || gBanList.isEmpty()) && (banList == null || banList.isEmpty()) = return;");
                }
                return;
            }
            if (checkallSides(location, player, topBlock, debugMode)) {
                player.sendMessage(ChatColor.RED + "You can't place that block next to the other block");
                if (debugMode) {
                    player.sendMessage("checkallSides() = setCancelled");
                }
                event.setCancelled(true);
                log.info("[LolnetItemControler]: " + player.getName() + "tried to place item: " + topBlock.getTypeId() + "~" + ((int) topBlock.getData()) + "near of " + "some other banned block");
                return;
            } else {
                if (debugMode) {
                    player.sendMessage("checkallSides() = allowed");
                }
            }
            
        }
        
        public boolean checkallSides(Location location, Player player, Block block1, boolean debugMode) {
            List<String> gBanList = (List<String>) config.getList("placeByPlayerDirectlyBelow.Global");
            List<String> banList = (List<String>) config.getList("placeByPlayerDirectlyBelow." + player.getWorld().getName());
            Block top = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()).getBlock();
            if (debugMode) {
                player.sendMessage("checking with: " + block1.getType().toString());
            }
            if (checkIfBannedPlaceNear(block1, top, gBanList, banList)) {
                return true;
            }
            if (debugMode) {
                player.sendMessage(top.getType().toString() + ":allowed");
            }
            Block bottom = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()).getBlock();
            if (checkIfBannedPlaceNear(block1, bottom, gBanList, banList)) {
                return true;
            }
            if (debugMode) {
                player.sendMessage(bottom.getType().toString() + ":allowed");
            }
            Block left = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY(), location.getBlockZ()).getBlock();
            if (checkIfBannedPlaceNear(block1, left, gBanList, banList)) {
                return true;
            }
            if (debugMode) {
                player.sendMessage(left.getType().toString() + ":allowed");
            }
            Block right = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY(), location.getBlockZ()).getBlock();
            if (checkIfBannedPlaceNear(block1, right, gBanList, banList)) {
                return true;
            }
            if (debugMode) {
                player.sendMessage(right.getType().toString() + ":allowed");
            }
            Block back = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ() + 1).getBlock();
            if (checkIfBannedPlaceNear(block1, back, gBanList, banList)) {
                return true;
            }
            if (debugMode) {
                player.sendMessage(back.getType().toString() + ":allowed");
            }
            Block front = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ() - 1).getBlock();
            if (checkIfBannedPlaceNear(block1, front, gBanList, banList)) {
                return true;
            }
            if (debugMode) {
                player.sendMessage(front.getType().toString() + ":allowed");
            }
            return false;
        }
        
        public boolean checkIfBannedPlaceNear(Block Block1, Block Block2, List<String> gBanList, List<String> banList) {
            if (Block1 == null || Block2 == null) {
                return false;
            }
            if (isBannedTopPlace(gBanList, Block1, Block2) || isBannedTopPlace(banList, Block1, Block2)
                    || isBannedTopPlace(gBanList, Block2, Block1) || isBannedTopPlace(banList, Block2, Block1)) {
                return true;
            }
            return false;
        }
        
        @EventHandler
        public void onPrepareCraftItemEvent(PrepareItemCraftEvent event) {
            Recipe r = event.getRecipe();
            String itemID;
            String meta;
            try {
                itemID = r.getResult().getType().toString();
                meta = Integer.toString(r.getResult().getDurability());
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
            if ((gBanList == null || gBanList.isEmpty()) && (banList == null || banList.isEmpty())) {
                return;
            }
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
            String itemID;
            String meta;
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            itemID = block.getType().toString();
            Collection<ItemStack> drops = block.getDrops();
            for (ItemStack itemStack : drops) {
                meta = Integer.toString(itemStack.getDurability());
                if (hasBypass(player, itemID, meta, "playerBlockInteract")) {
                    return;
                }
                
                List<String> gBanList = (List<String>) config.getList("playerBlockInteract.Global");
                List<String> banList = (List<String>) config.getList("playerBlockInteract." + player.getWorld().getName());
                if ((gBanList == null || gBanList.isEmpty()) && (banList == null || banList.isEmpty())) {
                } else if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                    player.sendMessage(ChatColor.RED + "you can't interact with that item in this world");
                    event.setCancelled(true);
                    return;
                }
            }
            
        }
        
        @EventHandler
        public void onPlayerRightClickItem(PlayerInteractEvent event) {
            if (event.isCancelled() || !(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                return;
            }
            String itemID;
            String meta;
            Player player = event.getPlayer();
            ItemStack itemInHand = player.getInventory().getItemInHand();
            itemID = itemInHand.getType().toString();
            meta = Integer.toString(itemInHand.getDurability());
            if (hasBypass(player, itemID, meta, "playerRightClickItem")) {
                return;
            }
            
            List<String> gBanList = (List<String>) config.getList("playerRightClickItem.Global");
            List<String> banList = (List<String>) config.getList("playerRightClickItem." + player.getWorld().getName());
            if ((gBanList == null || gBanList.isEmpty()) && (banList == null || banList.isEmpty())) {
                return;
            }
            if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                player.sendMessage(ChatColor.RED + "you can't use that item in this world");
                event.setCancelled(true);
            }
        }
        
        @EventHandler
        public void onPlayerLeftClickItem(PlayerInteractEvent event) {
            if (event.isCancelled() || !(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                return;
            }
            String itemID;
            String meta;
            Player player = event.getPlayer();
            ItemStack itemInHand = player.getInventory().getItemInHand();
            itemID = itemInHand.getType().toString();
            meta = Integer.toString(itemInHand.getDurability());
            if (hasBypass(player, itemID, meta, "playerLeftClickItem")) {
                return;
            }
            
            List<String> gBanList = (List<String>) config.getList("playerLeftClickItem.Global");
            List<String> banList = (List<String>) config.getList("playerLeftClickItem." + player.getWorld().getName());
            if ((gBanList == null || gBanList.isEmpty()) && (banList == null || banList.isEmpty())) {
                return;
            }
            if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                player.sendMessage(ChatColor.RED + "you can't use that item in this world");
                event.setCancelled(true);
            }
        }

        //InventoryClickEvent
        @EventHandler
        public void onPlayerInventoryClickEvent(InventoryClickEvent event) {
            if (event.isCancelled()) {
                return;
            }
            String itemID;
            String meta;
            Player player = (Player) event.getWhoClicked();
            
            List<String> gBanList = (List<String>) config.getList("playerClickItemInventory.Global");
            List<String> banList = (List<String>) config.getList("playerClickItemInventory." + player.getWorld().getName());
            if ((gBanList == null || gBanList.isEmpty()) && (banList == null || banList.isEmpty())) {
                return;
            }
            ItemStack[] contents = player.getInventory().getContents();
            
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    itemID = contents[i].getType().toString();
                    meta = Integer.toString(contents[i].getDurability());
                    boolean placedInChest = false;
                    if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                        if (!hasBypass(player, itemID, meta, "playerClickItemInventory")) {
                            contents[i] = null;
                            player.sendMessage(ChatColor.RED + "That Item is banned");
                            
                            log.info("removed item: " + itemID + "~" + meta + " from player: " + player.getName() + " gone forever.");
                        }
                    }
                }
                
            }
            player.getInventory().setContents(contents);
            ItemStack item = event.getCurrentItem();
            if (item == null) {
                return;
            }
            itemID = item.getType().toString();
            meta = Integer.toString(item.getDurability());
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
            if (event.isCancelled()) {
                return;
            }
            
            Player player = event.getPlayer();
            ItemStack itemStack = event.getItem().getItemStack();
            String itemID = itemStack.getType().toString();
            String meta = Integer.toString(itemStack.getDurability());
            
            List<String> gBanList = (List<String>) config.getList("playerPickupItem.Global");
            List<String> banList = (List<String>) config.getList("playerPickupItem." + player.getWorld().getName());
            if ((gBanList == null || gBanList.isEmpty()) && (banList == null || banList.isEmpty())) {
                return;
            }
            if (isBanned(gBanList, itemID, meta) || isBanned(banList, itemID, meta)) {
                if (!hasBypass(player, itemID, meta, "playerPickupItem")) {
                    event.setCancelled(true);
                    return;
                }
            }
            
        }
        
        private boolean hasBypass(Player player, String itemID, String meta, String eventName) {
            String worldName = player.getWorld().getName();
            if (player.isOp()
                    || player.hasPermission("ItemControler.fullbypass")
                    || player.hasPermission("ItemControler.bypass." + eventName)
                    || player.hasPermission("ItemControler.bypass." + eventName + "." + worldName)
                    || player.hasPermission("ItemControler.bypass." + eventName + "." + itemID)
                    || player.hasPermission("ItemControler.bypass." + eventName + "." + itemID + "~" + meta)
                    || player.hasPermission("ItemControler.bypass." + eventName + "." + worldName + "." + itemID)
                    || player.hasPermission("ItemControler.bypass." + eventName + "." + worldName + "." + itemID + "~" + meta)) {
                return true;
            }
            
            return false;
        }
        
        private void debug(Player player, Boolean debugMode, String string) {
            if (!debugMode) {
                return;
            }
            player.sendMessage(string);
        }
        
    }
    
    public static boolean isBannedTopPlace(List<String> banList, Block topBlock, Block bottomBlock) {
        if (banList == null) {
            return false;
        }
        for (String string : banList) {
            if (string.contains(",")) {
                
                String[] split = string.split(",");
                String topID = topBlock.getType().toString();
                String bottomID = bottomBlock.getType().toString();
                int topMeta = (int) topBlock.getData();
                int bottomMeta = (int) bottomBlock.getData();
                if (split[0].contains("~") || split[1].contains("~")) {
                    String[] split1, split2;
                    if (split[0].contains("~")) {
                        split1 = split[0].split("~");
                        if (split1[0].equals("" + topID) && split1[1].equals("" + topMeta)) {
                            if (split[1].contains("~")) {
                                split2 = split[1].split("~");
                                if (split2[0].equals("" + bottomID) && split2[1].equals("" + bottomMeta)) {
                                    return true;
                                }
                            } else {
                                if (split[1].equals("" + bottomID)) {
                                    return true;
                                }
                            }
                        }
                    } else {
                        split2 = split[1].split("~");
                        if (split2[0].equals("" + bottomID) && split2[1].equals("" + bottomMeta)) {
                            
                            if (split[0].equals("" + topID)) {
                                return true;
                            }
                            
                        }
                    }
                    
                } else {
                    if (split[0].equals("" + topID) && split[1].equals("" + bottomID)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public static boolean isBanned(List<String> banList, String itemID, String meta) {
        //System.out.println(itemID+"~"+meta);
        if (banList == null) {
            return false;
        }
        for (String string : banList) {
            //System.out.println(string);
            if (string.contains("~")) {
                
                List<String> items = Arrays.asList(string.split("\\s*~\\s*"));
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
    
    public void removeRecipes() {
        
        Iterator<Recipe> it = getServer().recipeIterator();
        List<String> BanList = (List<String>) getConfig().getList("GlobalCraftBan");
        if (BanList == null || BanList.isEmpty()) {
            return;
        }
        while (it.hasNext()) {
            Recipe itRecipe = it.next();
            ItemStack itemStack = itRecipe.getResult();
            String itemID = itemStack.getType().toString();
            String meta = Integer.toString(itemStack.getDurability());
            if (isBanned(BanList, itemID, meta)) {
                it.remove();
                log.info("banned item:" + itemID + "~" + meta);
            }
        }
    }
}

package com.cnaude.chairs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class Chairs extends JavaPlugin {
    public static ChairEffects chairEffects;
    public List<ChairBlock> allowedBlocks;
    public List<Material> validSigns;
    public boolean autoRotate, signCheck, permissions, notifyplayer, opsOverridePerms;
    public boolean invertedStairCheck, seatOccupiedCheck, invertedStepCheck, perItemPerms, ignoreIfBlockInHand;
    public boolean sitEffectsEnabled;
    public boolean authmelogincorrection;
    public double sittingHeight, sittingHeightAdj, distance;
    public int maxChairWidth;
    public int sitMaxHealth;
    public int sitHealthPerInterval;
    public int sitEffectInterval;
    private File pluginFolder;
    private File configFile;    
    private Logger log;
    public PluginManager pm;
    public static ChairsIgnoreList ignoreList; 
    public String msgSitting, msgStanding, msgOccupied, msgNoPerm, msgReloaded, msgDisabled, msgEnabled;
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
    	log = this.getLogger();
        ignoreList = new ChairsIgnoreList(this);
        ignoreList.load();
        pm = this.getServer().getPluginManager();
        pluginFolder = getDataFolder();
        configFile = new File(pluginFolder, "config.yml");
        createConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(new EventListener(this, ignoreList), this);
        getCommand("chairs").setExecutor(new ChairsCommand(this, ignoreList));
        if (sitEffectsEnabled) {
            logInfo("Enabling sitting effects.");
            chairEffects = new ChairEffects(this);
        }
        protocolManager = ProtocolLibrary.getProtocolManager();
        new PacketListener(protocolManager, this);
    }

    @Override
    public void onDisable() {
    	protocolManager.getAsynchronousManager().unregisterAsyncHandlers(this);
    	protocolManager = null;
        for (String pName : new HashSet<String>(sit.keySet())) {
        	ejectPlayerOnDisable(Bukkit.getPlayerExact(pName));
        }
        if (ignoreList != null) {
            ignoreList.save();
        }
        if (chairEffects != null) {
            chairEffects.cancel();     
        }
        HandlerList.unregisterAll(this);
        log = null;
    }
    
    public void restartEffectsTask() {
        if (chairEffects != null) {
            chairEffects.restart();
        }
    }

    private void createConfig() {
        if (!pluginFolder.exists()) {
            try {
                pluginFolder.mkdir();
            } catch (Exception e) {
                logInfo("ERROR: " + e.getMessage());                
            }
        }

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception e) {
                logInfo("ERROR: " + e.getMessage());
            }
        }
    }
    
    protected HashMap<String, Entity> sit = new HashMap<String, Entity>();
    protected HashMap<Block, String> sitblock = new HashMap<Block, String>();
    protected HashMap<String, Block> sitblockbr = new HashMap<String, Block>();
    protected HashMap<String, Location> sitstopteleportloc = new HashMap<String, Location>();
    protected HashMap<String, Integer> sittask = new HashMap<String, Integer>();
    protected void reSitPlayer(final Player player)
    {
    	player.eject();
    	final Entity prevarrow = sit.get(player.getName());
		Block block = sitblockbr.get(player.getName());
		final Entity arrow = block.getWorld().spawnArrow(block.getLocation().add(0.5, 0, 0.5), new Vector(0, 0, 0), 0, 0);
		arrow.setPassenger(player);
		sit.put(player.getName(), arrow);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable()
		{
			public void run()
			{
				prevarrow.remove();
			}
		},100);
    }
    protected void ejectPlayer(final Player player)
    {
    	player.eject();
    	final Location tploc = sitstopteleportloc.get(player.getName());
    	if (tploc != null)
    	{
    		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
    			public void run()
    			{
    	    		player.teleport(tploc);
    			}
    		},1);
    	}
    	unSit(player);
    }
    private void ejectPlayerOnDisable(Player player)
    {
    	player.eject();
    	unSit(player);
    }
    protected void unSit(Player player) {
    	if (sit.containsKey(player.getName()))
    	{
    		sit.get(player.getName()).remove();
    		sitblock.remove(sitblockbr.get(player.getName()));
    		sitblockbr.remove(player.getName());
    		sitstopteleportloc.remove(player.getName());
    		sit.remove(player.getName());
    		Bukkit.getScheduler().cancelTask(sittask.get(player.getName()));
    		sittask.remove(player.getName());
    		if (notifyplayer && !msgStanding.isEmpty()) {
            	player.sendMessage(msgStanding);
        	}
    	}
    }


    public void loadConfig() {
        autoRotate = getConfig().getBoolean("auto-rotate");
        signCheck = getConfig().getBoolean("sign-check");
        sittingHeight = getConfig().getDouble("sitting-height");
        sittingHeightAdj = getConfig().getDouble("sitting-height-adj");
        distance = getConfig().getDouble("distance");
        maxChairWidth = getConfig().getInt("max-chair-width");
        permissions = getConfig().getBoolean("permissions");
        notifyplayer = getConfig().getBoolean("notify-player");
        invertedStairCheck = getConfig().getBoolean("upside-down-check");
        seatOccupiedCheck = getConfig().getBoolean("seat-occupied-check");
        invertedStepCheck = getConfig().getBoolean("upper-step-check");
        perItemPerms = getConfig().getBoolean("per-item-perms");
        opsOverridePerms = getConfig().getBoolean("ops-override-perms");
        ignoreIfBlockInHand = getConfig().getBoolean("ignore-if-block-in-hand");
        
        authmelogincorrection = getConfig().getBoolean("authme-loginlocation-correction");
        
        sitEffectsEnabled = getConfig().getBoolean("sit-effects.enabled", false);
        sitEffectInterval = getConfig().getInt("sit-effects.interval",20);
        sitMaxHealth = getConfig().getInt("sit-effects.healing.max-percent",100);
        sitHealthPerInterval = getConfig().getInt("sit-effects.healing.amount",1);
        
        msgSitting = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.sitting"));
        msgStanding = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.standing"));
        msgOccupied = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.occupied"));
        msgNoPerm = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.no-permission"));
        msgEnabled = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.enabled"));
        msgDisabled = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.disabled"));
        msgReloaded = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.reloaded"));

        allowedBlocks = new ArrayList<ChairBlock>();
        for (String s : getConfig().getStringList("allowed-blocks")) {
            String type;
            double sh = sittingHeight;
            String d = "0";
            if (s.contains(":")) {
                String tmp[] = s.split(":",3);
                type = tmp[0];                 
                if (!tmp[1].isEmpty()) {
                    sh = Double.parseDouble(tmp[1]);
                }                
                if (tmp.length == 3) {
                    d = tmp[2];
                }
            } else {
                type = s;                
            }
            try {                
                Material mat;
                if (type.matches("\\d+")) {
                    mat = Material.getMaterial(Integer.parseInt(type));
                } else {
                    mat = Material.matchMaterial(type);
                }
                if (mat != null) {                    
                    logInfo("Allowed block: " + mat.toString() + " => " + sh + " => " + d);
                    allowedBlocks.add(new ChairBlock(mat,sh,d));
                } else {
                    logError("Invalid block: " + type);
                }
            }
            catch (Exception e) {
                logError(e.getMessage());
            }
        }
        
        validSigns = new ArrayList<Material>();    
        for (String type : getConfig().getStringList("valid-signs")) {            
            try {
                if (type.matches("\\d+")) {
                    validSigns.add(Material.getMaterial(Integer.parseInt(type)));
                } else {
                    validSigns.add(Material.matchMaterial(type));
                }
            }
            catch (Exception e) {
                logError(e.getMessage());
            }
        }
        
        ArrayList<String> perms = new ArrayList<String>();
        perms.add("chairs.sit");
        perms.add("chairs.reload");
        perms.add("chairs.self");
        perms.add("chairs.sit.health");
        for (String s : perms) {
            if (pm.getPermission(s) != null) {
                pm.removePermission(s);
            }
        }
        PermissionDefault pd;
        if (opsOverridePerms) {
            pd = PermissionDefault.OP;
        } else {
            pd = PermissionDefault.FALSE;
        }
        
        pm.addPermission(new Permission("chairs.sit","Allow player to sit on a block.",pd));
        pm.addPermission(new Permission("chairs.reload","Allow player to reload the Chairs configuration.",pd));
        pm.addPermission(new Permission("chairs.self","Allow player to self disable or enable sitting.",pd));
    } 
    
    public void logInfo(String _message) {
        log.log(Level.INFO, _message);
    }

    public void logError(String _message) {
        log.log(Level.SEVERE, _message);
    }
    
    
        
}

package com.cnaude.chairs.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.cnaude.chairs.api.APIInit;
import com.cnaude.chairs.commands.ChairsCommand;
import com.cnaude.chairs.listeners.NANLoginListener;
import com.cnaude.chairs.listeners.TrySitEventListener;
import com.cnaude.chairs.listeners.TryUnsitEventListener;
import com.cnaude.chairs.sitaddons.ChairEffects;
import com.cnaude.chairs.sitaddons.CommandRestrict;
import com.cnaude.chairs.vehiclearrow.NMSAccess;

public class Chairs extends JavaPlugin {

	public HashSet<String> sitDisabled = new HashSet<String>();
	public ChairEffects chairEffects;
	public List<ChairBlock> allowedBlocks;
	public List<Material> validSigns;
	public boolean autoRotate, signCheck, notifyplayer;
	public boolean ignoreIfBlockInHand;
	public double distance;
	public int maxChairWidth;
	public boolean sitHealEnabled;
	public int sitMaxHealth;
	public int sitHealthPerInterval;
	public int sitHealInterval;
	public boolean sitPickupEnabled;
	public boolean sitDisableAllCommands = false;
	public HashSet<String> sitDisabledCommands = new HashSet<String>();
	private Logger log;
	public String msgSitting, msgStanding, msgOccupied, msgNoPerm, msgReloaded, msgDisabled, msgEnabled, msgCommandRestricted;


	private PlayerSitData psitdata;
	public PlayerSitData getPlayerSitData() {
		return psitdata;
	}
	private NMSAccess nmsaccess = new NMSAccess();
	protected NMSAccess getNMSAccess() {
		return nmsaccess;
	}

	@Override
	public void onEnable() {
		log = this.getLogger();
		try {
			nmsaccess.setupChairsArrow();
		} catch (Exception e) {
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		chairEffects = new ChairEffects(this);
		psitdata = new PlayerSitData(this);
		getConfig().options().copyDefaults(true);
		saveConfig();
		loadConfig();
		if (sitHealEnabled) {
			chairEffects.startHealing();
		}
		if (sitPickupEnabled) {
			chairEffects.startPickup();
		}
		getServer().getPluginManager().registerEvents(new NANLoginListener(), this);
		getServer().getPluginManager().registerEvents(new TrySitEventListener(this), this);
		getServer().getPluginManager().registerEvents(new TryUnsitEventListener(this), this);
		getServer().getPluginManager().registerEvents(new CommandRestrict(this), this);
		getCommand("chairs").setExecutor(new ChairsCommand(this));
		new APIInit().initAPI(getPlayerSitData());
	}

	@Override
	public void onDisable() {
		if (psitdata != null) {
			for (Player player : Utils.getOnlinePlayers()) {
				if (psitdata.isSitting(player)) {
					psitdata.unsitPlayerForce(player);
				}
			}
		}
		if (chairEffects != null) {
			chairEffects.cancelHealing();
			chairEffects.cancelPickup();
			chairEffects = null;
		}
		log = null;
		nmsaccess = null;
		psitdata = null;
	}

	public void loadConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(),"config.yml"));
		autoRotate = config.getBoolean("auto-rotate");
		signCheck = config.getBoolean("sign-check");
		distance = config.getDouble("distance");
		maxChairWidth = config.getInt("max-chair-width");
		notifyplayer = config.getBoolean("notify-player");
		ignoreIfBlockInHand = config.getBoolean("ignore-if-item-in-hand");

		sitHealEnabled = config.getBoolean("sit-effects.healing.enabled", false);
		sitHealInterval = config.getInt("sit-effects.healing.interval",20);
		sitMaxHealth = config.getInt("sit-effects.healing.max-percent",100);
		sitHealthPerInterval = config.getInt("sit-effects.healing.amount",1);

		sitPickupEnabled = config.getBoolean("sit-effects.itempickup.enabled", false);

		sitDisableAllCommands = config.getBoolean("sit-restrictions.commands.all");
		sitDisabledCommands = new HashSet<String>(config.getStringList("sit-restrictions.commands.list"));

		msgSitting = ChatColor.translateAlternateColorCodes('&',config.getString("messages.sitting"));
		msgStanding = ChatColor.translateAlternateColorCodes('&',config.getString("messages.standing"));
		msgOccupied = ChatColor.translateAlternateColorCodes('&',config.getString("messages.occupied"));
		msgNoPerm = ChatColor.translateAlternateColorCodes('&',config.getString("messages.no-permission"));
		msgEnabled = ChatColor.translateAlternateColorCodes('&',config.getString("messages.enabled"));
		msgDisabled = ChatColor.translateAlternateColorCodes('&',config.getString("messages.disabled"));
		msgReloaded = ChatColor.translateAlternateColorCodes('&',config.getString("messages.reloaded"));
		msgCommandRestricted = ChatColor.translateAlternateColorCodes('&',config.getString("messages.command-restricted"));

		allowedBlocks = new ArrayList<ChairBlock>();
		for (String s : config.getStringList("sit-blocks")) {
			String type;
			double sh = 0.7;
			String tmp[] = s.split("[:]");
			type = tmp[0];
			if (tmp.length == 2) {
				sh = Double.parseDouble(tmp[1]);
			}
			Material mat = Material.matchMaterial(type);
			if (mat != null) {
				logInfo("Allowed block: " + mat.toString() + " => " + sh);
				allowedBlocks.add(new ChairBlock(mat,sh));
			} else {
				logError("Invalid block: " + type);
			}
		}

		validSigns = new ArrayList<Material>();
		for (String type : config.getStringList("valid-signs")) {
			try {
				validSigns.add(Material.matchMaterial(type));
			}
			catch (Exception e) {
				logError(e.getMessage());
			}
		}
	}

	public void logInfo(String _message) {
		log.log(Level.INFO, _message);
	}

	public void logError(String _message) {
		log.log(Level.SEVERE, _message);
	}

}

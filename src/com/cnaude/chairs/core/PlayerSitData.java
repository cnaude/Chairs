package com.cnaude.chairs.core;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerSitData {

	private Chairs plugin;
	public PlayerSitData(Chairs plugin) {
		this.plugin = plugin;
	}

	private HashMap<String, Entity> sit = new HashMap<String, Entity>();
	private HashMap<Block, String> sitblock = new HashMap<Block, String>();
	private HashMap<String, Block> sitblockbr = new HashMap<String, Block>();
	private HashMap<String, Location> sitstopteleportloc = new HashMap<String, Location>();
	private HashMap<String, Integer> sittask = new HashMap<String, Integer>();
	public boolean isSitting(Player player) {
		return sit.containsKey(player.getName());
	}
	public boolean isBlockOccupied(Block block) {
		return sitblock.containsKey(block);
	}
	public Player getPlayerOnChair(Block chair) {
		return Bukkit.getPlayerExact(sitblock.get(chair));
	}
	public void sitPlayer(Player player, Location sitlocation) {
		try {
			if (plugin.notifyplayer) {
				player.sendMessage(plugin.msgSitting);
			}
			Block block = sitlocation.getBlock();
			sitstopteleportloc.put(player.getName(), player.getLocation());
			player.teleport(sitlocation);
			Location arrowloc = block.getLocation().add(0.5, 0 , 0.5);
			Entity arrow = sitPlayerOnArrow(player, arrowloc);
			sit.put(player.getName(), arrow);
			sitblock.put(block, player.getName());
			sitblockbr.put(player.getName(), block);
			startReSitTask(player);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void startReSitTask(final Player player) {
		int task =
				Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
					@Override
					public void run() {
						reSitPlayer(player);
					}
				},1000,1000);
		sittask.put(player.getName(), task);
	}
	public void reSitPlayer(final Player player) {
		try {
			final Entity prevarrow = sit.get(player.getName());
			sit.remove(player.getName());
			player.eject();
			Block block = sitblockbr.get(player.getName());
			Location arrowloc = block.getLocation().add(0.5, 0 , 0.5);
			Entity arrow = sitPlayerOnArrow(player, arrowloc);
			sit.put(player.getName(), arrow);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					prevarrow.remove();
				}
			},100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private Entity sitPlayerOnArrow(Player player, Location arrowloc) throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Entity arrow = plugin.getNMSAccess().spawnArrow(arrowloc);
		arrow.setPassenger(player);
		return arrow;
	}
	public void unsitPlayerNormal(Player player) {
		unsitPlayer(player, false, true, false);
	}
	public void unsitPlayerForce(Player player) {
		unsitPlayer(player, true, true, false);
	}
	public void unsitPlayerNow(Player player) {
		unsitPlayer(player, true, false, true);
	}
	private void unsitPlayer(final Player player, boolean eject,  boolean restoreposition, boolean correctleaveposition) {
		final Entity arrow = sit.get(player.getName());
		sit.remove(player.getName());
		if (eject) {
			player.eject();
		}
		arrow.remove();
		final Location tploc = sitstopteleportloc.get(player.getName());
		if (restoreposition) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					player.teleport(tploc);
					player.setSneaking(false);
				}
			},1);
		} else if (correctleaveposition) {
			player.teleport(tploc);
		}
		sitblock.remove(sitblockbr.get(player.getName()));
		sitblockbr.remove(player.getName());
		sitstopteleportloc.remove(player.getName());
		Bukkit.getScheduler().cancelTask(sittask.get(player.getName()));
		sittask.remove(player.getName());
		if (plugin.notifyplayer) {
			player.sendMessage(plugin.msgStanding);
		}
	}

}

package com.cnaude.chairs.core;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.cnaude.chairs.api.PlayerChairSitEvent;
import com.cnaude.chairs.api.PlayerChairUnsitEvent;

public class PlayerSitData {

	private Chairs plugin;
	public PlayerSitData(Chairs plugin) {
		this.plugin = plugin;
	}

	private HashMap<Player, SitData> sit = new HashMap<Player, SitData>();
	private HashMap<Block, Player> sitblock = new HashMap<Block, Player>();

	public boolean isSitting(Player player) {
		return sit.containsKey(player) && sit.get(player).sitting;
	}

	public boolean isBlockOccupied(Block block) {
		return sitblock.containsKey(block);
	}

	public Player getPlayerOnChair(Block chair) {
		return sitblock.get(chair);
	}

	public boolean sitPlayer(final Player player,  Block blocktooccupy, Location sitlocation) {
		PlayerChairSitEvent playersitevent = new PlayerChairSitEvent(player, sitlocation.clone());
		Bukkit.getPluginManager().callEvent(playersitevent);
		if (playersitevent.isCancelled()) {
			return false;
		}
		sitlocation = playersitevent.getSitLocation().clone();
		if (plugin.notifyplayer) {
			player.sendMessage(plugin.msgSitting);
		}
		SitData sitdata = new SitData();
		Entity arrow = plugin.getNMSAccess().spawnArrow(sitlocation.getBlock().getLocation().add(0.5, 0 , 0.5));
		sitdata.arrow = arrow;
		sitdata.block = blocktooccupy;
		sitdata.teleportloc = player.getLocation();
		int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(
			plugin,
			new Runnable() {
				@Override
				public void run() {
					reSitPlayer(player);
				}
			},
			1000, 1000
		);
		sitdata.resittask = task;
		player.teleport(sitlocation);
		arrow.setPassenger(player);
		sit.put(player, sitdata);
		sitblock.put(blocktooccupy, player);
		sitdata.sitting = true;
		return true;
	}

	public void reSitPlayer(final Player player) {
		SitData sitdata = sit.get(player);
		sitdata.sitting = false;
		Entity prevarrow = sit.get(player).arrow;
		Entity arrow = plugin.getNMSAccess().spawnArrow(prevarrow.getLocation());
		arrow.setPassenger(player);
		sitdata.arrow = arrow;
		prevarrow.remove();
		sitdata.sitting = true;
	}

	public boolean unsitPlayer(Player player) {
		return unsitPlayer(player, true);
	}

	public void unsitPlayerForce(Player player) {
		unsitPlayer(player, false);
	}

	private boolean unsitPlayer(final Player player, boolean canCancel) {
		SitData sitdata = sit.get(player);
		final PlayerChairUnsitEvent playerunsitevent = new PlayerChairUnsitEvent(player, sitdata.teleportloc.clone(), canCancel);
		Bukkit.getPluginManager().callEvent(playerunsitevent);
		if (playerunsitevent.isCancelled() && playerunsitevent.canBeCancelled()) {
			return false;
		}
		sitdata.sitting = false;
		player.leaveVehicle();
		sitdata.arrow.remove();
		player.teleport(playerunsitevent.getTeleportLocation().clone());
		player.setSneaking(false);
		sitblock.remove(sitdata.block);
		Bukkit.getScheduler().cancelTask(sitdata.resittask);
		sit.remove(player);
		if (plugin.notifyplayer) {
			player.sendMessage(plugin.msgStanding);
		}
		return true;
	}

	private class SitData {

		private boolean sitting;
		private Entity arrow;
		private Location teleportloc;
		private int resittask;
		private Block block;

	}

}

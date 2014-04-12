package com.cnaude.chairs.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import com.cnaude.chairs.api.PlayerChairUnsitEvent;
import com.cnaude.chairs.core.Chairs;

public class TryUnsitEventListener implements Listener {

	public Chairs plugin;

	public TryUnsitEventListener(Chairs plugin) {
		this.plugin = plugin;
	}

	//spigot...
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		if (plugin.getPlayerSitData().isSitting(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (plugin.getPlayerSitData().isSitting(player)) {
			PlayerChairUnsitEvent playerunsitevent = new PlayerChairUnsitEvent(player, false);
			Bukkit.getPluginManager().callEvent(playerunsitevent);
			plugin.getPlayerSitData().unsitPlayerNow(player);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (plugin.getPlayerSitData().isSitting(player)) {
			PlayerChairUnsitEvent playerunsitevent = new PlayerChairUnsitEvent(player, false);
			Bukkit.getPluginManager().callEvent(playerunsitevent);
			plugin.getPlayerSitData().unsitPlayerNow(player);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onExitVehicle(VehicleExitEvent e) {
		if (e.getVehicle().getPassenger() instanceof Player) {
			final Player player = (Player) e.getVehicle().getPassenger();
			if (plugin.getPlayerSitData().isSitting(player)) {
				PlayerChairUnsitEvent playerunsitevent = new PlayerChairUnsitEvent(player, true);
				Bukkit.getPluginManager().callEvent(playerunsitevent);
				if (!playerunsitevent.isCancelled()) {
					plugin.getPlayerSitData().unsitPlayerNormal(player);
				} else {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		if (plugin.getPlayerSitData().isBlockOccupied(b)) {
			Player player = plugin.getPlayerSitData().getPlayerOnChair(b);
			PlayerChairUnsitEvent playerunsitevent = new PlayerChairUnsitEvent(player, false);
			Bukkit.getPluginManager().callEvent(playerunsitevent);
			plugin.getPlayerSitData().unsitPlayerForce(player);
		}
	}

}

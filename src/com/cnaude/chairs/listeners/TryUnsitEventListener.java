package com.cnaude.chairs.listeners;

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

import com.cnaude.chairs.core.Chairs;

public class TryUnsitEventListener implements Listener {

	public Chairs plugin;

	public TryUnsitEventListener(Chairs plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		if (plugin.getPlayerSitData().isSitting(player)) {
			plugin.getPlayerSitData().unsitPlayerForce(player);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (plugin.getPlayerSitData().isSitting(player)) {
			plugin.getPlayerSitData().unsitPlayerForce(player);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (plugin.getPlayerSitData().isSitting(player)) {
			plugin.getPlayerSitData().unsitPlayerForce(player);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onExitVehicle(VehicleExitEvent e) {
		if (e.getVehicle().getPassenger() instanceof Player) {
			final Player player = (Player) e.getVehicle().getPassenger();
			if (plugin.getPlayerSitData().isSitting(player)) {
				if (!plugin.getPlayerSitData().unsitPlayer(player)) {
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
			plugin.getPlayerSitData().unsitPlayerForce(player);
		}
	}

}

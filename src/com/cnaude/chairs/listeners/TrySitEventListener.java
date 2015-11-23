package com.cnaude.chairs.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.material.WoodenStep;

import com.cnaude.chairs.core.ChairBlock;
import com.cnaude.chairs.core.Chairs;

public class TrySitEventListener implements Listener {

	public Chairs plugin;

	public TrySitEventListener(Chairs plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player player = event.getPlayer();
			Block block = event.getClickedBlock();
			if (sitAllowed(player, block)) {
				Location sitLocation = getSitLocation(block, player.getLocation().getYaw());
				if (plugin.getPlayerSitData().sitPlayer(player, block, sitLocation)) {
					event.setCancelled(true);
				}
			}
		}
	}

	private boolean sitAllowed(Player player, Block block) {

		// Check for sitting disabled
		if (plugin.sitDisabled.contains(player.getName())) {
			return false;
		}

		// Check for permissions
		if (!player.hasPermission("chairs.sit")) {
			return false;
		}

		// Check for already sitting
		if (isSitting(player)) {
			return false;
		}

		// Check for item in hand
		if (plugin.ignoreIfBlockInHand && player.getItemInHand().getType() != Material.AIR) {
			return false;
		}

		// Check for sneaking
		if (player.isSneaking()) {
			return false;
		}

		// Sit occupied check
		if (plugin.getPlayerSitData().isBlockOccupied(block)) {
			player.sendMessage(plugin.msgOccupied.replace("%PLAYER%", plugin.getPlayerSitData().getPlayerOnChair(block).getName()));
			return false;
		}

		Stairs stairs = null;
		Step step = null;
		WoodenStep wStep = null;

		// Check for block is chair
		if (isValidChair(block)) {

			if (block.getState().getData() instanceof Stairs) {
				stairs = (Stairs) block.getState().getData();
			} else if (block.getState().getData() instanceof Step) {
				step = (Step) block.getState().getData();
			} else if (block.getState().getData() instanceof WoodenStep) {
				wStep = (WoodenStep) block.getState().getData();
			}

			int chairwidth = 1;

			// Check if block beneath chair is solid.
			if (block.getRelative(BlockFace.DOWN).isLiquid()) {
				return false;
			}
			if (block.getRelative(BlockFace.DOWN).isEmpty()) {
				return false;
			}
			if (!block.getRelative(BlockFace.DOWN).getType().isSolid()) {
				return false;
			}

			// Check for distance distance between player and chair.
			if (plugin.distance > 0 && player.getLocation().distance(block.getLocation().add(0.5, 0, 0.5)) > plugin.distance) {
				return false;
			}

			// Check if block is inverted
			if (stairs != null && stairs.isInverted()) {
				return false;
			}
			if (step != null && step.isInverted()) {
				return false;
			}
			if (wStep != null && wStep.isInverted()) {
				return false;
			}

			// Check for signs (only for stairs)
			if (plugin.signCheck && stairs != null) {
				boolean sign1 = false;
				boolean sign2 = false;

				if (stairs.getDescendingDirection() == BlockFace.NORTH || stairs.getDescendingDirection() == BlockFace.SOUTH) {
					sign1 = checkSign(block, BlockFace.EAST) || checkFrame(block, BlockFace.EAST, player);
					sign2 = checkSign(block, BlockFace.WEST) || checkFrame(block, BlockFace.WEST, player);
				} else if (stairs.getDescendingDirection() == BlockFace.EAST || stairs.getDescendingDirection() == BlockFace.WEST) {
					sign1 = checkSign(block, BlockFace.NORTH) || checkFrame(block, BlockFace.NORTH, player);
					sign2 = checkSign(block, BlockFace.SOUTH) || checkFrame(block, BlockFace.SOUTH, player);
				}

				if (!(sign1 && sign2)) {
					return false;
				}
			}

			// Check for maximal chair width (only for stairs)
			if (plugin.maxChairWidth > 0 && stairs != null) {
				if (stairs.getDescendingDirection() == BlockFace.NORTH || stairs.getDescendingDirection() == BlockFace.SOUTH) {
					chairwidth += getChairWidth(block, BlockFace.EAST);
					chairwidth += getChairWidth(block, BlockFace.WEST);
				} else if (stairs.getDescendingDirection() == BlockFace.EAST || stairs.getDescendingDirection() == BlockFace.WEST) {
					chairwidth += getChairWidth(block, BlockFace.NORTH);
					chairwidth += getChairWidth(block, BlockFace.SOUTH);
				}

				if (chairwidth > plugin.maxChairWidth) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	private Location getSitLocation(Block block, Float playerYaw) {
		double sh = 0.7;

		for (ChairBlock cb : plugin.allowedBlocks) {
			if (cb.getMat().equals(block.getType())) {
				sh = cb.getSitHeight();
				break;
			}
		}

		Stairs stairs = null;
		if (block.getState().getData() instanceof Stairs) {
			stairs = (Stairs) block.getState().getData();
		}

		Location plocation = block.getLocation();
		plocation.add(0.5D, (sh - 0.5D), 0.5D);

		// Rotate the player's view to the descending side of the block.
		if (plugin.autoRotate && stairs != null) {
			switch (stairs.getDescendingDirection()) {
				case NORTH: {
					plocation.setYaw(180);
					break;
				}
				case EAST: {
					plocation.setYaw(-90);
					break;
				}
				case SOUTH: {
					plocation.setYaw(0);
					break;
				}
				case WEST: {
					plocation.setYaw(90);
					break;
				}
				default: {
				}
			}
		} else {
			plocation.setYaw(playerYaw);
		}
		return plocation;
	}



	private boolean isValidChair(Block block) {
		for (ChairBlock cb : plugin.allowedBlocks) {
			if (cb.getMat().equals(block.getType())) {
				return true;
			}
		}
		return false;
	}

	private boolean isSitting(Player player) {
		return plugin.getPlayerSitData().isSitting(player);
	}

	private int getChairWidth(Block block, BlockFace face) {
		int width = 0;

		// Go through the blocks next to the clicked block and check if there are any further stairs.
		for (int i = 1; i <= plugin.maxChairWidth; i++) {
			Block relative = block.getRelative(face, i);
			if (relative.getState().getData() instanceof Stairs) {
				if (isValidChair(relative) && ((Stairs) relative.getState().getData()).getDescendingDirection() == ((Stairs) block.getState().getData()).getDescendingDirection()) {
					width++;
				} else {
					break;
				}
			}
		}

		return width;
	}

	private boolean checkSign(Block block, BlockFace face) {
		// Go through the blocks next to the clicked block and check if are signs on the end.
		for (int i = 1; i <= plugin.maxChairWidth + 2; i++) {
			Block relative = block.getRelative(face, i);
			if (checkDirection(block, relative)) {
				continue;
			}
			if (plugin.validSigns.contains(relative.getType())) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	private boolean checkDirection(Block block1, Block block2) {
		if (block1.getState().getData() instanceof Stairs && block2.getState().getData() instanceof Stairs) {
			if (((Stairs) block1.getState().getData()).getDescendingDirection().equals(((Stairs) block2.getState().getData()).getDescendingDirection())) {
				return true;
			}
		}
		return false;
	}

	private boolean checkFrame(Block block, BlockFace face, Player player) {
		// Go through the blocks next to the clicked block and check if are signs on the end.

		for (int i = 1; i <= plugin.maxChairWidth + 2; i++) {
			Block relative = block.getRelative(face, i);
			if (checkDirection(block, relative)) {
				continue;
			}
			if (relative.getType().equals(Material.AIR)) {
				int x = relative.getLocation().getBlockX();
				int y = relative.getLocation().getBlockY();
				int z = relative.getLocation().getBlockZ();
				for (Entity e : player.getNearbyEntities(plugin.distance, plugin.distance, plugin.distance)) {
					if (e instanceof ItemFrame && plugin.validSigns.contains(Material.ITEM_FRAME)) {
						int x2 = e.getLocation().getBlockX();
						int y2 = e.getLocation().getBlockY();
						int z2 = e.getLocation().getBlockZ();
						if (x == x2 && y == y2 && z == z2) {
							return true;
						}
					}
				}
				return false;
			} else {
				return false;
			}
		}
		return false;
	}
}
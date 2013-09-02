package com.cnaude.chairs;

import org.bukkit.Bukkit;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.material.WoodenStep;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;

public class EventListener implements Listener {

    public Chairs plugin;
    public ChairsIgnoreList ignoreList;

    public EventListener(Chairs plugin, ChairsIgnoreList ignoreList) {
        this.plugin = plugin;
        this.ignoreList = ignoreList;
    }

    public boolean isValidChair(Block block) {
        for (ChairBlock cb : plugin.allowedBlocks) {
            if (cb.getMat().equals(block.getType())) {
                return true;
            }
        }
        return false;
    }


    private boolean isSitting(Player player) {
        if (player.isInsideVehicle()) {
            if (plugin.sit.containsKey(player.getName())) {
                return true;
            }
        } else if (plugin.sit.containsKey(player.getName())) {
        	plugin.unSit(player);
        }
        return false;
    }
    
    
    @EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
    public void onJoin(PlayerJoinEvent e)
    {
    	if (!plugin.authmelogincorrection)
    	{
    		return;
    	}
    	
    	Player player = e.getPlayer();
    	Location loc = player.getLocation();
    	if (Double.isNaN(loc.getY()))
    	{
    		loc = player.getBedSpawnLocation();
    		if (loc == null)
    		{
    			loc = player.getWorld().getSpawnLocation();
    		}
			player.teleport(loc);
    	}
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
    	Player player = event.getPlayer();
    	if (plugin.sit.containsKey(player.getName()))
    	{
    		plugin.ejectPlayer(player);
    	}
    }
    
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event)
    {
    	Block b = event.getBlock();
    	if (plugin.sitblock.containsKey(b))
    	{
    		Player player = Bukkit.getPlayerExact(plugin.sitblock.get(b));
    		plugin.ejectPlayer(player);
    	}
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isSitting(player)) {
            return;
        }
        if (event.getPlayer().getItemInHand().getType().isBlock()
                && (event.getPlayer().getItemInHand().getTypeId() != 0)
                && plugin.ignoreIfBlockInHand) {
            return;
        }
        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        	sitPlayer(player, event.getClickedBlock());
        	if (sit) {
        		event.setCancelled(true);
        		sit = false;
        	}
        }
    }
    private boolean sit = false;
    protected void sitPlayer(Player player, Block block)
    {
        Stairs stairs = null;
        Step step = null;
        WoodenStep wStep = null;
        double sh = plugin.sittingHeight;
        boolean blockOkay = false;

        if (ignoreList.isIgnored(player.getName())) {
            return;
        }
        // Permissions Check
        if (plugin.permissions) {
            if (!player.hasPermission("chairs.sit")) {
                return;
            }
        }
        if (plugin.perItemPerms) {
            if (plugin.pm.getPermission("chairs.sit." + block.getTypeId()) == null) {
                plugin.pm.addPermission(new Permission("chairs.sit." + block.getTypeId(),
                        "Allow players to sit on a '" + block.getType().name() + "'",
                        PermissionDefault.FALSE));
            }
            if (plugin.pm.getPermission("chairs.sit." + block.getType().toString()) == null) {
                plugin.pm.addPermission(new Permission("chairs.sit." + block.getType().toString(),
                        "Allow players to sit on a '" + block.getType().name() + "'",
                        PermissionDefault.FALSE));
            }
            if (plugin.pm.getPermission("chairs.sit." + block.getTypeId() + ":" + block.getData()) == null) {
                plugin.pm.addPermission(new Permission("chairs.sit." + block.getTypeId() + ":" + block.getData(),
                        "Allow players to sit on a '" + block.getType().name() + "'",
                        PermissionDefault.FALSE));
            }
            if (plugin.pm.getPermission("chairs.sit." + block.getType().toString() + ":" + block.getData()) == null) {
                plugin.pm.addPermission(new Permission("chairs.sit." + block.getType().toString() + ":" + block.getData(),
                        "Allow players to sit on a '" + block.getType().name() + "'",
                        PermissionDefault.FALSE));
            }
        }

        for (ChairBlock cb : plugin.allowedBlocks) {
            if (cb.getMat().toString().contains("STAIRS")) {
                if (cb.getMat().equals(block.getType())) {
                    blockOkay = true;
                    sh = cb.getSitHeight();
                    continue;
                }
            } else if (cb.getMat().equals(block.getType())
                    && cb.getDamage() == block.getData()) {
                blockOkay = true;
                sh = cb.getSitHeight();
                continue;
            }
        }
        if (blockOkay
                || (player.hasPermission("chairs.sit." + block.getTypeId() + ":" + block.getData()) && plugin.perItemPerms)
                || (player.hasPermission("chairs.sit." + block.getType().toString() + ":" + block.getData()) && plugin.perItemPerms)
                || (player.hasPermission("chairs.sit." + block.getTypeId()) && plugin.perItemPerms)
                || (player.hasPermission("chairs.sit." + block.getType().toString()) && plugin.perItemPerms)) {

            if (block.getState().getData() instanceof Stairs) {
                stairs = (Stairs) block.getState().getData();
            } else if (block.getState().getData() instanceof Step) {
                step = (Step) block.getState().getData();
            } else if (block.getState().getData() instanceof WoodenStep) {
                wStep = (WoodenStep) block.getState().getData();
            } else {
                sh += plugin.sittingHeightAdj;
            }

            int chairwidth = 1;

            // Check if block beneath chair is solid.
            if (block.getRelative(BlockFace.DOWN).isLiquid()) {
                return;
            }
            if (block.getRelative(BlockFace.DOWN).isEmpty()) {
                return;
            }
            if (!block.getRelative(BlockFace.DOWN).getType().isSolid()) {
                return;
            }

            // Check for distance distance between player and chair.
            if (plugin.distance > 0 && player.getLocation().distance(block.getLocation().add(0.5, 0, 0.5)) > plugin.distance) {
                return;
            }

            if (stairs != null) {
                if (stairs.isInverted() && plugin.invertedStairCheck) {
                    return;
                }
            }
            if (step != null) {
                if (step.isInverted() && plugin.invertedStepCheck) {
                    return;
                }
            }
            if (wStep != null) {
                if (wStep.isInverted() && plugin.invertedStepCheck) {
                    return;
                }
            }
            
            // Check for signs.
            if (plugin.signCheck == true && stairs != null) {
                boolean sign1 = false;
                boolean sign2 = false;

                if (stairs.getDescendingDirection() == BlockFace.NORTH || stairs.getDescendingDirection() == BlockFace.SOUTH) {
                    sign1 = checkSign(block, BlockFace.EAST) || checkFrame(block, BlockFace.EAST, player);
                    sign2 = checkSign(block, BlockFace.WEST) || checkFrame(block, BlockFace.WEST, player);
                } else if (stairs.getDescendingDirection() == BlockFace.EAST || stairs.getDescendingDirection() == BlockFace.WEST) {
                    sign1 = checkSign(block, BlockFace.NORTH) || checkFrame(block, BlockFace.NORTH, player);
                    sign2 = checkSign(block, BlockFace.SOUTH) || checkFrame(block, BlockFace.SOUTH, player);
                }

                if (!(sign1 == true && sign2 == true)) {
                    return;
                }
            }

            // Check for maximal chair width.
            if (plugin.maxChairWidth > 0 && stairs != null) {
                if (stairs.getDescendingDirection() == BlockFace.NORTH || stairs.getDescendingDirection() == BlockFace.SOUTH) {
                    chairwidth += getChairWidth(block, BlockFace.EAST);
                    chairwidth += getChairWidth(block, BlockFace.WEST);
                } else if (stairs.getDescendingDirection() == BlockFace.EAST || stairs.getDescendingDirection() == BlockFace.WEST) {
                    chairwidth += getChairWidth(block, BlockFace.NORTH);
                    chairwidth += getChairWidth(block, BlockFace.SOUTH);
                }

                if (chairwidth > plugin.maxChairWidth) {
                    return;
                }
            }
            
            // Sit-down process.
                if (plugin.seatOccupiedCheck) {
                    if (!plugin.sit.isEmpty()) {
                    	if (plugin.sitblock.containsKey(block))
                    	{
                    		if (!plugin.msgOccupied.isEmpty()) {
                    			player.sendMessage(plugin.msgOccupied.replaceAll("%PLAYER%", plugin.sitblock.get(block)));
                    		}
                    		return;
                    	}
                    }
                }

                Location plocation = block.getLocation().clone();
                plocation.add(0.5D, (sh - 0.5D), 0.5D);

                // Rotate the player's view to the descending side of the block.
                if (plugin.autoRotate && stairs != null) {
                    switch (stairs.getDescendingDirection()) {
                        case NORTH:
                            plocation.setYaw(180);
                            break;
                        case EAST:
                            plocation.setYaw(-90);
                            break;
                        case SOUTH:
                            plocation.setYaw(0);
                            break;
                        case WEST:
                            plocation.setYaw(90);
					default:
						;
                    }
                } else {
                    plocation.setYaw(player.getLocation().getYaw());
                }

                if (plugin.notifyplayer && !plugin.msgSitting.isEmpty()) {
                    player.sendMessage(plugin.msgSitting);
                }

                plugin.sitstopteleportloc.put(player.getName(), player.getLocation());
                player.teleport(plocation);
                Entity arrow = block.getWorld().spawnArrow(getBlockCentre(block).subtract(0, 0.5, 0), new Vector(0, 0, 0), 0, 0);
                arrow.setPassenger(player);
                player.setSneaking(false);
                arrow.setTicksLived(1);
                plugin.sit.put(player.getName(), arrow);
                plugin.sitblock.put(block, player.getName());
                plugin.sitblockbr.put(player.getName(), block);
                startReSitTask(player);
                sit = true;
        }
    }
    
    protected void startReSitTask(final Player player)
    {
    	int task = 
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
    	{
    		public void run()
    		{
    			plugin.reSitPlayer(player);
    		}    	
    	},1000,1000);
    	plugin.sittask.put(player.getName(), task);
    }
    

    // https://github.com/sk89q/craftbook/blob/master/src/main/java/com/sk89q/craftbook/util/BlockUtil.java
    public static Location getBlockCentre(Block block) {
        return block.getLocation().add(0.5, 0.5, 0.5);
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
        for (int i = 1; i <= 100; i++) {
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
        if (block1.getState().getData() instanceof Stairs
                && block2.getState().getData() instanceof Stairs) {
            if (((Stairs) block1.getState().getData()).getDescendingDirection()
                    .equals(((Stairs) block2.getState().getData()).getDescendingDirection())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkFrame(Block block, BlockFace face, Player player) {
        // Go through the blocks next to the clicked block and check if are signs on the end.

        for (int i = 1; i <= 100; i++) {
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
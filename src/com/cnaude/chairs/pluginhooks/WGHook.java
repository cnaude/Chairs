package com.cnaude.chairs.pluginhooks;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;

public class WGHook {

	public static boolean isAllowedInRegion(HashSet<String> disabledRegions, Location location) {
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
			return true;
		}
		if (disabledRegions.isEmpty()) {
			return true;
		}

		List<String> aregions = WGBukkit.getRegionManager(location.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(location));
		for (String region : aregions) {
			if (disabledRegions.contains(region)) {
				return false;
			}
		}
		return true;
	}

}

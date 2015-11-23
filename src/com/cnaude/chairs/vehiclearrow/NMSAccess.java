package com.cnaude.chairs.vehiclearrow;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;

public class NMSAccess {

	private NMSArrowFactoryInterface arrowfactory;

	public void setupChairsArrow() throws NMSAccessException, ClassNotFoundException {
		String pkgname = getClass().getPackage().getName();
		String packageName = Bukkit.getServer().getClass().getPackage().getName();
		String nmspackageversion = packageName.substring(packageName.lastIndexOf('.') + 1);
		try {
			arrowfactory = (NMSArrowFactoryInterface) Class.forName(pkgname+"."+nmspackageversion+".NMSArrowFactory").newInstance();
			return;
		} catch (Throwable t) {
		}
		throw new NMSAccessException("ChairsReloaded is not compatible with your server version");
	}

	public Arrow spawnArrow(Location location) {
		return arrowfactory.spawnArrow(location);
	}

}

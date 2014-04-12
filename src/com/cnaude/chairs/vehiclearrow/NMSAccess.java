package com.cnaude.chairs.vehiclearrow;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;

public class NMSAccess {

	private Class<?> nmsArrowClass;

	public void setupChairsArrow() throws NMSAccessException, ClassNotFoundException {
		String pkgname = getClass().getPackage().getName();
		String packageName = Bukkit.getServer().getClass().getPackage().getName();
		String nmspackageversion = packageName.substring(packageName.lastIndexOf('.') + 1);
		switch (nmspackageversion) {
			case "v1_7_R3": {
				nmsArrowClass = Class.forName(pkgname+"."+"nms178"+".NMSChairsArrow");
				return;
			}
			case "v1_7_R2": {
				nmsArrowClass = Class.forName(pkgname+"."+"nms175"+".NMSChairsArrow");
				return;
			}
			case "v1_7_R1": {
				nmsArrowClass = Class.forName(pkgname+"."+"nms172"+".NMSChairsArrow");
				return;
			}
			case "v1_6_R3": {
				nmsArrowClass = Class.forName(pkgname+"."+"nms164"+".NMSChairsArrow");
				return;
			}
		}
		throw new NMSAccessException("ChairsReloaded is not compatible with your server version");
	}

	public Arrow spawnArrow(Location location) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		World world = location.getWorld();
		Constructor<?> ct = nmsArrowClass.getConstructor(world.getClass());
		NMSChairsArrowInterface vehiclearrow = (NMSChairsArrowInterface) ct.newInstance(world);
		vehiclearrow.setArrowLocation(location);
		vehiclearrow.addToWorld();
		vehiclearrow.setBukkitEntity(Bukkit.getServer());
		return vehiclearrow.getBukkitArrow();
	}

}

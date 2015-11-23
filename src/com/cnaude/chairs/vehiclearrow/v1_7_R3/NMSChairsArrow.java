package com.cnaude.chairs.vehiclearrow.v1_7_R3;

import net.minecraft.server.v1_7_R3.EntityArrow;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;

public class NMSChairsArrow extends EntityArrow {

	public NMSChairsArrow(CraftWorld cworld, Location location) {
		super(cworld.getHandle());
		setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		world.addEntity(this);
		bukkitEntity = new CraftChairsArrow((CraftServer) Bukkit.getServer(), this);
	}

	@Override
	public void h() {
	}

}

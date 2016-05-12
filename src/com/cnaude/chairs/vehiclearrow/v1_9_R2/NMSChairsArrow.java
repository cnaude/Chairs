package com.cnaude.chairs.vehiclearrow.v1_9_R2;

import net.minecraft.server.v1_9_R2.EntityTippedArrow;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;

public class NMSChairsArrow extends EntityTippedArrow {

	public NMSChairsArrow(CraftWorld cworld, Location location) {
		super(cworld.getHandle());
		setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		world.addEntity(this);
		bukkitEntity = new CraftChairsArrow((CraftServer) Bukkit.getServer(), this);
	}

	@Override
	public void m() {
	}

}

package com.cnaude.chairs.vehiclearrow.nms178;

import net.minecraft.server.v1_7_R3.EntityArrow;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.entity.Arrow;

import com.cnaude.chairs.vehiclearrow.NMSChairsArrowInterface;

public class NMSChairsArrow extends EntityArrow implements NMSChairsArrowInterface {

	public NMSChairsArrow(CraftWorld cworld) {
		super(cworld.getHandle());
	}

	@Override
	public void h() {
	}

	@Override
	public void setBukkitEntity(Server server) {
		bukkitEntity = new CraftChairsArrow((CraftServer) server, this);
	}

	@Override
	public Arrow getBukkitArrow() {
		return (Arrow) bukkitEntity;
	}

	@Override
	public void setArrowLocation(Location location) {
		setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

	@Override
	public void addToWorld() {
		world.addEntity(this);
	}

}

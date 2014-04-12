package com.cnaude.chairs.vehiclearrow;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Arrow;

public interface NMSChairsArrowInterface {

	public void setBukkitEntity(Server server);

	public Arrow getBukkitArrow();

	public void setArrowLocation(Location location);

	public void addToWorld();

}

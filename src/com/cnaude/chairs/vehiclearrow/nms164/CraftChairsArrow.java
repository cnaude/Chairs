package com.cnaude.chairs.vehiclearrow.nms164;

import net.minecraft.server.v1_6_R3.EntityArrow;

import org.bukkit.craftbukkit.v1_6_R3.CraftServer;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

import com.cnaude.chairs.api.ChairsAPI;

public class CraftChairsArrow extends CraftArrow implements Vehicle {

	public CraftChairsArrow(CraftServer server, EntityArrow entity) {
		super(server, entity);
	}

	@Override
	public void remove() {
		Entity passenger = getPassenger();
		if (passenger != null && passenger instanceof Player) {
			if (ChairsAPI.isSitting((Player) passenger)) {
				return;
			}
		}
		super.remove();
	}

}

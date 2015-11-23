package com.cnaude.chairs.vehiclearrow.v1_8_R2;

import net.minecraft.server.v1_8_R2.EntityArrow;

import org.bukkit.craftbukkit.v1_8_R2.CraftServer;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftArrow;
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

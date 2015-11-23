package com.cnaude.chairs.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.cnaude.chairs.core.PlayerSitData;

public class ChairsAPI {

	private static PlayerSitData pdata;
	protected static void init(PlayerSitData pdata) {
		ChairsAPI.pdata = pdata;
	}

	public static boolean isSitting(Player player) {
		return pdata.isSitting(player);
	}

	public static boolean isBlockOccupied(Block block) {
		return pdata.isBlockOccupied(block);
	}

	public static Player getBlockOccupiedBy(Block block) {
		return pdata.getPlayerOnChair(block);
	}

	public static boolean sit(Player player, Block blocktouccupy, Location sitlocation) {
		return pdata.sitPlayer(player, blocktouccupy, sitlocation);
	}

	public static void unsit(Player player) {
		pdata.unsitPlayerForce(player);
	}

}

package com.cnaude.chairs.core;

import org.bukkit.Material;

public class ChairBlock {

	private Material mat;
	private double sitHeight;

	public ChairBlock(Material m, double s) {
		mat = m;
		sitHeight = s;
	}

	public Material getMat() {
		return mat;
	}

	public double getSitHeight() {
		return sitHeight;
	}

}

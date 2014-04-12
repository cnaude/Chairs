package com.cnaude.chairs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cnaude.chairs.core.Chairs;

public class ChairsCommand implements CommandExecutor {

	private final Chairs plugin;
	public ChairsIgnoreList ignoreList;

	public ChairsCommand(Chairs instance, ChairsIgnoreList ignoreList) {
		this.plugin = instance;
		this.ignoreList = ignoreList;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}
		if (args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("chairs.reload") || !(sender instanceof Player)) {
				plugin.loadConfig();
				if (plugin.sitHealEnabled) {
					plugin.chairEffects.restartHealing();
				} else {
					plugin.chairEffects.cancelHealing();
				}
				if (plugin.sitPickupEnabled) {
					plugin.chairEffects.restartPickup();
				} else {
					plugin.chairEffects.cancelPickup();
				}
				sender.sendMessage(plugin.msgReloaded);
			} else {
				sender.sendMessage(plugin.msgNoPerm);
			}
		}
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args[0].equalsIgnoreCase("on")) {
				if (p.hasPermission("chairs.self")) {
					ignoreList.removePlayer(p.getName());
					p.sendMessage(plugin.msgEnabled);
				} else {
					p.sendMessage(plugin.msgNoPerm);
				}
			}
			if (args[0].equalsIgnoreCase("off")) {
				if (p.hasPermission("chairs.self")) {
					ignoreList.addPlayer(p.getName());
					p.sendMessage(plugin.msgDisabled);
				} else {
					p.sendMessage(plugin.msgNoPerm);
				}
			}
		}
		return true;
	}

}

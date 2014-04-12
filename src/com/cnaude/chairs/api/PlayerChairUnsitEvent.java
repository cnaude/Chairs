package com.cnaude.chairs.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerChairUnsitEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private boolean canbecancelled = true;

	public PlayerChairUnsitEvent(Player who, boolean canbecancelled) {
		super(who);
		this.canbecancelled = canbecancelled;
	}

	public boolean canBeCancelled() {
		return canbecancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}

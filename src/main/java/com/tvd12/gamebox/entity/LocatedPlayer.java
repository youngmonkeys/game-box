package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.constant.IPlayerRole;
import com.tvd12.gamebox.constant.IPlayerStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
public class LocatedPlayer extends Player {

	@Setter
	protected int location;
	@Setter
	protected IPlayerRole role;
	@Setter
	protected IPlayerStatus status;

	public LocatedPlayer(String name) {
		super(name);
	}
	
	protected LocatedPlayer(Builder<?> builder) {
		super(builder);
	}
	
	public static Builder<?> builder() {
		return new Builder<>();
	}

	public static class Builder<B extends Builder<B>> extends Player.Builder<B> {
		
		@Override
		protected Player newProduct() {
			return new LocatedPlayer(this);
		}
		
	}
	
}

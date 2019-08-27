package com.tvd12.gamebox.constant;

import com.tvd12.ezyfox.util.EzyEnums;

import lombok.Getter;

public enum PlayerRole implements IPlayerRole {

	MASTER(1),
	SPECTATOR(2),
	PLAYER(3),
	NPC(4);
	
	@Getter
	private int id;
	
	private PlayerRole(int id) {
		this.id = id;
	}
	
	@Override
	public String getName() {
		return toString();
	}
	
	public static PlayerRole valueOf(int id) {
		return EzyEnums.valueOf(values(), id);
	}
	
}

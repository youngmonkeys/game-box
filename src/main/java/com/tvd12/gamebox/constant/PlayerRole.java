package com.tvd12.gamebox.constant;

import java.util.Map;

import com.tvd12.ezyfox.util.EzyEnums;

import lombok.Getter;

public enum PlayerRole implements IPlayerRole {

	NULL(0),
	MASTER(1),
	SPECTATOR(2),
	PLAYER(3),
	NPC(4);
	
	@Getter
	private int id;
	
	private static final Map<Integer, PlayerRole> ROLE_BY_ID =
			EzyEnums.enumMapInt(PlayerRole.class);
	
	private PlayerRole(int id) {
		this.id = id;
	}
	
	@Override
	public String getName() {
		return toString();
	}
	
	public static PlayerRole valueOf(int id) {
		return ROLE_BY_ID.getOrDefault(Integer.valueOf(id), NULL);
	}
	
}

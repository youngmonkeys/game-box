package com.tvd12.gamebox.constant;

import com.tvd12.ezyfox.util.EzyEnums;

import lombok.Getter;

public enum RoomStatus implements IRoomStatus {

	WAITING(1),
	PLAYING(2),
	FINISHING(3);
	
	@Getter
	private int id;
	
	private RoomStatus(int id) {
		this.id = id;
	}
	
	@Override
	public String getName() {
		return toString();
	}
	
	public static RoomStatus valueOf(int id) {
		return EzyEnums.valueOf(values(), id);
	}
	
}

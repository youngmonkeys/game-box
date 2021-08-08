package com.tvd12.gamebox.entity;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyLoggable;

public class MMOVirtualWorld extends EzyLoggable {
	
	private final MMORoomGroup[] roomGroups;
	private final int mmoRoomGroupCount;
	private final int timeTickMillis;
	
	public MMOVirtualWorld(Builder builder) {
		this.mmoRoomGroupCount = builder.mmoRoomGroupCount;
		this.timeTickMillis = builder.timeTickMillis;
		roomGroups = new MMORoomGroup[this.mmoRoomGroupCount];
		this.start();
	}
	
	private void start() {
		for (int i = 0; i < mmoRoomGroupCount; i++) {
			MMORoomGroup group = MMORoomGroup.builder()
					.timeTickMillis(timeTickMillis)
					.build();
			roomGroups[i] = group;
		}
	}
	
	private MMORoomGroup getRoomGroupByRoomId(long roomId) {
		int roomGroupIndex = (int) (roomId % mmoRoomGroupCount);
		return roomGroups[roomGroupIndex];
	}
	
	public void addRoom(MMORoom room) {
		MMORoomGroup group = getRoomGroupByRoomId(room.getId());
		group.addRoom(room);
	}
	
	public void removeRoom(MMORoom room) {
		MMORoomGroup group = getRoomGroupByRoomId(room.getId());
		group.removeRoom(room);
	}
	
	public MMORoom getRoom(long roomId) {
		MMORoomGroup group = getRoomGroupByRoomId(roomId);
		return group.getRoom(roomId);
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements EzyBuilder<MMOVirtualWorld> {
		private Integer timeTickMillis;
		private Integer mmoRoomGroupCount;
		
		public Builder mmoRoomGroupCount(int mmoRoomGroupCount) {
			this.mmoRoomGroupCount = mmoRoomGroupCount;
			return this;
		}
		
		public Builder timeTickMillis(int timeTickMillis) {
			this.timeTickMillis = timeTickMillis;
			return this;
		}
		
		protected void preBuild() {
			if (this.mmoRoomGroupCount == null) {
				this.mmoRoomGroupCount = 2 * Runtime.getRuntime().availableProcessors();
			}
			
			if (this.timeTickMillis == null) {
				this.timeTickMillis = 100;
			}
		}
		
		@Override
		public MMOVirtualWorld build() {
			preBuild();
			
			return new MMOVirtualWorld(this);
		}
		
	}
}

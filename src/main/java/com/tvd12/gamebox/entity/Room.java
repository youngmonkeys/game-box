package com.tvd12.gamebox.entity;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyEquals;
import com.tvd12.ezyfox.util.EzyHashCodes;
import com.tvd12.gamebox.constant.IRoomStatus;
import com.tvd12.gamebox.constant.RoomStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicLong;

@Getter
public class Room {
	
	protected final long id;
	protected final String name;
	
	@Setter
	protected String password;
	@Setter
	protected IRoomStatus status = RoomStatus.WAITING;
	
	protected final static String NAME_PREFIX = "Room#";
	
	protected Room(Builder<?> builder) {
		this.id = builder.id;
		this.name = builder.name;
	}
	
	@Override
	public boolean equals(Object obj) {
		return new EzyEquals<Room>()
				.function(t -> t.id)
				.isEquals(this, obj);
	}
	
	@Override
	public int hashCode() {
		return new EzyHashCodes()
				.append(id)
				.toHashCode();
	}
	
	public static Builder<?> builder() {
		return new Builder<>();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append("(")
				.append("name = ").append(name)
				.append(", id = ").append(id)
				.append(")")
				.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static class Builder<B extends Builder<B>> implements EzyBuilder<Room> {

		protected Long id;
		protected String name;
		
		protected static final AtomicLong ID_GENTOR = new AtomicLong(0);
		
		public B id(long id) {
			this.id = id;
			return (B)this;
		}
		
		public B name(String name) {
			this.name = name;
			return (B)this;
		}
		
		@Override
		public Room build() {
			if(id == null)
				this.id = ID_GENTOR.incrementAndGet();
			if(name == null)
				this.name = NAME_PREFIX + id;
			preBuild();
			return newProduct();
		}
		
		protected void preBuild() {
		}
		
		protected Room newProduct() {
			return new Room(this);
		}
	}
}

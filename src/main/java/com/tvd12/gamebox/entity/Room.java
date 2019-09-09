package com.tvd12.gamebox.entity;

import java.util.concurrent.atomic.AtomicLong;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.util.EzyEquals;
import com.tvd12.ezyfox.util.EzyHashCodes;
import com.tvd12.ezyfoxserver.wrapper.EzyDefaultUserManager;
import com.tvd12.ezyfoxserver.wrapper.EzyUserManager;
import com.tvd12.gamebox.constant.IRoomStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Room {
	
	protected final long id;
	protected final String name;
	protected final EzyUserManager userManager;
	
	@Setter
	protected String password;
	@Setter
	protected IRoomStatus status;
	
	protected final static String NAME_PREFIX = "Room#";
	
	protected Room(Builder<?> builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.userManager = builder.userManager;
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
		protected EzyUserManager userManager;
		
		protected static final AtomicLong ID_GENTOR = new AtomicLong(0);
		
		public B id(long id) {
			this.id = id;
			return (B)this;
		}
		
		public B name(String name) {
			this.name = name;
			return (B)this;
		}
		
		public B defaultUserManager(int maxUser) {
			this.userManager = new EzyDefaultUserManager(maxUser);
			return (B)this;
		}
		
		public B userManager(EzyUserManager userManager) {
			this.userManager = userManager;
			return (B)this;
		}
		
		@Override
		public Room build() {
			if(id == null)
				this.id = ID_GENTOR.incrementAndGet();
			if(name == null)
				this.name = NAME_PREFIX + id;
			if(userManager == null)
				this.userManager = new EzyDefaultUserManager(999);
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

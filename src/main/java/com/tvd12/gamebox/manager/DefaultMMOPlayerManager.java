package com.tvd12.gamebox.manager;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.entity.MMOPlayer;

public class DefaultMMOPlayerManager extends DefaultPlayerManager<MMOPlayer> {
	
	public DefaultMMOPlayerManager() {
		super();
	}
	
	public DefaultMMOPlayerManager(int maxPlayer) {
		super(maxPlayer);
	}
	
	public DefaultMMOPlayerManager(Builder<?, ?> builder) {
		super(builder);
	}
}

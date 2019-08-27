package com.tvd12.gamebox.manager;

import java.util.List;
import java.util.function.Predicate;

import com.tvd12.ezyfox.function.EzyPredicates;
import com.tvd12.gamebox.entity.LocatedPlayer;

public interface LocatedPlayerManager {

	void setMaster(LocatedPlayer master);
	
	LocatedPlayer setNewMaster();
	
	LocatedPlayer getMaster();
	
	void setSpeakinger(LocatedPlayer speakinger);
	
	LocatedPlayer getSpeakinger();
	
	LocatedPlayer getPlayer(int location);
	
	List<LocatedPlayer> getPlayerList();
	
	void addPlayer(LocatedPlayer player, int location);
	
	void removePlayer(int location);
	
	LocatedPlayer nextOf(LocatedPlayer player, Predicate<LocatedPlayer> condition);
	
	default LocatedPlayer nextOf(LocatedPlayer player) {
		return nextOf(player, EzyPredicates.alwayTrue());
	}
	
}

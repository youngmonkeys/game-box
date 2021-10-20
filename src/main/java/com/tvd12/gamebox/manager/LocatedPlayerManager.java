package com.tvd12.gamebox.manager;

import com.tvd12.ezyfox.function.EzyPredicates;
import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.util.ReadOnlyCollection;
import com.tvd12.gamebox.util.ReadOnlySet;

import java.util.function.Predicate;

public interface LocatedPlayerManager {

	void setMaster(LocatedPlayer master);
	
	LocatedPlayer setNewMaster();
	
	LocatedPlayer getMaster();
	
	void setSpeakinger(LocatedPlayer speakinger);
	
	LocatedPlayer getSpeakinger();
	
	LocatedPlayer getPlayer(int location);
	
	ReadOnlyCollection<LocatedPlayer> getPlayerCollection();
	
	void addPlayer(LocatedPlayer player, int location);
	
	void removePlayer(int location);
	
	LocatedPlayer nextOf(LocatedPlayer player, Predicate<LocatedPlayer> condition);
	
	ReadOnlySet<String> getPlayerNames();

	boolean containsPlayer(String username);
	
	int getPlayerCount();
	
	boolean isEmpty();
	
	default LocatedPlayer nextOf(LocatedPlayer player) {
		return nextOf(player, EzyPredicates.alwayTrue());
	}
	
}

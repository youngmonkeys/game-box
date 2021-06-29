package com.tvd12.gamebox.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.exception.LocationNotAvailableException;

import lombok.Getter;
import lombok.Setter;

public class DefaultLocatedPlayerManager 
		extends EzyLoggable 
		implements LocatedPlayerManager {

	@Getter
	@Setter
	protected LocatedPlayer master;
	@Getter
	@Setter
	protected LocatedPlayer speakinger;
	protected final LinkedList<LocatedPlayer> playerList = new LinkedList<>();
	protected final Map<String, LocatedPlayer> playersByName = newPlayersByNameMap();
	protected final Map<Integer, LocatedPlayer> playersByLocation = newPlayersByLocationsMap();
	
	@Override
	public LocatedPlayer getPlayer(int location) {
		LocatedPlayer player = playersByLocation.get(location);
		return player;
	}
	
	@Override
	public List<LocatedPlayer> getPlayerList() {
		return playerList;
	}

	@Override
	public void addPlayer(LocatedPlayer player, int location) {
		LocatedPlayer current = playersByLocation.get(location);
		if(current != null)
			throw new LocationNotAvailableException("location: " + location + " has owned by: " + current.getName());
		playersByLocation.put(location, player);
		playersByName.put(player.getName(), player);
		playerList.add(player);
	}

	@Override
	public void removePlayer(int location) {
		LocatedPlayer removed = playersByLocation.remove(location);
		if(removed != null) {
			playerList.remove(removed);
			playersByName.remove(removed.getName());
		}
	}
	
	@Override
	public LocatedPlayer setNewMaster() {
		master = nextOf(master);
		return master;
	}
	
	@Override
	public LocatedPlayer nextOf(LocatedPlayer player, Predicate<LocatedPlayer> condition) {
		if(player == null) 
			return null;
		int maxInt = Integer.MAX_VALUE;
		int maxDistance = 0;
		int minDistance = -maxInt;
		int currentDistance = maxInt - player.getLocation();
		LocatedPlayer answer = null;
		for(LocatedPlayer p : playerList) {
			LocatedPlayer beforeAnswer = answer;
			int beforeMaxDistance = maxDistance;
			int beforeMinDistance = minDistance;
			if(player.equals(p))
				continue;
			LocatedPlayer accept = null;
			int nextDistance = maxInt - p.getLocation();
			int distance = nextDistance - currentDistance;
			if(distance > 0) {
				if(distance > maxDistance) {
					accept = p;
					maxDistance = distance;
				}
			}
			else if(distance > minDistance) {
				accept = p;
				minDistance = distance;
			}
			if(accept != null && condition.test(accept))
				answer = accept;
			if(answer == beforeAnswer) {
				maxDistance = beforeMaxDistance;
				minDistance = beforeMinDistance;
			}
		}
		return answer;
	}
	
	@Override
	public List<String> getPlayerNames() {
		return new ArrayList<>(playersByName.keySet());
	}
	
	@Override
	public int getPlayerCount() {
		return playersByLocation.size();
	}

	@Override
	public boolean containsPlayer(String username) {
		return playersByName.containsKey(username);
	}

	@Override
	public boolean isEmpty() {
		return playersByLocation.isEmpty();
	}
	
    protected Map<String, LocatedPlayer> newPlayersByNameMap() {
		return new HashMap<>();
	}
	
	protected Map<Integer, LocatedPlayer> newPlayersByLocationsMap() {
		return new HashMap<>();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append("(")
					.append("master: ").append(master).append(", ")
					.append("speakinger: ").append(speakinger).append(", ")
					.append("playersByLocation: ").append(playersByLocation)
				.append(")")
				.toString();
	}

}

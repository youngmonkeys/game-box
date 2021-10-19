package com.tvd12.gamebox.manager;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.exception.LocationNotAvailableException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

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
	protected final NavigableMap<Integer, LocatedPlayer> playersByLocation = newPlayersByLocationsMap();
	
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
		
		int currentLocation = player.getLocation();
		LocatedPlayer leftPlayer = find(currentLocation, condition, playersByLocation::lowerEntry);
		LocatedPlayer rightPlayer = find(currentLocation, condition, playersByLocation::higherEntry);
		
		return getCloserPlayer(leftPlayer, rightPlayer, currentLocation);
	}
	
	private LocatedPlayer getCloserPlayer(LocatedPlayer left, LocatedPlayer right, int location) {
		if (left == null && right == null) return null;
		if (left == null) return right;
		if (right == null) return left;
		return (location - left.getLocation()) < (right.getLocation() - location) ?
				left : right;
	}
	
	private LocatedPlayer find(int currentLocation, Predicate<LocatedPlayer> condition,
	                           Function<Integer, Map.Entry<Integer, LocatedPlayer>> function) {
		Map.Entry<Integer, LocatedPlayer> next = function.apply(currentLocation);
		while (next != null && !condition.test(next.getValue())) {
			next = function.apply(next.getKey());
		}
		return next != null ? next.getValue() : null;
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
	
	protected NavigableMap<Integer, LocatedPlayer> newPlayersByLocationsMap() {
		return new TreeMap<>();
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

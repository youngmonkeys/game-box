package com.tvd12.gamebox.manager;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.exception.LocationNotAvailableException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
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
		Integer leftLocation = playersByLocation.lowerKey(currentLocation);
		Integer rightLocation = playersByLocation.higherKey(currentLocation);
		
		while ((leftLocation != null) || (rightLocation != null)) {
			LocatedPlayer leftPlayer = (leftLocation != null) ? playersByLocation.get(leftLocation) : null;
			LocatedPlayer rightPlayer = (rightLocation != null) ? playersByLocation.get(rightLocation) : null;
			
			boolean isLeftCloser = (leftLocation != null) && (rightLocation != null) &&
					((currentLocation - leftLocation) < (rightLocation - currentLocation));
			
			if ((leftLocation != null) && (rightLocation != null) &&
					condition.test(leftPlayer) && condition.test(rightPlayer)) {
				return isLeftCloser
						? leftPlayer : rightPlayer;
			}
			if ((leftLocation != null) && condition.test(leftPlayer)) {
				if (isLeftCloser || (rightLocation == null)) {
					return leftPlayer;
				}
				rightLocation = playersByLocation.higherKey(rightLocation);
				continue;
			}
			if ((rightLocation != null) && condition.test(rightPlayer)) {
				if (!isLeftCloser || (leftLocation == null)) {
					return rightPlayer;
				}
				leftLocation = playersByLocation.lowerKey(leftLocation);
				continue;
			}
			if (leftLocation != null) leftLocation = playersByLocation.lowerKey(leftLocation);
			if (rightLocation != null) rightLocation = playersByLocation.higherKey(rightLocation);
		}
		return null;
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

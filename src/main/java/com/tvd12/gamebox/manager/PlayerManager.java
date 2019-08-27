package com.tvd12.gamebox.manager;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;

import com.tvd12.gamebox.entity.Player;

public interface PlayerManager<P extends Player> {

	/**
	 * Get user by id
	 * 
	 * @param username the user name
	 * @return the user
	 */
	P getPlayer(String username);
	
	/**
	 * Get players as list
	 * 
	 * @return the user as list
	 */
	List<P> getPlayerList();
	
	/**
	 * Get players as list
	 * 
	 * @param predicate the predicate to test
	 * @return the player list
	 */
	List<P> getPlayerList(Predicate<P> predicate);
	
	/**
	 * Get list of player name
	 * 
	 * @return the list of player name
	 */
	List<String> getPlayerNames();
	
	/**
	 * Get count of users
	 * 
	 * @return count of users
	 */
	int getPlayerCount();
	
	/**
	 * Get max users count
	 * 
	 * @return the max users count
	 */
	int getMaxPlayers();
	
	/**
	 * available to add user or not
	 * 
	 * @return available to add user or not
	 */
	boolean available();
	
	/**
	 * Check whether contains user or not
	 * 
	 * @param username the user name
	 * @return true or false
	 */
	boolean containsPlayer(String username);
	
	/**
	 * add player
	 * 
	 * @param player player to add
	 * @param failIfAdded throw an exception if user has added
	 */
	void addPlayer(P player, boolean failIfAdded);
	
	/**
	 * add players
	 * 
	 * @param players players to add
	 * @param failIfAdded throw an exception if user has added
	 */
	void addPlayers(Collection<P> players, boolean failIfAdded);
	
	/**
	 * Remove player
	 * 
	 * @param player the user
	 */
	P removePlayer(P player);
	
	/**
	 * remove collection of player
	 * 
	 * @param players the player to remove
	 */
	void removePlayers(Collection<P> players);
	
	/**
	 * Get lock mapped to username
	 * 
	 * @param username the username
	 * @return the lock
	 */
	Lock getLock(String username);
	
	/**
	 * Remove lock mapped to username
	 * 
	 * @param username the username
	 */
	void removeLock(String username);
	
	/**
	 * clear all user
	 */
	void clear();
	
	/**
	 * count player
	 * 
	 * @param tester the test function
	 * @return the player count
	 */
	int countPlayers(Predicate<P> tester);
	
	/**
	 * filter player to list
	 * 
	 * @param tester the test function
	 * @return list of filtered player
	 */
	List<P> filterPlayers(Predicate<P> tester);
	
	/**
	 * check is empty 
	 * 
	 * @return empty or not
	 */
	public boolean isEmpty();
	
	/**
	 * Check whether contains user or not
	 * 
	 * @param player the user
	 * @return true or false
	 */
	default boolean containsPlayer(P player) {
		return containsPlayer(player.getName());
	}
	
	/**
	 * add player
	 * 
	 * @param player player to add
	 * @param failIfAdded throw an exception if user has added
	 */
	default void addPlayer(P player) {
		addPlayer(player, true);
	}
	
	/**
	 * add players
	 * 
	 * @param players players to add
	 * @param failIfAdded throw an exception if user has added
	 */
	default void addPlayers(Collection<P> players) {
		addPlayers(players, true);
	}
	
	/**
	 * Remove user byte name
	 * 
	 * @param username the user name
	 */
	default P removePlayer(String username) {
		return removePlayer(getPlayer(username));
	}
	
}

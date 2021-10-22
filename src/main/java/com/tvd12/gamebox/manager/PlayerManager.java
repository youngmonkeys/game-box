package com.tvd12.gamebox.manager;

import com.tvd12.gamebox.entity.Player;
import com.tvd12.gamebox.util.ReadOnlyCollection;
import com.tvd12.gamebox.util.ReadOnlyList;
import com.tvd12.gamebox.util.ReadOnlySet;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface PlayerManager<P extends Player> {

    /**
     * Get player by id.
     *
     * @param username
     *         the player's name
     * @return the player
     */
    P getPlayer(String username);

    /**
     * Get player by id.
     *
     * @param username
     *         the player's name
     * @param playerSupplier
     *         if the player's not existed, we will create a new one
     * @return the player
     */
    P getPlayer(String username, Supplier<P> playerSupplier);

    /**
     * Get players as a collection.
     *
     * @return the player collection
     */
    ReadOnlyCollection<P> getPlayerCollection();

    /**
     * Get player list to buffer.
     *
     * @param buffer
     *         buffer to get player list
     */
    void getPlayerList(List<P> buffer);


    /**
     * Get players as a ReadOnlyList.
     *
     * @param predicate
     *         the predicate to test
     * @return the read-only player list
     */
    ReadOnlyList<P> getPlayerList(Predicate<P> predicate);

    /**
     * Get list of player name.
     *
     * @return the list of player name
     */
    ReadOnlySet<String> getPlayerNames();

    /**
     * Get count of players.
     *
     * @return count of players
     */
    int getPlayerCount();

    /**
     * Get max players count.
     *
     * @return the max players count
     */
    int getMaxPlayer();

    /**
     * Is available to add player or not.
     *
     * @return available to add player or not
     */
    boolean available();

    /**
     * Check whether contains player or not.
     *
     * @param username
     *         the player's name
     * @return true or false
     */
    boolean containsPlayer(String username);


    /**
     * Check whether contains player or not.
     *
     * @param player
     *         the player
     * @return true or false
     */
    default boolean containsPlayer(P player) {
        return containsPlayer(player.getName());
    }

    /**
     * Add player.
     *
     * @param player
     *         player to add
     * @param failIfAdded
     *         throw an exception if player has added
     */
    void addPlayer(P player, boolean failIfAdded);

    /**
     * Add player.
     *
     * @param player
     *         player to add
     */
    default void addPlayer(P player) {
        addPlayer(player, true);
    }

    /**
     * Add players.
     *
     * @param players
     *         players to add
     * @param failIfAdded
     *         throw an exception if player has added
     */
    void addPlayers(Collection<P> players, boolean failIfAdded);

    /**
     * Add players.
     *
     * @param players
     *         players to add
     */
    default void addPlayers(Collection<P> players) {
        addPlayers(players, true);
    }

    /**
     * Remove player.
     *
     * @param player
     *         the player
     * @return removed player
     */
    P removePlayer(P player);

    /**
     * Remove player by name.
     *
     * @param username
     *         the player name
     * @return the removed player
     */
    default P removePlayer(String username) {
        return removePlayer(getPlayer(username));
    }

    /**
     * Remove collection of player.
     *
     * @param players
     *         the player to remove
     */
    void removePlayers(Collection<P> players);

    /**
     * Get lock mapped to player's name.
     *
     * @param username
     *         the player's name
     * @return the lock
     */
    Lock getLock(String username);

    /**
     * Remove lock mapped to player's name.
     *
     * @param username
     *         the player's name
     */
    void removeLock(String username);

    /**
     * Clear all user.
     */
    void clear();

    /**
     * Count player.
     *
     * @param tester
     *         the test function
     * @return the player count
     */
    int countPlayers(Predicate<P> tester);

    /**
     * Filter player to list.
     *
     * @param tester
     *         the test function
     * @return list of filtered player
     */
    List<P> filterPlayers(Predicate<P> tester);

    /**
     * Check is empty.
     *
     * @return empty or not
     */
    public boolean isEmpty();
}

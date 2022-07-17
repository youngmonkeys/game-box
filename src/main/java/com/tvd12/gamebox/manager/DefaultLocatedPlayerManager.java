package com.tvd12.gamebox.manager;

import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.exception.LocationNotAvailableException;
import com.tvd12.gamebox.exception.PlayerExistsException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DefaultLocatedPlayerManager implements LocatedPlayerManager {

    protected final Map<String, LocatedPlayer> playerByName
        = newPlayerByNameMap();
    protected final NavigableMap<Integer, LocatedPlayer> playerByLocation
        = newPlayerByLocationsMap();
    @Getter
    @Setter
    protected LocatedPlayer master;
    @Getter
    @Setter
    protected LocatedPlayer speakinger;

    @Override
    public LocatedPlayer getPlayer(int location) {
        return playerByLocation.get(location);
    }

    @Override
    public void addPlayer(LocatedPlayer player, int location) {
        LocatedPlayer current = playerByLocation.get(location);
        if (current != null) {
            throw new LocationNotAvailableException(
                "location: " + location + " has owned by: " + current.getName());
        }
        if (playerByName.containsKey(player.getName())) {
            throw new PlayerExistsException(player.getName());
        }
        player.setLocation(location);
        playerByLocation.put(location, player);
        playerByName.put(player.getName(), player);
    }

    @Override
    public LocatedPlayer removePlayer(int location) {
        LocatedPlayer removed = playerByLocation.remove(location);
        if (removed != null) {
            playerByName.remove(removed.getName());
        }
        return removed;
    }

    @Override
    public LocatedPlayer setNewMaster() {
        master = nextOf(master);
        return master;
    }

    @Override
    public List<String> getPlayerNames() {
        return new ArrayList<>(playerByName.keySet());
    }

    @Override
    public int getPlayerCount() {
        return playerByLocation.size();
    }

    @Override
    public boolean containsPlayer(String username) {
        return playerByName.containsKey(username);
    }

    @Override
    public boolean isEmpty() {
        return playerByLocation.isEmpty();
    }

    @Override
    public LocatedPlayer nextOf(LocatedPlayer player, Predicate<LocatedPlayer> condition) {
        if (player == null) {
            return null;
        }

        int currentLocation = player.getLocation();
        LocatedPlayer leftPlayer = find(currentLocation, condition, playerByLocation::lowerEntry);
        LocatedPlayer rightPlayer = find(
            currentLocation,
            condition,
            playerByLocation::higherEntry
        );

        return getCloserPlayer(leftPlayer, rightPlayer, currentLocation);
    }

    /**
     * Find the first player that is null or satisfies condition. Use function to determine the jump
     * direction
     *
     * @param currentLocation current considered location
     * @param condition       condition to test player
     * @param function        specify how to jump to next entry
     * @return the first player that satisfies condition or null
     */
    private LocatedPlayer find(
        int currentLocation,
        Predicate<LocatedPlayer> condition,
        Function<Integer, Entry<Integer, LocatedPlayer>> function
    ) {
        Entry<Integer, LocatedPlayer> next = function.apply(currentLocation);
        while (next != null && !condition.test(next.getValue())) {
            next = function.apply(next.getKey());
        }
        return next != null ? next.getValue() : null;
    }

    /**
     * Determine whether `leftPlayer` or `rightPlayer` is closer to `location`.
     *
     * @return closer player
     */
    private LocatedPlayer getCloserPlayer(LocatedPlayer left, LocatedPlayer right, int location) {
        if (left == null && right == null) {
            return null;
        }
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return (location - left.getLocation()) < (right.getLocation() - location) ? left : right;
    }


    @Override
    public LocatedPlayer rightOf(LocatedPlayer player, Predicate<LocatedPlayer> condition) {
        return findCircle(
            player.getLocation(),
            condition,
            playerByLocation::higherEntry,
            playerByLocation::firstEntry
        );
    }

    @Override
    public LocatedPlayer leftOf(LocatedPlayer player, Predicate<LocatedPlayer> condition) {
        return findCircle(
            player.getLocation(),
            condition,
            playerByLocation::lowerEntry,
            playerByLocation::lastEntry
        );
    }


    /**
     * Find the first player that satisfies condition (in circle, not in current position) Use
     * jumpFunction to determine the jump direction. Use anchorSupplier to determine which entry to
     * go to when reaching the end.
     *
     * @param currentLocation current considered location
     * @param condition       condition to test player
     * @param jumpFunction    specify how to jump to next entry
     * @param anchorSupplier  specify which entry to jump to
     *                        when reaching the end (next entry = null)
     * @return the first player that satisfies condition (in circle, not in current position)
     */
    private LocatedPlayer findCircle(
        int currentLocation,
        Predicate<LocatedPlayer> condition,
        Function<Integer, Entry<Integer, LocatedPlayer>> jumpFunction,
        Supplier<Entry<Integer, LocatedPlayer>> anchorSupplier
    ) {
        int nextLocation = currentLocation;
        Entry<Integer, LocatedPlayer> next;
        while (true) {
            next = jumpFunction.apply(nextLocation);
            if (next == null) {
                next = anchorSupplier.get();
            }
            if ((next == null) || (next.getKey() == currentLocation)) {
                return null;
            }
            if (condition.test(next.getValue())) {
                break;
            }
            nextLocation = next.getKey();
        }
        return next.getValue();
    }

    protected Map<String, LocatedPlayer> newPlayerByNameMap() {
        return new HashMap<>();
    }

    protected NavigableMap<Integer, LocatedPlayer> newPlayerByLocationsMap() {
        return new TreeMap<>();
    }

    @Override
    public String toString() {
        return "(" +
            "master: " + master + ", " +
            "speakinger: " + speakinger + ", " +
            "playerByLocation: " + playerByLocation +
            ")";
    }
}

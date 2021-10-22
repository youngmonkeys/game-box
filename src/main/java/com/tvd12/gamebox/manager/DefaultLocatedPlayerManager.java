package com.tvd12.gamebox.manager;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.exception.LocationNotAvailableException;
import com.tvd12.gamebox.util.ReadOnlyCollection;
import com.tvd12.gamebox.util.ReadOnlySet;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DefaultLocatedPlayerManager
        extends EzyLoggable
        implements LocatedPlayerManager {

    @Getter
    @Setter
    protected LocatedPlayer master;
    @Getter
    @Setter
    protected LocatedPlayer speakinger;
    protected final Map<String, LocatedPlayer> playersByName = newPlayersByNameMap();
    protected final NavigableMap<Integer, LocatedPlayer> playersByLocation
            = newPlayersByLocationsMap();

    @Override
    public LocatedPlayer getPlayer(int location) {
        LocatedPlayer player = playersByLocation.get(location);
        return player;
    }

    @Override
    public ReadOnlyCollection<LocatedPlayer> getPlayerCollection() {
        return new ReadOnlyCollection<>(playersByLocation.values());
    }

    @Override
    public void addPlayer(LocatedPlayer player, int location) {
        LocatedPlayer current = playersByLocation.get(location);
        if (current != null) {
            throw new LocationNotAvailableException(
                    "location: " + location + " has owned by: " + current.getName());
        }
        playersByLocation.put(location, player);
        playersByName.put(player.getName(), player);
    }

    @Override
    public void removePlayer(int location) {
        LocatedPlayer removed = playersByLocation.remove(location);
        if (removed != null) {
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
        if (player == null) {
            return null;
        }

        int currentLocation = player.getLocation();
        LocatedPlayer leftPlayer = find(currentLocation, condition, playersByLocation::lowerEntry);
        LocatedPlayer rightPlayer = find(
                currentLocation,
                condition,
                playersByLocation::higherEntry
        );

        return getCloserPlayer(leftPlayer, rightPlayer, currentLocation);
    }

    @Override
    public LocatedPlayer rightOf(LocatedPlayer player, Predicate<LocatedPlayer> condition) {
        return findCircle(
                player.getLocation(),
                condition,
                playersByLocation::higherEntry,
                playersByLocation::firstEntry
        );
    }

    @Override
    public LocatedPlayer leftOf(LocatedPlayer player, Predicate<LocatedPlayer> condition) {
        return findCircle(
                player.getLocation(),
                condition,
                playersByLocation::lowerEntry,
                playersByLocation::lastEntry
        );
    }

    @Override
    public ReadOnlySet<String> getPlayerNames() {
        return new ReadOnlySet<>(playersByName.keySet());
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

    /**
     * Find the first player that is null or satisfies condition. Use function to determine the jump
     * direction
     *
     * @param currentLocation
     *         current considered location
     * @param condition
     *         condition to test player
     * @param function
     *         specify how to jump to next entry
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
     * Find the first player that satisfies condition (in circle, not in current position) Use
     * jumpFunction to determine the jump direction. Use anchorSupplier to determine which entry to
     * go to when reaching the end.
     *
     * @param currentLocation
     *         current considered location
     * @param condition
     *         condition to test player
     * @param jumpFunction
     *         specify how to jump to next entry
     * @param anchorSupplier
     *         specify which entry to jump to when reaching the end (next entry = null)
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
}

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

    void addPlayer(LocatedPlayer player, int location);

    void removePlayer(int location);

    LocatedPlayer nextOf(LocatedPlayer player, Predicate<LocatedPlayer> condition);

    default LocatedPlayer nextOf(LocatedPlayer player) {
        return nextOf(player, EzyPredicates.alwayTrue());
    }

    LocatedPlayer rightOf(LocatedPlayer player, Predicate<LocatedPlayer> condition);

    default LocatedPlayer rightOf(LocatedPlayer player) {
        return rightOf(player, EzyPredicates.alwayTrue());
    }

    LocatedPlayer leftOf(LocatedPlayer player, Predicate<LocatedPlayer> condition);

    default LocatedPlayer leftOf(LocatedPlayer player) {
        return leftOf(player, EzyPredicates.alwayTrue());
    }

    List<String> getPlayerNames();

    boolean containsPlayer(String username);

    int getPlayerCount();

    boolean isEmpty();
}

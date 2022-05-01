package com.tvd12.gamebox.handler;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;

import java.util.List;

@SuppressWarnings("AbbreviationAsWordInName")
public interface MMORoomUpdatedHandler {

    void onRoomUpdated(MMORoom room, List<MMOPlayer> players);
}

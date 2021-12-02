package com.tvd12.gamebox.handler;

import java.util.List;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;

@SuppressWarnings("AbbreviationAsWordInName")
public interface MMORoomUpdatedHandler {
    
    void onRoomUpdated(MMORoom room, List<MMOPlayer> players);
}

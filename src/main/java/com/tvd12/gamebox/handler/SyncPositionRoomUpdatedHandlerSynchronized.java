package com.tvd12.gamebox.handler;

import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import com.tvd12.ezyfoxserver.support.factory.EzyResponseFactory;
import com.tvd12.gamebox.constant.Commands;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SyncPositionRoomUpdatedHandlerSynchronized implements MMORoomUpdatedHandler {

    @EzyAutoBind
    private EzyResponseFactory responseFactory;

    @Override
    public void onRoomUpdated(MMORoom room, List<MMOPlayer> players) {
        players.forEach(player -> {
            synchronized (player) {
                // Check if player's position or rotation is updated
                if (player.isStateChanged()) {
                    responseFactory.newArrayResponse()
                        .udpOrTcpTransport()
                        .command(Commands.SYNC_POSITION)
                        .param(player.getName())
                        .param(player.getPosition().toArray())
                        .param(player.getRotation().toArray())
                        .param(player.getClientTimeTick())
                        .usernames(player.getNearbyPlayerNames())
                        .execute();

                    player.setStateChanged(false);
                }
            }
        });
    }
}


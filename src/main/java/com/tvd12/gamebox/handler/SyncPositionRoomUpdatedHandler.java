package com.tvd12.gamebox.handler;

import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import com.tvd12.ezyfoxserver.support.factory.EzyResponseFactory;
import com.tvd12.gamebox.constant.Commands;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.manager.PlayerManager;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.gamebox.util.ReadOnlySet;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class SyncPositionRoomUpdatedHandler implements MMORoomUpdatedHandler {
	
	@Setter
	@EzyAutoBind
	private EzyResponseFactory responseFactory;
	
	@Override
	public void onRoomUpdated(MMORoom room) {
		PlayerManager<MMOPlayer> playerManager = room.getPlayerManager();
		playerManager.getPlayerList().forEach(player -> {
			// Check if player's position or rotation is updated
			if (player.isStateChanged()) {
				ReadOnlySet<String> nearbyPlayerNames = player.getNearbyPlayerNames();
				
				responseFactory.newArrayResponse()
						.udpTransport()
						.command(Commands.SYNC_POSITION)
						.param(player.getName())
						.param(Vec3.toArray(player.getPosition()))
						.param(Vec3.toArray(player.getRotation()))
						.param(player.getClientTimeTick())
						.usernames(nearbyPlayerNames.copyToList())
						.execute();
				
				player.setStateChanged(false);
			}
		});
	}
}

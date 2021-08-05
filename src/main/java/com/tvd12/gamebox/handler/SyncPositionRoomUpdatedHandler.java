package com.tvd12.gamebox.handler;

import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import com.tvd12.ezyfox.entity.EzyObject;
import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.ezyfoxserver.support.factory.EzyResponseFactory;
import com.tvd12.gamebox.constant.Commands;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.manager.PlayerManager;
import com.tvd12.gamebox.math.Vec3;

import java.util.ArrayList;
import java.util.List;

public class SyncPositionRoomUpdatedHandler implements MMORoomUpdateHandler {
	
	private static final float eps = 1e-8f;
	
	private List<String> nearbyPlayerNamesBuffer = new ArrayList<>();
	
	@EzyAutoBind
	private EzyResponseFactory responseFactory;
	
	@Override
	public void onRoomUpdated(MMORoom room) {
		PlayerManager<MMOPlayer> playerManager;
		synchronized (room) {
			playerManager = room.getPlayerManager();
		}
		for (MMOPlayer player : playerManager.getPlayerList()) {
			Vec3 newPosition = player.getPosition();
			Vec3 newRotation = player.getRotation();
			
			// Check if player's position or rotation is updated
			if (player.getStateChanged()) {
				nearbyPlayerNamesBuffer.clear();
				player.getNearbyPlayerNames(nearbyPlayerNamesBuffer);
				
				EzyObject data = EzyEntityFactory.newObjectBuilder()
						.append("playerName", player.getName())
						.append("position", newPosition)
						.append("rotation", newRotation)
						.build();
				
				responseFactory.newObjectResponse()
						.command(Commands.SYNC_POSITION)
						.data(data)
						.usernames(nearbyPlayerNamesBuffer)
						.execute();
				
				player.setStateChanged(false);
			}
		}
	}
}

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
import com.tvd12.gamebox.math.Vec3s;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class SyncPositionRoomUpdatedHandler implements MMORoomUpdatedHandler {
	
	@Setter
	@EzyAutoBind
	private EzyResponseFactory responseFactory;
	
	private List<String> nearbyPlayerNamesBuffer = new ArrayList<>();
	
	@Override
	public void onRoomUpdated(MMORoom room) {
		PlayerManager<MMOPlayer> playerManager = room.getPlayerManager();
		for (MMOPlayer player : playerManager.getPlayerList()) {
			// Check if player's position or rotation is updated
			if (player.isStateChanged()) {
				Vec3 newPosition = player.getPosition();
				Vec3 newRotation = player.getRotation();
				
				nearbyPlayerNamesBuffer.clear();
				player.getNearbyPlayerNames(nearbyPlayerNamesBuffer);
				
				responseFactory.newArrayResponse()
						.udpTransport()
						.command(Commands.SYNC_POSITION)
						.param(player.getName())
						.param(Vec3s.toArray(player.getPosition()))
						.param(Vec3s.toArray(player.getRotation()))
						.usernames(nearbyPlayerNamesBuffer)
						.execute();
				
				player.setStateChanged(false);
			}
		}
	}
}
package com.tvd12.gamebox.testing;

import com.tvd12.ezyfox.util.EzyNameAware;
import com.tvd12.ezyfoxserver.entity.EzySimpleUser;
import com.tvd12.ezyfoxserver.entity.EzyUser;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.handler.SyncPositionRoomUpdatedHandler;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class SyncPositionRoomUpdatedHandlerTest {
	
	@Test
	public void test() {
		// given
		SyncPositionRoomUpdatedHandler instance1 = new SyncPositionRoomUpdatedHandler();
		SyncPositionRoomUpdatedHandler instance2 = new SyncPositionRoomUpdatedHandler();
		
		MMORoom room = MMORoom.builder()
				.name("room")
				.distanceOfInterest(100.0D)
				.addRoomUpdatedHandler(instance1)
				.addRoomUpdatedHandler(instance2)
				.build();
		
		EzyUser user1 = new EzySimpleUser();
		((EzyNameAware) user1).setName("user1");
		MMOPlayer player1 = new MMOPlayer("player1");
		player1.setPosition(new Vec3(0, 0, 0));
		room.addUser(user1, player1);
		
		EzyUser user2 = new EzySimpleUser();
		((EzyNameAware) user2).setName("user2");
		MMOPlayer player2 = new MMOPlayer("player2");
		player2.setPosition(new Vec3(1, 1, 1));
		room.addUser(user2, player2);
		
		EzyUser user3 = new EzySimpleUser();
		((EzyNameAware) user3).setName("user3");
		MMOPlayer player3 = new MMOPlayer("player3");
		player3.setPosition(new Vec3(2, 2, 2));
		room.addUser(user3, player3);
		
		// when
		room.update();
		
		// then
	}
	
}

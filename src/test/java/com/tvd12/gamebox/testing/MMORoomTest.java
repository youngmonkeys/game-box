package com.tvd12.gamebox.testing;

import com.tvd12.ezyfox.util.EzyNameAware;
import com.tvd12.ezyfoxserver.entity.EzySimpleUser;
import com.tvd12.ezyfoxserver.entity.EzyUser;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;
import org.testng.annotations.Test;

public class MMORoomTest {
	
	@Test
	public void createRoomTest() {
		MMORoom room = MMORoom.builder()
				.defaultPlayerManager(5)
				.distanceOfInterest(100.0D)
				.name("room")
				.build();
		
		EzyUser user1 = new EzySimpleUser();
		((EzyNameAware) user1).setName("user1");
		MMOPlayer player1 = new MMOPlayer("player1");
		room.addUser(user1, player1);
		
		EzyUser user2 = new EzySimpleUser();
		((EzyNameAware) user2).setName("user2");
		MMOPlayer player2 = new MMOPlayer("player2");
		room.addUser(user2, player2);
		
		EzyUser user3 = new EzySimpleUser();
		((EzyNameAware) user3).setName("user3");
		MMOPlayer player3 = new MMOPlayer("player3");
		room.addUser(user3, player3);
		
		room.start();
	}
}

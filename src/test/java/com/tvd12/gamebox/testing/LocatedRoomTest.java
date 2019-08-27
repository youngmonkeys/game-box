package com.tvd12.gamebox.testing;

import org.testng.annotations.Test;

import com.tvd12.ezyfox.util.EzyNameAware;
import com.tvd12.ezyfoxserver.entity.EzySimpleUser;
import com.tvd12.ezyfoxserver.entity.EzyUser;
import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.entity.LocatedRoom;

public class LocatedRoomTest {
	
	@Test
	public void masterTest() {
		LocatedRoom room = LocatedRoom.builder()
				.defaultUserManager(5)
				.name("room")
				.build();
		room.setMaxSlots(5);
		EzyUser user1 = new EzySimpleUser();
		((EzyNameAware)(user1)).setName("user1");
		LocatedPlayer player1 = new LocatedPlayer("player1");
		room.addUser(user1, player1);
		
		EzyUser user2 = new EzySimpleUser();
		((EzyNameAware)(user2)).setName("user2");
		LocatedPlayer player2 = new LocatedPlayer("player2");
		room.addUser(user2, player2);
		
		EzyUser user3 = new EzySimpleUser();
		((EzyNameAware)(user3)).setName("user3");
		LocatedPlayer player3 = new LocatedPlayer("player3");
		room.addUser(user3, player3);
		
		room.getLocatedPlayerManager().setMaster(player2);
		
		System.out.println("\n\nnext master is: " + room.getLocatedPlayerManager().setNewMaster() + "\n\n");
	}
	
}

package com.tvd12.gamebox.testing;

import com.tvd12.ezyfox.util.EzyNameAware;
import com.tvd12.ezyfoxserver.entity.EzySimpleUser;
import com.tvd12.ezyfoxserver.entity.EzyUser;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.entity.MMORoomUpdateFinishedHandler;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import lombok.Getter;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class MMORoomTest {
	
	@Test
	public void updateMMORoomTest() {
		// given
		MMORoom room = MMORoom.builder()
				.defaultPlayerManager(5)
				.distanceOfInterest(100.0D)
				.name("room")
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
		List<String> buffer1 = new ArrayList<>();
		List<String> buffer2 = new ArrayList<>();
		List<String> buffer3 = new ArrayList<>();
		
		player1.getNearbyPlayerNames(buffer1);
		player2.getNearbyPlayerNames(buffer2);
		player3.getNearbyPlayerNames(buffer3);
		
		Asserts.assertEquals(buffer1.size(), 2);
		Asserts.assertEquals(buffer2.size(), 2);
		Asserts.assertEquals(buffer3.size(), 2);
	}
	
	@Test
	public void addRoomUpdateFinishedHandlerTest() {
		// given
		A aInstance1 = new A();
		A aInstance2 = new A();
		
		MMORoom room = MMORoom.builder()
				.defaultPlayerManager(5)
				.distanceOfInterest(100.0D)
				.name("room")
				.addRoomUpdateFinishedHandler(aInstance1)
				.addRoomUpdateFinishedHandler(aInstance2)
				.build();
		
		// when
		room.update();
		
		// then
		Asserts.assertEquals(aInstance1.isCalled(), true);
		Asserts.assertEquals(aInstance2.isCalled(), true);
	}
	
	public class A implements MMORoomUpdateFinishedHandler {
		@Getter
		private boolean isCalled = false;
		
		@Override
		public void onRoomUpdated(MMORoom room) {
			isCalled = true;
		}
	}
}

package com.tvd12.gamebox.testing;

import com.tvd12.ezyfox.util.EzyNameAware;
import com.tvd12.ezyfoxserver.entity.EzySimpleUser;
import com.tvd12.ezyfoxserver.entity.EzyUser;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.handler.MMORoomUpdateHandler;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.reflect.FieldUtil;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class MMORoomTest {
	
	@SuppressWarnings("unchecked")
	@Test
	public void updateMMORoomTest() {
		// given
		MMORoom room = MMORoom.builder()
				.distanceOfInterest(RandomUtil.randomSmallDouble())
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
		
		List<String> expectedNearbyPlayerNames1 =
				new ArrayList<>(((Map<String, MMOPlayer>) FieldUtil.getFieldValue(player1, "nearbyPlayers"))
						.keySet());
		List<String> expectedNearbyPlayerNames2 =
				new ArrayList<>(((Map<String, MMOPlayer>) FieldUtil.getFieldValue(player2, "nearbyPlayers"))
						.keySet());
		List<String> expectedNearbyPlayerNames3 =
				new ArrayList<>(((Map<String, MMOPlayer>) FieldUtil.getFieldValue(player3, "nearbyPlayers"))
						.keySet());
		
		Asserts.assertEquals(buffer1, expectedNearbyPlayerNames1);
		Asserts.assertEquals(buffer2, expectedNearbyPlayerNames2);
		Asserts.assertEquals(buffer3, expectedNearbyPlayerNames3);
	}
	
	@Test
	public void addRoomUpdatedHandlerTest() {
		// given
		A aInstance1 = mock(A.class);
		A aInstance2 = mock(A.class);
		
		MMORoom room = MMORoom.builder()
				.name("room")
				.addRoomUpdatedHandler(aInstance1)
				.addRoomUpdatedHandler(aInstance2)
				.build();
		
		// when
		room.update();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		room.update();
		
		// then
		verify(aInstance1, times(2)).onRoomUpdated(room);
		verify(aInstance2, times(2)).onRoomUpdated(room);
	}
	
	public class A implements MMORoomUpdateHandler {
		@Override
		public void onRoomUpdated(MMORoom room) {
		}
	}
}

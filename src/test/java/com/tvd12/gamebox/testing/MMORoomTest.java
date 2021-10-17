package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.exception.MaxPlayerException;
import com.tvd12.gamebox.handler.MMORoomUpdatedHandler;
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
		
		MMOPlayer player1 = new MMOPlayer("player1");
		player1.setPosition(new Vec3(0, 0, 0));
		room.addPlayer(player1);
		
		MMOPlayer player2 = new MMOPlayer("player2");
		player2.setPosition(new Vec3(1, 1, 1));
		room.addPlayer(player2);
		
		MMOPlayer player3 = new MMOPlayer("player3");
		player3.setPosition(new Vec3(2, 2, 2));
		room.addPlayer(player3);
		
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
				.distanceOfInterest(RandomUtil.randomSmallDouble())
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
	
	@Test
	public void maxPlayerTest() {
		// given
		MMORoom room = MMORoom.builder()
				.distanceOfInterest(RandomUtil.randomSmallDouble())
				.maxPlayer(2)
				.name("room")
				.build();
		
		// when
		MMOPlayer player1 = new MMOPlayer("player1");
		room.addPlayer(player1);
		
		MMOPlayer player2 = new MMOPlayer("player2");
		room.addPlayer(player2);
		
		MMOPlayer player3 = new MMOPlayer("player3");
		Throwable e = Asserts.assertThrows(() -> room.addPlayer(player3));
		
		// then
		Asserts.assertEquals(MaxPlayerException.class.toString(), e.getClass().toString());
	}
	
	public class A implements MMORoomUpdatedHandler {
		@Override
		public void onRoomUpdated(MMORoom room) {
		}
	}
}

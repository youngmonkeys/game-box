package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.entity.MMOVirtualWorld;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.reflect.FieldUtil;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class MMOVirtualWorldTest {
	
	@Test
	public void createMMOVirtualWorldTest() {
		// given
		int expectedMMORoomGroupCount = 10;
		int expectedTimeTickMillis = 100;
		
		// when
		MMOVirtualWorld world = MMOVirtualWorld.builder()
				.mmoRoomGroupCount(expectedMMORoomGroupCount)
				.timeTickMillis(expectedTimeTickMillis)
				.build();
		
		// then
		int timeTickMillis = FieldUtil.getFieldValue(world, "timeTickMillis");
		int mmoRoomGroupCount = FieldUtil.getFieldValue(world, "mmoRoomGroupCount");
		
		Asserts.assertEquals(expectedTimeTickMillis, timeTickMillis);
		Asserts.assertEquals(expectedMMORoomGroupCount, mmoRoomGroupCount);
		Asserts.assertNotNull(FieldUtil.getFieldValue(world, "roomGroups"));
	}
	
	@Test
	public void addAndGetRoomTest() {
		// given
		MMOVirtualWorld world = MMOVirtualWorld.builder().build();
		MMORoom expectedRoom = mock(MMORoom.class);
		long expectedRoomId = 1;
		when(expectedRoom.getId()).thenReturn(expectedRoomId);
		
		// when
		world.addRoom(expectedRoom);
		
		// then
		MMORoom room = world.getRoom(expectedRoomId);
		Asserts.assertEquals(expectedRoom, room);
	}
	
	@Test
	public void removeRoomTest() {
		// given
		MMOVirtualWorld world = MMOVirtualWorld.builder().build();
		MMORoom expectedRoom = mock(MMORoom.class);
		long expectedRoomId = 1;
		when(expectedRoom.getId()).thenReturn(expectedRoomId);
		world.addRoom(expectedRoom);
		
		// when
		world.removeRoom(expectedRoom);
		
		// then
		MMORoom room = world.getRoom(expectedRoomId);
		Asserts.assertNull(room);
		
	}
}

package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.entity.MMORoomGroup;
import com.tvd12.gamebox.manager.RoomManager;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.reflect.FieldUtil;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class MMORoomGroupTest {
	
	@Test
	public void createMMORoomGroupTest() throws InterruptedException {
		// given
		MMORoomGroup sut = MMORoomGroup.builder().timeTickMillies(100).build();
		
		// when
		Set<Thread> threads = Thread.getAllStackTraces().keySet();
		List<String> threadNames = threads.stream().map(Thread::getName).collect(Collectors.toList());
		
		// then
		Asserts.assertNotNull(FieldUtil.getFieldValue(sut, "timeTickMillis"));
		Asserts.assertNotNull(FieldUtil.getFieldValue(sut, "roomsBuffer"));
		Asserts.assertNotNull(FieldUtil.getFieldValue(sut, "roomManager"));
		
		Asserts.assertEquals(threadNames.contains("game-box-mmo-room-group-1"), true);
	}
	
	@Test
	public void addRoomTest() throws InterruptedException {
		// given
		MMORoomGroup sut = MMORoomGroup.builder().timeTickMillies(100).build();
		MMORoom room = mock(MMORoom.class);
		sut.addRoom(room);
		doNothing().when(room).update();
		
		// when
		Thread.sleep(1000);
		
		// then
		verify(room, atLeastOnce()).update();
	}
	
	@Test
	public void removeRoomTest() throws InterruptedException {
		// given
		MMORoomGroup sut = MMORoomGroup.builder().timeTickMillies(100).build();
		MMORoom room1 = mock(MMORoom.class);
		MMORoom room2 = mock(MMORoom.class);
		
		when(room1.getName()).thenReturn("room1");
		when(room1.getId()).thenReturn(0L);
		when(room2.getName()).thenReturn("room2");
		when(room1.getId()).thenReturn(1L);
		
		sut.addRoom(room1);
		sut.addRoom(room2);
		doNothing().when(room1).update();
		doNothing().when(room2).update();
		
		Thread.sleep(1000);
		
		// when
		sut.removeRoom(room1);
		
		// then
		RoomManager<MMORoom> roomManager = FieldUtil.getFieldValue(sut, "roomManager");
		
		List<MMORoom> expectedRoomList = new ArrayList<>();
		expectedRoomList.add(room2);
		
		List<MMORoom> roomsBuffer = new ArrayList<>();
		roomManager.getRoomList(roomsBuffer);
		
		Asserts.assertEquals(expectedRoomList, roomsBuffer);
	}
	
	@Test
	public void destroyGroupTest() throws InterruptedException {
		// given
		MMORoomGroup sut = MMORoomGroup.builder().timeTickMillies(100).build();
		
		// when
		Thread.sleep(1000);
		sut.destroy();
		
		// then
		Asserts.assertEquals(FieldUtil.getFieldValue(sut, "active"), false);
	}
}

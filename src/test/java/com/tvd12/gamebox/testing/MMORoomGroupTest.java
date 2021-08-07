package com.tvd12.gamebox.testing;

import com.tvd12.ezyfox.util.EzyWrap;
import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.entity.MMORoomGroup;
import com.tvd12.gamebox.manager.RoomManager;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.reflect.FieldUtil;
import com.tvd12.test.util.RandomUtil;
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
		MMORoom room = mock(MMORoom.class);
		EzyWrap<String> roomGroupThreadName = new EzyWrap<>();
		doAnswer(invocation -> {
			roomGroupThreadName.setValue(Thread.currentThread().getName());
			return null;
		}).when(room).update();
		
		// when
		MMORoomGroup sut = MMORoomGroup.builder().timeTickMillies(100).build();
		
		// then
		sut.addRoom(room);
		Asserts.assertNotNull(FieldUtil.getFieldValue(sut, "timeTickMillis"));
		Asserts.assertNotNull(FieldUtil.getFieldValue(sut, "roomsBuffer"));
		Asserts.assertNotNull(FieldUtil.getFieldValue(sut, "roomManager"));
		
		Thread.sleep(300);
		Asserts.assertNotEquals(Thread.currentThread().getName(), roomGroupThreadName.getValue());
	}
	
	@Test
	public void addRoomTest() throws InterruptedException {
		// given
		MMORoomGroup sut = MMORoomGroup.builder().timeTickMillies(100).build();
		MMORoom room = mock(MMORoom.class);
		doNothing().when(room).update();
		
		// when
		sut.addRoom(room);
		Thread.sleep(300);
		
		// then
		verify(room, atLeastOnce()).update();
	}
	
	@Test
	public void removeRoomTest() throws InterruptedException {
		// given
		MMORoomGroup sut = MMORoomGroup.builder().timeTickMillies(100).build();
		MMORoom room1 = MMORoom.builder()
				.distanceOfInterest(RandomUtil.randomSmallDouble())
				.name("room1")
				.build();
		
		MMORoom room2 = MMORoom.builder()
				.distanceOfInterest(RandomUtil.randomSmallDouble())
				.name("room2")
				.build();
		
		sut.addRoom(room1);
		sut.addRoom(room2);
		
		Thread.sleep(300);
		
		// when
		sut.removeRoom(room1);
		
		// then
		RoomManager<MMORoom> roomManager = FieldUtil.getFieldValue(sut, "roomManager");
		List<MMORoom> roomsBuffer = new ArrayList<>();
		roomManager.getRoomList(roomsBuffer);
		
		List<MMORoom> expectedRoomList = new ArrayList<>();
		expectedRoomList.add(room2);
		
		Asserts.assertEquals(expectedRoomList, roomsBuffer);
	}
	
	@Test
	public void destroyGroupTest() throws InterruptedException {
		// given
		MMORoom room = mock(MMORoom.class);
		EzyWrap<String> roomGroupThreadName = new EzyWrap<>();
		doAnswer(invocation -> {
			roomGroupThreadName.setValue(Thread.currentThread().getName());
			return null;
		}).when(room).update();
		
		MMORoomGroup sut = MMORoomGroup.builder().timeTickMillies(100).build();
		sut.addRoom(room);
		
		// when
		sut.destroy();
		Thread.sleep(300);
		
		// then
		Asserts.assertEquals(FieldUtil.getFieldValue(sut, "active"), false);
		
		Set<String> threadNames = Thread.getAllStackTraces().keySet()
				.stream()
				.map(Thread::getName)
				.collect(Collectors.toSet());
		Asserts.assertFalse(threadNames.contains(roomGroupThreadName.getValue()));
	}
	
	@Test
	public void roomExceptionTest() throws InterruptedException {
		// given
		MMORoomGroup sut = MMORoomGroup.builder().timeTickMillies(100).build();
		MMORoom room = mock(MMORoom.class);
		doAnswer(invocation -> {
			throw new Exception("Exception when room.update()");
		}).when(room).update();
		
		// when
		sut.addRoom(room);
		Thread.sleep(300);
		
		// then
	}
}

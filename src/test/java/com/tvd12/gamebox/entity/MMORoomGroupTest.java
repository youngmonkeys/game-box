package com.tvd12.gamebox.entity;

import com.tvd12.ezyfox.util.EzyWrap;
import com.tvd12.gamebox.exception.RoomExistsException;
import com.tvd12.gamebox.manager.RoomManager;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.reflect.FieldUtil;
import com.tvd12.test.reflect.ReflectMethodUtil;
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
        String roomName = RandomUtil.randomShortAlphabetString();
        when(room.getName()).thenReturn(roomName);

        EzyWrap<String> roomGroupThreadName = new EzyWrap<>();
        doAnswer(invocation -> {
            roomGroupThreadName.setValue(Thread.currentThread().getName());
            return null;
        }).when(room).update();

        // when
        MMORoomGroup sut = MMORoomGroup.builder().timeTickMillis(100).build();

        // then
        sut.addRoom(room);
        Asserts.assertNotNull(FieldUtil.getFieldValue(sut, "timeTickMillis"));
        Asserts.assertNotNull(FieldUtil.getFieldValue(sut, "roomManager"));

        Thread.sleep(300);
        Asserts.assertNotEquals(Thread.currentThread().getName(), roomGroupThreadName.getValue());
    }

    @Test
    public void addRoomTest() throws InterruptedException {
        // given
        MMORoomGroup sut = MMORoomGroup.builder().timeTickMillis(100).build();
        MMORoom room = mock(MMORoom.class);
        String roomName = RandomUtil.randomShortAlphabetString();
        when(room.getName()).thenReturn(roomName);

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
        MMORoomGroup sut = MMORoomGroup.builder().timeTickMillis(100).build();
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
        Asserts.assertEquals(sut.getRoom(room2.getName()), room2);
    }

    @Test
    public void destroyGroupTest() throws InterruptedException {
        // given
        MMORoom room = mock(MMORoom.class);
        String roomName = RandomUtil.randomShortAlphabetString();
        when(room.getName()).thenReturn(roomName);

        EzyWrap<String> roomGroupThreadName = new EzyWrap<>();
        doAnswer(invocation -> {
            roomGroupThreadName.setValue(Thread.currentThread().getName());
            return null;
        }).when(room).update();

        MMORoomGroup sut = MMORoomGroup.builder().timeTickMillis(100).build();
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
        MMORoomGroup sut = MMORoomGroup.builder().timeTickMillis(100).build();
        MMORoom room = mock(MMORoom.class);
        String roomName = RandomUtil.randomShortAlphabetString();
        when(room.getName()).thenReturn(roomName);

        doThrow(InterruptedException.class).when(room).update();

        // when
        sut.addRoom(room);
        Thread.sleep(300);

        // then
        verify(room, atLeastOnce()).update();
    }

    @Test
    public void overTimeTickTest() throws InterruptedException {
        // given
        MMORoomGroup sut = MMORoomGroup.builder().timeTickMillis(100).build();
        MMORoom room = mock(MMORoom.class);
        String roomName = RandomUtil.randomShortAlphabetString();
        when(room.getName()).thenReturn(roomName);

        doAnswer(it -> {
            Thread.sleep(300);
            return null;
        }).when(room).update();

        // when
        sut.addRoom(room);
        Thread.sleep(350);

        // then
        verify(room, atLeastOnce()).update();
    }

    @Test
    public void loopException() throws InterruptedException {
        // given
        MMORoomGroup sut = MMORoomGroup.builder().timeTickMillis(100).build();
        MMORoom room = mock(MMORoom.class);
        String roomName = RandomUtil.randomShortAlphabetString();
        when(room.getName()).thenReturn(roomName);

        doAnswer(it -> {
            Thread.currentThread().interrupt();
            return null;
        }).when(room).update();

        // when
        sut.addRoom(room);
        Thread.sleep(110);

        // then
        sut.destroy();
    }

    @Test
    public void getRoomCountTest() {
        // given
        MMORoomGroup sut = MMORoomGroup.builder().timeTickMillis(100).build();
        MMORoom room1 = mockRoom(1L, "room1");
        MMORoom room2 = mockRoom(2L, "room2");

        // when
        int initialRoomCount = sut.getRoomCount();
        sut.addRoom(room1);
        sut.addRoom(room2);
        int roomCountAfterAdd = sut.getRoomCount();
        sut.removeRoom(room1);
        int roomCountAfterRemove = sut.getRoomCount();

        // then
        Asserts.assertEquals(initialRoomCount, 0);
        Asserts.assertEquals(roomCountAfterAdd, 2);
        Asserts.assertEquals(roomCountAfterRemove, 1);
        sut.destroy();
    }

    @Test
    public void getRoomCountRemoveNotExistedRoomTest() {
        // given
        MMORoomGroup sut = MMORoomGroup.builder().timeTickMillis(100).build();
        MMORoom room = mockRoom(1L, "room1");
        sut.addRoom(room);

        // when
        boolean removedNotExistedRoom = sut.removeRoom(mockRoom(2L, "room2"));
        boolean removedRoom = sut.removeRoom(room);
        boolean removedRoomAgain = sut.removeRoom(room);

        // then
        Asserts.assertFalse(removedNotExistedRoom);
        Asserts.assertTrue(removedRoom);
        Asserts.assertFalse(removedRoomAgain);
        Asserts.assertEquals(sut.getRoomCount(), 0);
        sut.destroy();
    }

    @Test
    public void getRoomCountAddDuplicatedRoomTest() {
        // given
        MMORoomGroup sut = MMORoomGroup.builder().timeTickMillis(100).build();
        sut.addRoom(mockRoom(1L, "room1"));

        // when
        Throwable e = Asserts.assertThrows(() ->
            sut.addRoom(mockRoom(1L, "room1"))
        );

        // then
        Asserts.assertEqualsType(e, RoomExistsException.class);
        Asserts.assertEquals(sut.getRoomCount(), 1);
        sut.destroy();
    }

    @Test
    public void buildTimeTickMillisZeroTest() {
        // given
        MMORoomGroup.Builder builder = MMORoomGroup.builder()
            .timeTickMillis(0);

        // when
        Throwable e = Asserts.assertThrows(builder::build);

        // then
        Asserts.assertEqualsType(e, IllegalArgumentException.class);
        Asserts.assertEquals(e.getMessage(), "timeTickMillis must be greater than 0");
    }

    @Test
    public void buildTimeTickMillisNegativeTest() {
        // given
        MMORoomGroup.Builder builder = MMORoomGroup.builder()
            .timeTickMillis(-1);

        // when
        Throwable e = Asserts.assertThrows(builder::build);

        // then
        Asserts.assertEqualsType(e, IllegalArgumentException.class);
        Asserts.assertEquals(e.getMessage(), "timeTickMillis must be greater than 0");
    }

    @Test
    public void buildWithoutTimeTickMillisTest() {
        // given
        MMORoomGroup.Builder builder = MMORoomGroup.builder();

        // when
        Throwable e = Asserts.assertThrows(builder::build);

        // then
        Asserts.assertEqualsType(e, IllegalArgumentException.class);
        Asserts.assertEquals(e.getMessage(), "timeTickMillis must be greater than 0");
    }

    @Test
    public void destroyNotStartedGroupTest() {
        // given
        MMORoomGroup sut = new MMORoomGroup(
            MMORoomGroup.builder().timeTickMillis(100)
        );

        // when
        sut.destroy();

        // then
        Asserts.assertNull(FieldUtil.getFieldValue(sut, "thread"));
        Asserts.assertEquals(FieldUtil.getFieldValue(sut, "active"), false);
    }

    @Test
    public void loopWhenNotActiveTest() {
        // given
        MMORoomGroup sut = new MMORoomGroup(
            MMORoomGroup.builder().timeTickMillis(100)
        );
        MMORoom room = mockRoom(1L, "room1");
        sut.addRoom(room);

        // when
        ReflectMethodUtil.invokeMethod("loop", sut);

        // then
        verify(room, never()).update();
    }

    @Test
    public void loopStopWhenInactiveTest() {
        // given
        MMORoomGroup sut = new MMORoomGroup(
            MMORoomGroup.builder().timeTickMillis(100)
        );
        MMORoom room = mockRoom(1L, "room1");
        doAnswer(it -> {
            FieldUtil.setFieldValue(sut, "active", false);
            return null;
        }).when(room).update();
        sut.addRoom(room);
        FieldUtil.setFieldValue(sut, "active", true);

        // when
        ReflectMethodUtil.invokeMethod("loop", sut);

        // then
        verify(room, times(1)).update();
        Asserts.assertFalse(Thread.currentThread().isInterrupted());
    }

    @Test
    public void loopCatchUpWhenBehindLessThanOneTickTest() {
        // given
        MMORoomGroup sut = new MMORoomGroup(
            MMORoomGroup.builder().timeTickMillis(100)
        );
        List<Long> updateStartNanos = new ArrayList<>();
        MMORoom room = mockRoom(1L, "room1");
        doAnswer(it -> {
            updateStartNanos.add(System.nanoTime());
            if (updateStartNanos.size() == 1) {
                Thread.sleep(150);
            }
            if (updateStartNanos.size() == 3) {
                FieldUtil.setFieldValue(sut, "active", false);
            }
            return null;
        }).when(room).update();
        sut.addRoom(room);
        FieldUtil.setFieldValue(sut, "active", true);

        // when
        ReflectMethodUtil.invokeMethod("loop", sut);

        // then
        long firstGapMillis = (updateStartNanos.get(1) - updateStartNanos.get(0)) / 1_000_000;
        long secondGapMillis = (updateStartNanos.get(2) - updateStartNanos.get(1)) / 1_000_000;
        Asserts.assertTrue(firstGapMillis >= 150 && firstGapMillis < 180);
        Asserts.assertTrue(secondGapMillis >= 40 && secondGapMillis < 80);
    }

    @Test
    public void loopResetDeadlineWhenBehindMoreThanOneTickTest() {
        // given
        MMORoomGroup sut = new MMORoomGroup(
            MMORoomGroup.builder().timeTickMillis(100)
        );
        List<Long> updateStartNanos = new ArrayList<>();
        MMORoom room = mockRoom(1L, "room1");
        doAnswer(it -> {
            updateStartNanos.add(System.nanoTime());
            if (updateStartNanos.size() == 1) {
                Thread.sleep(350);
            }
            if (updateStartNanos.size() == 3) {
                FieldUtil.setFieldValue(sut, "active", false);
            }
            return null;
        }).when(room).update();
        sut.addRoom(room);
        FieldUtil.setFieldValue(sut, "active", true);

        // when
        ReflectMethodUtil.invokeMethod("loop", sut);

        // then
        long firstGapMillis = (updateStartNanos.get(1) - updateStartNanos.get(0)) / 1_000_000;
        long secondGapMillis = (updateStartNanos.get(2) - updateStartNanos.get(1)) / 1_000_000;
        Asserts.assertTrue(firstGapMillis >= 350 && firstGapMillis < 380);
        Asserts.assertTrue(secondGapMillis >= 90);
    }

    private static MMORoom mockRoom(long id, String name) {
        MMORoom room = mock(MMORoom.class);
        when(room.getId()).thenReturn(id);
        when(room.getName()).thenReturn(name);
        return room;
    }
}

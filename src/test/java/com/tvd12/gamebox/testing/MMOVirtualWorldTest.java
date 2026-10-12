package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.entity.MMOVirtualWorld;
import com.tvd12.gamebox.exception.MaxRoomException;
import com.tvd12.gamebox.exception.RoomExistsException;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.base.BaseTest;
import com.tvd12.test.reflect.FieldUtil;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class MMOVirtualWorldTest extends BaseTest {

    @Test
    public void createMMOVirtualWorldTest() {
        // given
        int expectedMMORoomGroupCount = 10;
        int expectedTimeTickMillis = 100;
        int expectedMaxRoomCount = 1000;

        // when
        MMOVirtualWorld world = MMOVirtualWorld.builder()
            .roomGroupCount(expectedMMORoomGroupCount)
            .timeTickMillis(expectedTimeTickMillis)
            .maxRoomCount(expectedMaxRoomCount)
            .build();

        // then
        int timeTickMillis = FieldUtil.getFieldValue(world, "timeTickMillis");
        int mmoRoomGroupCount = FieldUtil.getFieldValue(world, "roomGroupCount");
        int maxRoomCount = FieldUtil.getFieldValue(world, "maxRoomCount");

        Asserts.assertEquals(expectedTimeTickMillis, timeTickMillis);
        Asserts.assertEquals(expectedMMORoomGroupCount, mmoRoomGroupCount);
        Asserts.assertEquals(expectedMaxRoomCount, maxRoomCount);
        Asserts.assertNotNull(FieldUtil.getFieldValue(world, "roomGroups"));
    }

    @Test
    public void addAndGetRoomTest() {
        // given
        MMOVirtualWorld world = MMOVirtualWorld.builder().build();
        MMORoom expectedRoom = mock(MMORoom.class);
        String roomName = RandomUtil.randomShortAlphabetString();
        when(expectedRoom.getName()).thenReturn(roomName);
        long expectedRoomId = 1;
        when(expectedRoom.getId()).thenReturn(expectedRoomId);

        // when
        world.addRoom(expectedRoom);

        // then
        MMORoom room = world.getRoom(expectedRoomId);
        Asserts.assertEquals(expectedRoom, room);
        verify(expectedRoom, times(2)).getId();
        verify(expectedRoom, times(1)).getName();
    }

    @Test
    public void removeRoomTest() {
        // given
        MMOVirtualWorld world = MMOVirtualWorld.builder().build();
        MMORoom expectedRoom = mock(MMORoom.class);
        String roomName = RandomUtil.randomShortAlphabetString();
        when(expectedRoom.getName()).thenReturn(roomName);

        long expectedRoomId = 1;
        when(expectedRoom.getId()).thenReturn(expectedRoomId);
        world.addRoom(expectedRoom);

        // when
        world.removeRoom(expectedRoom);

        // then
        MMORoom room = world.getRoom(expectedRoomId);
        Asserts.assertNull(room);
        verify(expectedRoom, times(4)).getId();
        verify(expectedRoom, times(3)).getName();
    }


    @Test(expectedExceptions = MaxRoomException.class)
    public void maxRoomCountTest() {
        // given
        int expectedMaxRoomCount = 2;
        MMOVirtualWorld world = MMOVirtualWorld.builder()
            .maxRoomCount(expectedMaxRoomCount).build();
        MMORoom room1 = mock(MMORoom.class);
        String room1Name = RandomUtil.randomShortAlphabetString();
        when(room1.getName()).thenReturn(room1Name);

        MMORoom room2 = mock(MMORoom.class);
        String room2Name = RandomUtil.randomShortAlphabetString();
        when(room2.getName()).thenReturn(room2Name);

        MMORoom room3 = mock(MMORoom.class);
        String room3Name = RandomUtil.randomShortAlphabetString();
        when(room3.getName()).thenReturn(room3Name);

        long roomId1 = 1;
        long roomId2 = 2;
        long roomId3 = 3;
        when(room1.getId()).thenReturn(roomId1);
        when(room2.getId()).thenReturn(roomId2);
        when(room3.getId()).thenReturn(roomId3);

        world.addRoom(room1);
        world.addRoom(room2);

        // when
        world.addRoom(room3);

        // then
        Asserts.assertTrue(world.getRoomCount() == 1);
    }

    @Test
    public void negativeRoomIdTest() {
        // given
        MMOVirtualWorld world = MMOVirtualWorld.builder()
            .roomGroupCount(3)
            .build();
        MMORoom room = mock(MMORoom.class);
        when(room.getName()).thenReturn("negative");
        when(room.getId()).thenReturn(-1L);

        // when
        world.addRoom(room);

        // then
        Asserts.assertTrue(world.getRoom(-1L) == room);
        world.destroy();
    }

    @Test
    public void getRoomByNameTest() {
        // given
        MMOVirtualWorld world = MMOVirtualWorld.builder()
            .roomGroupCount(3)
            .build();
        MMORoom room1 = mockRoom(1L, "room1");
        MMORoom room2 = mockRoom(2L, "room2");
        world.addRoom(room1);
        world.addRoom(room2);

        // when
        MMORoom actualRoom1 = world.getRoom("room1");
        MMORoom actualRoom2 = world.getRoom("room2");

        // then
        Asserts.assertTrue(actualRoom1 == room1);
        Asserts.assertTrue(actualRoom2 == room2);
        world.destroy();
    }

    @Test
    public void getRoomByNameNotFoundTest() {
        // given
        MMOVirtualWorld world = MMOVirtualWorld.builder()
            .roomGroupCount(3)
            .build();
        world.addRoom(mockRoom(1L, "room1"));

        // when
        MMORoom room = world.getRoom("unknown");

        // then
        Asserts.assertNull(room);
        world.destroy();
    }

    @Test
    public void getRoomByNameAfterRemoveTest() {
        // given
        MMOVirtualWorld world = MMOVirtualWorld.builder()
            .roomGroupCount(3)
            .build();
        MMORoom room = mockRoom(1L, "room1");
        world.addRoom(room);

        // when
        world.removeRoom(room);

        // then
        Asserts.assertNull(world.getRoom("room1"));
        world.destroy();
    }

    @Test
    public void getRoomCountTest() {
        // given
        MMOVirtualWorld world = MMOVirtualWorld.builder()
            .roomGroupCount(3)
            .build();
        MMORoom room1 = mockRoom(1L, "room1");
        MMORoom room2 = mockRoom(2L, "room2");
        MMORoom room3 = mockRoom(3L, "room3");

        // when
        int initialRoomCount = world.getRoomCount();
        world.addRoom(room1);
        world.addRoom(room2);
        world.addRoom(room3);
        int roomCountAfterAdd = world.getRoomCount();
        world.removeRoom(room2);
        int roomCountAfterRemove = world.getRoomCount();

        // then
        Asserts.assertEquals(initialRoomCount, 0);
        Asserts.assertEquals(roomCountAfterAdd, 3);
        Asserts.assertEquals(roomCountAfterRemove, 2);
        world.destroy();
    }

    @Test
    public void getRoomCountRemoveNotExistedRoomTest() {
        // given
        MMOVirtualWorld world = MMOVirtualWorld.builder()
            .roomGroupCount(3)
            .build();
        MMORoom room = mockRoom(1L, "room1");
        world.addRoom(room);

        // when
        world.removeRoom(mockRoom(2L, "room2"));
        world.removeRoom(room);
        world.removeRoom(room);

        // then
        Asserts.assertEquals(world.getRoomCount(), 0);
        world.destroy();
    }

    @Test
    public void getRoomCountAddDuplicatedRoomTest() {
        // given
        MMOVirtualWorld world = MMOVirtualWorld.builder()
            .roomGroupCount(3)
            .build();
        MMORoom room = mockRoom(1L, "room1");
        world.addRoom(room);

        // when
        Throwable e = Asserts.assertThrows(() ->
            world.addRoom(mockRoom(1L, "room1"))
        );

        // then
        Asserts.assertEqualsType(e, RoomExistsException.class);
        Asserts.assertEquals(world.getRoomCount(), 1);
        world.destroy();
    }

    @Test
    public void getRoomCountMaxRoomExceededTest() {
        // given
        MMOVirtualWorld world = MMOVirtualWorld.builder()
            .roomGroupCount(3)
            .maxRoomCount(2)
            .build();
        world.addRoom(mockRoom(1L, "room1"));
        world.addRoom(mockRoom(2L, "room2"));

        // when
        Throwable e = Asserts.assertThrows(() ->
            world.addRoom(mockRoom(3L, "room3"))
        );

        // then
        Asserts.assertEqualsType(e, MaxRoomException.class);
        Asserts.assertEquals(world.getRoomCount(), 2);
        world.destroy();
    }

    @Test
    public void buildRoomGroupCountZeroTest() {
        // given
        MMOVirtualWorld.Builder builder = MMOVirtualWorld.builder()
            .roomGroupCount(0);

        // when
        Throwable e = Asserts.assertThrows(builder::build);

        // then
        Asserts.assertEqualsType(e, IllegalArgumentException.class);
        Asserts.assertEquals(e.getMessage(), "roomGroupCount must be greater than 0");
    }

    @Test
    public void buildRoomGroupCountNegativeTest() {
        // given
        MMOVirtualWorld.Builder builder = MMOVirtualWorld.builder()
            .roomGroupCount(-1);

        // when
        Throwable e = Asserts.assertThrows(builder::build);

        // then
        Asserts.assertEqualsType(e, IllegalArgumentException.class);
        Asserts.assertEquals(e.getMessage(), "roomGroupCount must be greater than 0");
    }

    @Test
    public void buildTimeTickMillisZeroTest() {
        // given
        MMOVirtualWorld.Builder builder = MMOVirtualWorld.builder()
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
        MMOVirtualWorld.Builder builder = MMOVirtualWorld.builder()
            .timeTickMillis(-1);

        // when
        Throwable e = Asserts.assertThrows(builder::build);

        // then
        Asserts.assertEqualsType(e, IllegalArgumentException.class);
        Asserts.assertEquals(e.getMessage(), "timeTickMillis must be greater than 0");
    }

    @Test
    public void buildMaxRoomCountZeroTest() {
        // given
        MMOVirtualWorld.Builder builder = MMOVirtualWorld.builder()
            .maxRoomCount(0);

        // when
        Throwable e = Asserts.assertThrows(builder::build);

        // then
        Asserts.assertEqualsType(e, IllegalArgumentException.class);
        Asserts.assertEquals(e.getMessage(), "maxRoomCount must be greater than 0");
    }

    @Test
    public void buildMaxRoomCountNegativeTest() {
        // given
        MMOVirtualWorld.Builder builder = MMOVirtualWorld.builder()
            .maxRoomCount(-1);

        // when
        Throwable e = Asserts.assertThrows(builder::build);

        // then
        Asserts.assertEqualsType(e, IllegalArgumentException.class);
        Asserts.assertEquals(e.getMessage(), "maxRoomCount must be greater than 0");
    }

    private static MMORoom mockRoom(long id, String name) {
        MMORoom room = mock(MMORoom.class);
        when(room.getId()).thenReturn(id);
        when(room.getName()).thenReturn(name);
        return room;
    }
}

package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.entity.MMOVirtualWorld;
import com.tvd12.gamebox.exception.MaxRoomException;
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
        verify(expectedRoom, times(2)).getName();
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
}

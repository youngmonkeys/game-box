package com.tvd12.gamebox.testing.manager;

import com.tvd12.gamebox.entity.LocatedRoom;
import com.tvd12.gamebox.exception.MaxRoomException;
import com.tvd12.gamebox.exception.RoomExistsException;
import com.tvd12.gamebox.manager.SynchronizedRoomManager;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

import java.util.Arrays;

public class SynchronizedRoomManagerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        // given
        SynchronizedRoomManager<LocatedRoom> sut =
            (SynchronizedRoomManager<LocatedRoom>) SynchronizedRoomManager.builder()
                .maxRoom(2)
                .build();

        LocatedRoom room1 = LocatedRoom.builder()
            .name("room1")
            .build();

        LocatedRoom room2 = LocatedRoom.builder()
            .name("room2")
            .build();

        LocatedRoom roomx = LocatedRoom.builder()
            .name("roomx")
            .build();

        // when
        // then
        Asserts.assertNotNull(sut.getSynchronizedLock());
        sut.addRoom(room1, false);
        sut.addRoom(room1, false);
        Throwable e1 = Asserts.assertThrows(() -> sut.addRoom(room1, true));
        Asserts.assertEqualsType(e1, RoomExistsException.class);
        sut.addRoom(room2, true);
        Throwable e2 = Asserts.assertThrows(() -> sut.addRoom(roomx, true));
        Asserts.assertEqualsType(e2, MaxRoomException.class);
        sut.clear();

        sut.addRooms(Arrays.asList(room1), false);
        Throwable e3 = Asserts.assertThrows(() -> sut.addRooms(Arrays.asList(room1), true));
        Asserts.assertEqualsType(e3, RoomExistsException.class);
        sut.addRooms(Arrays.asList(room2), true);
        Throwable e4 = Asserts.assertThrows(() -> sut.addRooms(Arrays.asList(roomx), true));
        Asserts.assertEqualsType(e4, MaxRoomException.class);

        Asserts.assertEquals(sut.getRoom("room1"), room1);
        Asserts.assertEquals(sut.getRoomList(), Arrays.asList(room1, room2), false);

        sut.removeRoom(room1.getId());
        Asserts.assertEquals(sut.getRoomCount(), 1);

        sut.removeRoom(room2.getName());
        Asserts.assertEquals(sut.getRoomCount(), 0);

        sut.addRoom(room1);

        Asserts.assertTrue(sut.available());

        sut.addRoom(room2);
        sut.removeRooms(Arrays.asList(room1, room2));
        Asserts.assertEquals(sut.getRoomCount(), 0);

        sut.addRoom(room1);
        sut.addRoom(room2);

        Asserts.assertEquals(sut.getRoom(it -> it.getName().equals("room1")), room1);
        Asserts.assertNull(sut.getRoom(it -> it.getName().equals("unknown")));
        Asserts.assertEquals(sut.getRoomList(it -> it.getName().equals("room1")), Arrays.asList(room1), false);

        sut.clear();
        sut.addRooms(new LocatedRoom[]{room1}, false);
        Throwable e5 = Asserts.assertThrows(() -> sut.addRooms(new LocatedRoom[]{room1}, true));
        Asserts.assertEqualsType(e5, RoomExistsException.class);
        sut.addRooms(new LocatedRoom[]{room2}, true);
        Throwable e6 = Asserts.assertThrows(() -> sut.addRooms(new LocatedRoom[]{roomx}, true));
        Asserts.assertEqualsType(e6, MaxRoomException.class);
        Asserts.assertFalse(sut.available());

        Asserts.assertTrue(sut.containsRoom(room1.getId()));
        Asserts.assertTrue(sut.containsRoom("room1"));

        sut.clear();
        sut.removeRoom((LocatedRoom) null);
    }
}

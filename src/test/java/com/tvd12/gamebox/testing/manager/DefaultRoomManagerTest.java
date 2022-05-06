package com.tvd12.gamebox.testing.manager;

import com.tvd12.gamebox.entity.NormalRoom;
import com.tvd12.gamebox.entity.Room;
import com.tvd12.gamebox.manager.DefaultRoomManager;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

import java.util.Collections;

public class DefaultRoomManagerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        // given
        DefaultRoomManager<Room> sut =
            (DefaultRoomManager<Room>) DefaultRoomManager.builder()
                .maxRoom(100)
                .build();

        Room room1 = NormalRoom.builder()
            .name("room1")
            .build();

        Room room2 = NormalRoom.builder()
            .name("room2")
            .build();

        // when
        sut.addRooms(new Room[]{room1});
        sut.addRooms(Collections.singletonList(room2));

        // then
        Asserts.assertEquals(sut.getMaxRoom(), 100);
        Asserts.assertEquals(sut.getRoomCount(), 2);
        Asserts.assertTrue(sut.containsRoom(room1));
    }

    @Test
    public void defaultConstructorTest() {
        // given
        DefaultRoomManager<Room> sut = new DefaultRoomManager<>();

        // when
        // then
        Asserts.assertEquals(sut.getMaxRoom(), 10000);
    }

    @Test
    public void cConstructorWithMaxRoomTest() {
        // given
        DefaultRoomManager<Room> sut = new DefaultRoomManager<>(200);

        // when
        // then
        Asserts.assertEquals(sut.getMaxRoom(), 200);
    }
}

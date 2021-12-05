package com.tvd12.gamebox.testing.manager;

import org.testng.annotations.Test;

import com.tvd12.gamebox.entity.Room;
import com.tvd12.gamebox.manager.SimpleRoomManager;
import com.tvd12.test.assertion.Asserts;

public class SimpleRoomManagerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        // given
        SimpleRoomManager<Room> sut = 
            (SimpleRoomManager<Room>) SimpleRoomManager.builder()
            .maxRoom(100)
            .build();
        
        // when
        // then
        Asserts.assertEquals(sut.getMaxRoom(), 100);
    }
    
    @Test
    public void defaultConstructorTest() {
        // given
        SimpleRoomManager<Room> sut = new SimpleRoomManager<>();
        
        // when
        // then
        Asserts.assertEquals(sut.getMaxRoom(), 10000);
    }
    
    @Test
    public void cConstructorWithMaxRoomTest() {
        // given
        SimpleRoomManager<Room> sut = new SimpleRoomManager<>(200);
        
        // when
        // then
        Asserts.assertEquals(sut.getMaxRoom(), 200);
    }
}

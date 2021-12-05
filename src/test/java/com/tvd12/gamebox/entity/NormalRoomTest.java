package com.tvd12.gamebox.entity;

import org.testng.annotations.Test;

import com.tvd12.test.assertion.Asserts;

public class NormalRoomTest {

    @Test
    public void test() {
        // given
        NormalRoom sut = NormalRoom.builder()
            .defaultPlayerManager(100)
            .build();
        
        // when
        // then
        Asserts.assertEquals(sut.getPlayerManager().getMaxPlayer(), 100);
    }
}

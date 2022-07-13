package com.tvd12.gamebox.testing.entity;

import com.tvd12.gamebox.entity.NormalRoom;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

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

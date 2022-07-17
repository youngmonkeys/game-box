package com.tvd12.gamebox.testing.entity;

import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.entity.LocatedRoom;
import com.tvd12.gamebox.exception.NoSlotException;
import com.tvd12.gamebox.manager.DefaultLocatedPlayerManager;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

public class LocatedRoomTest {

    @Test
    public void test() {
        // given
        LocatedRoom sut = LocatedRoom.builder()
            .maxSlot(2)
            .playerManager(new DefaultLocatedPlayerManager())
            .build();

        LocatedPlayer player1 = LocatedPlayer.builder()
            .name("player1")
            .build();
        sut.addPlayer(player1);

        LocatedPlayer player2 = LocatedPlayer.builder()
            .name("player2")
            .build();
        sut.addPlayer(player2);

        LocatedPlayer playerx = LocatedPlayer.builder()
            .name("playerx")
            .build();

        // when
        // then
        Asserts.assertEquals(sut.getMaxSlot(), 2);
        Asserts.assertEmpty(sut.getSlots());

        sut.removePlayer(playerx);
        sut.removePlayer(player2);
        Asserts.assertEquals(sut.getPlayerManager().getPlayerCount(), 1);

        sut.addPlayer(player2);
        Throwable e = Asserts.assertThrows(() -> sut.addPlayer(playerx));
        Asserts.assertEqualsType(e, NoSlotException.class);
    }
}

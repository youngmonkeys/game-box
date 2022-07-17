package com.tvd12.gamebox.testing.entity;

import com.tvd12.gamebox.constant.PlayerRole;
import com.tvd12.gamebox.constant.PlayerStatus;
import com.tvd12.gamebox.entity.Player;
import com.tvd12.test.assertion.Asserts;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlayerTest {

    @Test
    public void test() {
        // given
        Player sut = Player.builder()
            .name("test")
            .build();

        // when
        sut.setRole(PlayerRole.MASTER);
        sut.setStatus(PlayerStatus.PLAYING);
        sut.setCurrentRoomId(1L);

        // then
        Asserts.assertEquals(sut.getRole(), PlayerRole.MASTER);
        Asserts.assertEquals(sut.getStatus(), PlayerStatus.PLAYING);
        Asserts.assertEquals(sut.getCurrentRoomId(), 1L);
        //noinspection EqualsWithItself
        Asserts.assertTrue(sut.equals(sut));
        //noinspection ConstantConditions
        Asserts.assertFalse(sut.equals(null));

        Player me = Player.builder()
            .name("test")
            .build();
        Player other = Player.builder()
            .name("other")
            .build();
        Assert.assertEquals(sut, me);
        Asserts.assertNotEquals(me, other);
        Assert.assertEquals(sut.hashCode(), me.hashCode());
        Asserts.assertNotEquals(sut.hashCode(), other.hashCode());
    }
}

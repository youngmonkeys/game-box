package com.tvd12.gamebox.entity;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;

public class MMORoomTest {

    @Test
    public void test() {
        // given
        MMORoom sut = MMORoom.builder()
            .defaultPlayerManager(2)
            .distanceOfInterest(100)
            .build();
        
        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
        
        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();
        
        sut.addPlayer(player1);
        sut.addPlayer(player2);
        
        // when
        Asserts.assertEquals(sut.getMaxPlayer(), 2);
        Asserts.assertEquals(sut.getDistanceOfInterest(), 100D);
        
        sut.setMaster(player1);
        Asserts.assertEquals(sut.getMaster(), player1);
        
        sut.removePlayer(player1);
        Asserts.assertEquals(sut.getMaster(), player2);
        sut.removePlayer(player2);
        Asserts.assertNull(sut.getMaster());
        Asserts.assertTrue(sut.isEmpty());
        
        sut.addPlayer(player1);
        sut.addPlayer(player1);
        sut.addPlayer(player2);
        sut.removePlayer(player2);
        Asserts.assertFalse(sut.isEmpty());
        sut.addPlayer(player2);
        
        player1.setPosition(Vec3.ZERO);
        player2.setPosition(new Vec3(1000, 1000, 1000));
        Asserts.assertEmpty(player1.getNearbyPlayerNames());
        Asserts.assertEmpty(player2.getNearbyPlayerNames());
        sut.update();
        Asserts.assertEquals(player1.getNearbyPlayerNames(), Arrays.asList(player1.getName()), false);
        Asserts.assertEquals(player2.getNearbyPlayerNames(), Arrays.asList(player2.getName()), false);
        
        player2.setPosition(new Vec3(1, 1, 1));
        sut.update();
        Asserts.assertEquals(player1.getNearbyPlayerNames(), Arrays.asList(player1.getName(), player2.getName()), false);
        Asserts.assertEquals(player2.getNearbyPlayerNames(), Arrays.asList(player1.getName(), player2.getName()), false);
    }
}

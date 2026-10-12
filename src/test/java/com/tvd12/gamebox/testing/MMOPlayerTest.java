package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.reflect.ReflectMethodUtil;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MMOPlayerTest {

    @Test
    public void setPositionTest() {
        // given
        Vec3 position = TestHelper.randomVec3();
        MMOPlayer sut = new MMOPlayer("a");

        // when
        sut.setPosition(position);

        // then
        Asserts.assertEquals(position, sut.getPosition());
    }

    @Test
    public void setRotationTest() {
        // given
        Vec3 rotation = TestHelper.randomVec3();
        MMOPlayer sut = new MMOPlayer("a");

        // when
        sut.setRotation(rotation);

        // then
        Asserts.assertEquals(rotation, sut.getRotation());
    }

    @Test
    public void addNearbyPlayersTest() {
        // given
        int nearbyPlayerCount = RandomUtil.randomSmallInt();
        MMOPlayer sut = new MMOPlayer("a");
        List<MMOPlayer> nearbyPlayers = new ArrayList<>();
        for (int i = 0; i < nearbyPlayerCount; i++) {
            nearbyPlayers.add(new MMOPlayer("player#" + i));
        }

        // when
        nearbyPlayers.forEach((nearbyPlayer) -> {
            ReflectMethodUtil.invokeMethod("addNearbyPlayer", sut, nearbyPlayer);
        });

        // then
        List<String> buffer = new ArrayList<>();
        sut.getNearbyPlayerNames(buffer);
        buffer.sort(String::compareTo);
        Asserts.assertEquals(
            nearbyPlayers.stream().map(MMOPlayer::getName).collect(Collectors.toList()),
            buffer
        );
    }

    @Test
    public void getNearbyPlayersTest() {
        // given
        MMOPlayer sut = new MMOPlayer("a");
        MMOPlayer player1 = new MMOPlayer("b");
        MMOPlayer player2 = new MMOPlayer("c");
        ReflectMethodUtil.invokeMethod("addNearbyPlayer", sut, player1);
        ReflectMethodUtil.invokeMethod("addNearbyPlayer", sut, player2);
        List<MMOPlayer> buffer = new ArrayList<>();

        // when
        sut.getNearbyPlayers(buffer);

        // then
        buffer.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        Asserts.assertEquals(buffer.size(), 2);
        Asserts.assertTrue(buffer.get(0) == player1);
        Asserts.assertTrue(buffer.get(1) == player2);
        Asserts.assertEquals(sut.getNearbyPlayerCount(), 2);
    }

    @Test
    public void interestEnabledTest() {
        // given
        MMOPlayer defaultPlayer = new MMOPlayer("a");
        MMOPlayer disabledPlayer = new MMOPlayer("b", false);
        MMOPlayer builtPlayer = MMOPlayer.builder()
            .name("c")
            .interestEnabled(false)
            .build();
        MMOPlayer builtDefaultPlayer = MMOPlayer.builder()
            .name("d")
            .build();

        // when
        // then
        Asserts.assertTrue(defaultPlayer.isInterestEnabled());
        Asserts.assertFalse(disabledPlayer.isInterestEnabled());
        Asserts.assertFalse(builtPlayer.isInterestEnabled());
        Asserts.assertTrue(builtDefaultPlayer.isInterestEnabled());
    }

    @Test
    public void getNearbyPlayersEmptyTest() {
        // given
        MMOPlayer sut = new MMOPlayer("a");
        List<MMOPlayer> buffer = new ArrayList<>();

        // when
        sut.getNearbyPlayers(buffer);

        // then
        Asserts.assertTrue(buffer.isEmpty());
        Asserts.assertEquals(sut.getNearbyPlayerCount(), 0);
    }
}

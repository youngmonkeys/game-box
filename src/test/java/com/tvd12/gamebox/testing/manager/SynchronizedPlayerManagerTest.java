package com.tvd12.gamebox.testing.manager;

import com.tvd12.ezyfox.collect.Sets;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.Player;
import com.tvd12.gamebox.exception.MaxPlayerException;
import com.tvd12.gamebox.exception.PlayerExistsException;
import com.tvd12.gamebox.manager.SynchronizedPlayerManager;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

import java.util.*;

public class SynchronizedPlayerManagerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        // given
        SynchronizedPlayerManager<MMOPlayer> sut =
            (SynchronizedPlayerManager<MMOPlayer>) SynchronizedPlayerManager.builder()
                .maxPlayer(2)
                .build();

        MMOPlayer player1 = MMOPlayer.builder()
            .name("test")
            .build();
        sut.addPlayer(player1);

        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();

        MMOPlayer playerx = MMOPlayer.builder()
            .name("playerx")
            .build();

        // when
        // then
        Asserts.assertEquals(sut.getMaxPlayer(), 2);
        Asserts.assertEquals(sut.getPlayer("test"), player1);
        Asserts.assertNull(sut.getPlayer("unknown"));
        Asserts.assertEquals(sut.getPlayer("test", () -> null), player1);
        Asserts.assertEquals(sut.getFirstPlayer(), player1);
        Asserts.assertEquals(
            sut.getPlayerList(it -> true),
            Collections.singletonList(player1),
            false
        );
        Asserts.assertEmpty(sut.getPlayerList(it -> it.getName().equals("unknown")));
        Asserts.assertEquals(
            sut.getPlayerNames(),
            Collections.singletonList("test"),
            false
        );

        Asserts.assertEquals(sut.removePlayer(player1), player1);
        sut.addPlayer(player1, true);
        Asserts.assertTrue(sut.available());
        sut.addPlayer(player1, false);
        Throwable e1 = Asserts.assertThrows(() -> sut.addPlayer(player1, true));
        Asserts.assertEqualsType(e1, PlayerExistsException.class);
        sut.addPlayer(player2, false);
        Throwable e2 = Asserts.assertThrows(() -> sut.addPlayer(playerx, true));
        Asserts.assertEqualsType(e2, MaxPlayerException.class);
        Asserts.assertEquals(sut.getPlayerCount(), 2);
        Asserts.assertFalse(sut.available());

        sut.removePlayers(Collections.singletonList(player1));
        Asserts.assertEquals(sut.getPlayerCount(), 1);

        sut.clear();
        Asserts.assertNull(sut.getFirstPlayer());
        Asserts.assertEquals(sut.getPlayer("unknown", () -> playerx), playerx);
        sut.clear();
        sut.addPlayers(Collections.singletonList(player1), false);
        Asserts.assertEquals(sut.getPlayerCount(), 1);
        sut.addPlayers(Collections.singletonList(player1), false);
        Throwable e3 = Asserts.assertThrows(
            () -> sut.addPlayers(Collections.singletonList(player1), true)
        );
        Asserts.assertEqualsType(e3, PlayerExistsException.class);
        Throwable e4 = Asserts.assertThrows(() -> sut.addPlayers(Arrays.asList(player2, playerx), false));
        Asserts.assertEqualsType(e4, MaxPlayerException.class);

        Asserts.assertEquals(sut.countPlayers(it -> it.getName().equals("test")), 1);
        Asserts.assertEquals(
            sut.filterPlayers(it -> it.getName().equals("test")),
            Collections.singletonList(player1),
            false
        );
        List<Player> playerList = new ArrayList<>();
        sut.forEach(playerList::add);
        Asserts.assertEquals(
            playerList,
            sut.getPlayerList()
        );

        sut.clear();
        sut.removePlayer((MMOPlayer) null);
    }

    @Test
    public void defaultConstructorTest() {
        // given
        SynchronizedPlayerManager<MMOPlayer> sut = new SynchronizedPlayerManager<>();

        // when
        // then
        Asserts.assertTrue(sut.available());
    }

    @Test
    public void getPlayerListAndToStringTest() {
        // given
        String player1Name = RandomUtil.randomShortAlphabetString();
        Player player1 = new Player(player1Name);

        String player2Name = RandomUtil.randomShortAlphabetString();
        Player player2 = new Player(player2Name);

        SynchronizedPlayerManager<Player> sut = new SynchronizedPlayerManager<>();
        sut.addPlayer(player1);
        sut.addPlayer(player2);

        // when
        List<Player> actual = sut.getPlayerList();

        // then
        Asserts.assertEquals(
            new HashSet<>(actual),
            Sets.newHashSet(player1, player2),
            false
        );

        Asserts.assertEquals(
            sut.toString(),
            "playerByName.size = 2"
        );
    }
}

package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMOUniformGridRoom;
import com.tvd12.gamebox.entity.Player;
import com.tvd12.gamebox.grid.UniformGrid;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.reflect.FieldUtil;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MMOUniformGridRoomTest {

    @Test
    public void createRoomTest() {
        // given
        // when
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(20)
            .maxPlayer(10)
            .build();
        MMOUniformGridRoom roomWithCellSize = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .cellSize(5)
            .distanceOfInterest(20)
            .maxPlayer(10)
            .build();

        // then
        Asserts.assertEquals(room.getCellSize(), 20F);
        Asserts.assertEquals(roomWithCellSize.getCellSize(), 5F);
    }

    @Test
    public void nearbyPlayersUseRealDistanceTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        MMOPlayer player3 = new MMOPlayer("player3");

        // when
        room.setPlayerPosition(player1, new Vec3(0, 0, 0));
        room.setPlayerPosition(player2, new Vec3(6, 0, 6));
        room.setPlayerPosition(player3, new Vec3(9, 0, 9));

        // then
        Asserts.assertEquals(
            new HashSet<>(player1.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("player1", "player2"))
        );
        Asserts.assertEquals(
            new HashSet<>(player2.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("player1", "player2", "player3"))
        );
        Asserts.assertEquals(
            new HashSet<>(player3.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("player2", "player3"))
        );
        Asserts.assertTrue(room.getPlayerManager().containsPlayer(player1));
    }

    @Test
    public void movePlayerAwayTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        room.setPlayerPosition(player1, new Vec3(0, 0, 0));
        room.setPlayerPosition(player2, new Vec3(1, 0, 1));

        // when
        room.setPlayerPosition(player2, new Vec3(-100, 0, -100));

        // then
        Asserts.assertEquals(
            player1.getNearbyPlayerNames(),
            Collections.singletonList("player1"),
            false
        );
        Asserts.assertEquals(
            player2.getNearbyPlayerNames(),
            Collections.singletonList("player2"),
            false
        );
        Asserts.assertEquals(
            room.searchPlayers(new Vec3(-100, 0, -100), 1),
            Collections.singletonList(player2),
            false
        );
    }

    @Test
    public void removePlayerTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        room.addPlayer(player1);
        room.addPlayer(player2);

        // when
        room.removePlayer(player2);
        room.removePlayer(new MMOPlayer("unknown"));

        // then
        Asserts.assertEquals(
            player1.getNearbyPlayerNames(),
            Collections.singletonList("player1"),
            false
        );
        Asserts.assertEquals(
            player2.getNearbyPlayerNames(),
            Collections.emptyList(),
            false
        );
        Asserts.assertFalse(room.getPlayerManager().containsPlayer(player2));
    }

    @Test
    public void addPlayerTwiceTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        player2.setPosition(new Vec3(100, 0, 100));
        room.addPlayer(player1);
        room.addPlayer(player2);

        // when
        room.addPlayer(player1);

        // then
        Asserts.assertEquals(room.getPlayerManager().getPlayerCount(), 2);
        Asserts.assertEquals(
            player1.getNearbyPlayerNames(),
            Collections.singletonList("player1"),
            false
        );
        Asserts.assertEquals(
            room.searchPlayers(new Vec3(0, 0, 0), 1),
            Collections.singletonList(player1),
            false
        );
    }

    @Test
    public void removeNotMMOPlayerTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        Player player = new Player("player");

        // when
        Throwable e = Asserts.assertThrows(() -> room.removePlayer(player));

        // then
        Asserts.assertEqualsType(e, IllegalArgumentException.class);
    }

    @Test
    public void removePlayerWithNearbyPlayerNotInRoomTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer player = new MMOPlayer("player");
        room.addPlayer(player);

        MMOPlayer outsidePlayer = new MMOPlayer("outsidePlayer");
        Map<String, MMOPlayer> nearbyPlayers = FieldUtil.getFieldValue(player, "nearbyPlayers");
        nearbyPlayers.put(outsidePlayer.getName(), outsidePlayer);

        // when
        room.removePlayer(player);

        // then
        Asserts.assertTrue(player.getNearbyPlayerNames().isEmpty());
        Asserts.assertFalse(room.getPlayerManager().containsPlayer(player));
    }

    @Test
    public void updateTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        room.setPlayerPosition(player1, new Vec3(0, 0, 0));
        room.setPlayerPosition(player2, new Vec3(100, 0, 100));

        // when
        room.update();

        // then
        Asserts.assertEquals(
            player1.getNearbyPlayerNames(),
            Collections.singletonList("player1"),
            false
        );
        Asserts.assertEquals(
            player2.getNearbyPlayerNames(),
            Collections.singletonList("player2"),
            false
        );
    }

    @Test
    public void randomMovesMatchBruteForceTest() {
        // given
        float distanceOfInterest = 15;
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(distanceOfInterest)
            .cellSize(6)
            .maxPlayer(50)
            .build();
        List<MMOPlayer> players = new ArrayList<>();
        for (int i = 0; i < 30; ++i) {
            players.add(new MMOPlayer("player" + i));
        }

        for (int round = 0; round < 100; ++round) {
            // when
            for (MMOPlayer player : players) {
                room.setPlayerPosition(
                    player,
                    new Vec3(
                        RandomUtil.randomFloat(-50, 50),
                        0,
                        RandomUtil.randomFloat(-50, 50)
                    )
                );
            }

            // then
            for (MMOPlayer player : players) {
                Set<String> expected = players.stream()
                    .filter(it -> player.getPosition().distanceSquare(it.getPosition())
                        <= distanceOfInterest * distanceOfInterest)
                    .map(MMOPlayer::getName)
                    .collect(Collectors.toSet());
                Asserts.assertEquals(new HashSet<>(player.getNearbyPlayerNames()), expected);
            }
        }
    }

    @Test
    public void nonInterestPlayerTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer item = new MMOPlayer("item", false);

        // when
        room.setPlayerPosition(observer, new Vec3(0, 0, 0));
        room.setPlayerPosition(item, new Vec3(5, 0, 0));

        // then
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("observer", "item"))
        );
        Asserts.assertEquals(item.getNearbyPlayerCount(), 0);

        // when
        room.setPlayerPosition(item, new Vec3(50, 0, 0));

        // then
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            Collections.singleton("observer"),
            false
        );

        // when
        room.setPlayerPosition(item, new Vec3(3, 0, 0));
        room.removePlayer(item);

        // then
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            Collections.singleton("observer"),
            false
        );
        Asserts.assertEquals(item.getNearbyPlayerCount(), 0);
    }

    @Test
    public void moveObserverAwayFromNonInterestPlayerTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer item = new MMOPlayer("item", false);
        room.setPlayerPosition(item, new Vec3(5, 0, 0));
        room.setPlayerPosition(observer, new Vec3(0, 0, 0));

        // when
        room.setPlayerPosition(observer, new Vec3(40, 0, 0));

        // then
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            Collections.singleton("observer"),
            false
        );
        Asserts.assertEquals(item.getNearbyPlayerCount(), 0);
    }

    @Test
    public void unlinkStaleNearbyPlayerAfterExternalPositionChangeTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        room.setPlayerPosition(player1, new Vec3(0, 0, 0));
        room.setPlayerPosition(player2, new Vec3(5, 0, 0));
        Vec3 farPosition = new Vec3(100, 0, 0);
        player1.setPosition(farPosition);

        // when
        room.setPlayerPosition(player1, farPosition);

        // then
        Asserts.assertEquals(
            new HashSet<>(player1.getNearbyPlayerNames()),
            Collections.singleton("player1"),
            false
        );
        Asserts.assertEquals(
            new HashSet<>(player2.getNearbyPlayerNames()),
            Collections.singleton("player2"),
            false
        );
    }

    @Test
    public void searchPlayersWithBufferTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        room.setPlayerPosition(player1, new Vec3(0, 0, 0));
        room.setPlayerPosition(player2, new Vec3(30, 0, 0));
        List<MMOPlayer> buffer = new ArrayList<>();

        // when
        room.searchPlayers(new Vec3(1, 0, 0), 5, buffer);

        // then
        Asserts.assertEquals(buffer, Collections.singletonList(player1), false);
    }

    @Test
    public void updateNearbyOnTickTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .updateNearbyOnTick(true)
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer item = new MMOPlayer("item", false);

        // when
        room.setPlayerPosition(observer, new Vec3(0, 0, 0));
        room.setPlayerPosition(item, new Vec3(5, 0, 0));

        // then
        Asserts.assertTrue(room.isUpdateNearbyOnTick());
        Asserts.assertEquals(observer.getNearbyPlayerCount(), 0);

        // when
        room.update();

        // then
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("observer", "item"))
        );
        Asserts.assertEquals(item.getNearbyPlayerCount(), 0);

        // when
        room.setPlayerPosition(item, new Vec3(50, 0, 0));

        // then
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("observer", "item"))
        );

        // when
        room.update();

        // then
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            Collections.singleton("observer"),
            false
        );
    }

    @Test
    public void updateNearbyOnTickRemovePlayerTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .updateNearbyOnTick(true)
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer other = new MMOPlayer("other");
        room.setPlayerPosition(observer, new Vec3(0, 0, 0));
        room.setPlayerPosition(other, new Vec3(5, 0, 0));
        room.update();

        // when
        room.removePlayer(other);

        // then
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            Collections.singleton("observer"),
            false
        );
        Asserts.assertEquals(other.getNearbyPlayerCount(), 0);
    }

    @Test
    public void nearbyUpdateIntervalTicksTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .updateNearbyOnTick(true)
            .nearbyUpdateIntervalTicks(2)
            .distanceOfInterest(10)
            .maxPlayer(20)
            .build();
        List<MMOPlayer> observers = new ArrayList<>();
        for (int i = 0; i < 6; ++i) {
            MMOPlayer observer = new MMOPlayer("observer" + i);
            room.setPlayerPosition(observer, new Vec3(i, 0, 0));
            observers.add(observer);
        }
        MMOPlayer item = new MMOPlayer("item", false);
        room.setPlayerPosition(item, new Vec3(5, 0, 0));

        // when
        room.update();
        room.update();

        // then
        Asserts.assertEquals(room.getNearbyUpdateIntervalTicks(), 2);
        for (MMOPlayer observer : observers) {
            Asserts.assertTrue(observer.getNearbyPlayerNames().contains("item"));
            Asserts.assertEquals(observer.getNearbyPlayerCount(), 7);
        }
        Asserts.assertEquals(item.getNearbyPlayerCount(), 0);
    }

    @Test
    public void nearbyUpdateIntervalTicksInvalidValueTest() {
        // given
        // when
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .updateNearbyOnTick(true)
            .nearbyUpdateIntervalTicks(0)
            .distanceOfInterest(10)
            .maxPlayer(20)
            .build();

        // then
        Asserts.assertEquals(room.getNearbyUpdateIntervalTicks(), 1);
    }

    @Test
    public void updateNearbyOnTickSkipPlayerNotInTurnTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .updateNearbyOnTick(true)
            .nearbyUpdateIntervalTicks(2)
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer playerA = new MMOPlayer("a");
        MMOPlayer playerB = new MMOPlayer("b");
        room.setPlayerPosition(playerA, new Vec3(0, 0, 0));
        room.setPlayerPosition(playerB, new Vec3(1, 0, 0));

        // when
        room.update();

        // then
        Asserts.assertEquals(
            new HashSet<>(playerA.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("a", "b"))
        );
        Asserts.assertEquals(playerB.getNearbyPlayerCount(), 0);

        // when
        room.update();

        // then
        Asserts.assertEquals(
            new HashSet<>(playerB.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("a", "b"))
        );
    }

    @Test
    public void updateNearbyOnTickSkipPlayerNotInGridTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .updateNearbyOnTick(true)
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer outsider = new MMOPlayer("outsider");
        room.setPlayerPosition(observer, new Vec3(0, 0, 0));
        room.setPlayerPosition(outsider, new Vec3(1, 0, 0));
        UniformGrid<MMOPlayer> grid = FieldUtil.getFieldValue(room, "grid");
        grid.remove(outsider);

        MMOPlayer stalePlayer = new MMOPlayer("stalePlayer");
        Map<String, MMOPlayer> outsiderNearbyPlayers =
            FieldUtil.getFieldValue(outsider, "nearbyPlayers");
        outsiderNearbyPlayers.put(stalePlayer.getName(), stalePlayer);

        // when
        room.update();

        // then
        Asserts.assertTrue(room.getPlayerManager().containsPlayer(outsider));
        Asserts.assertEquals(
            outsider.getNearbyPlayerNames(),
            Collections.singletonList("stalePlayer"),
            false
        );
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            Collections.singleton("observer"),
            false
        );
    }

    @Test
    public void updateNearbyOnTickSkipNonInterestPlayerTest() {
        // given
        MMOUniformGridRoom room = (MMOUniformGridRoom) MMOUniformGridRoom.builder()
            .updateNearbyOnTick(true)
            .distanceOfInterest(10)
            .maxPlayer(10)
            .build();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer item = new MMOPlayer("item", false);
        room.setPlayerPosition(observer, new Vec3(0, 0, 0));
        room.setPlayerPosition(item, new Vec3(1, 0, 0));

        // when
        room.update();

        // then
        Asserts.assertEquals(item.getNearbyPlayerCount(), 0);
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("observer", "item"))
        );
    }
}

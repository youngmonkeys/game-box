package com.tvd12.gamebox.testing;

import com.tvd12.ezyfox.collect.Lists;
import com.tvd12.gamebox.entity.MMOGridRoom;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.performance.Performance;
import com.tvd12.test.reflect.FieldUtil;
import com.tvd12.test.util.RandomUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Stream;

public class MMOGridRoomTest {
    
    public static void main(String[] args) {
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(50)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(800)
            .distanceOfInterest(4)
            .build();
        
        ArrayList<MMOPlayer> players = new ArrayList<>();
        
        for (int i = 0; i < room.getMaxPlayer(); ++i) {
            final MMOPlayer player = MMOPlayer.builder()
                .name("player" + i)
                .build();
            players.add(player);
            room.setPlayerPosition(
                player,
                new Vec3(
                    RandomUtil.randomFloat(0, room.getMaxX()),
                    RandomUtil.randomFloat(0, room.getMaxY()),
                    RandomUtil.randomFloat(0, room.getMaxZ())
                )
            );
            room.addPlayer(player);
        }
        
        final long elapsedTime = Performance
            .create()
            .loop(100000)
            .test(() -> {
                MMOPlayer player = players.get(RandomUtil.randomInt(0, room.getMaxPlayer()));
                room.setPlayerPosition(
                    player,
                    new Vec3(
                        RandomUtil.randomFloat(0, room.getMaxX()),
                        RandomUtil.randomFloat(0, room.getMaxY()),
                        RandomUtil.randomFloat(0, room.getMaxZ())
                    )
                );
            })
            .getTime();
        System.out.println("elapsed time: " + elapsedTime);
    }
    
    @Test
    public void createMMOGridRoomTest() {
        // given
        float maxX = RandomUtil.randomFloat(50, 100);
        float maxY = RandomUtil.randomFloat(50, 100);
        float maxZ = RandomUtil.randomFloat(50, 100);
        float cellSize = 1 + RandomUtil.randomSmallFloat();
        int maxPlayer = 1 + RandomUtil.randomSmallInt();
        
        // when
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(maxX)
            .maxY(maxY)
            .maxZ(maxZ)
            .cellSize(cellSize)
            .maxPlayer(maxPlayer)
            .distanceOfInterest(1 + RandomUtil.randomSmallInt())
            .build();
        
        // then
        Asserts.assertEquals(room.getMaxX(), maxX);
        Asserts.assertEquals(room.getMaxY(), maxY);
        Asserts.assertEquals(room.getMaxZ(), maxZ);
        Asserts.assertEquals(room.getCellSize(), cellSize);
    }
    
    @Test
    public void removePlayerTest() {
        // given
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(50)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(2)
            .distanceOfInterest(1)
            .build();

        MMOPlayer player1 = new MMOPlayer("player1");
        player1.setPosition(new Vec3(0, 0, 0));
        room.addPlayer(player1);

        MMOPlayer player2 = new MMOPlayer("player2");
        player2.setPosition(new Vec3(1, 1, 1));
        room.addPlayer(player2);

        // when
        room.removePlayer(player2);

        // then
        List<String> buffer1 = new ArrayList<>();
        List<String> buffer2 = new ArrayList<>();

        player1.getNearbyPlayerNames(buffer1);
        player2.getNearbyPlayerNames(buffer2);

        List<String> expectedNearbyPlayerNames1 = Collections.singletonList("player1");
        List<String> expectedNearbyPlayerNames2 = Collections.emptyList();

        Assert.assertEquals(buffer1, expectedNearbyPlayerNames1);
        Assert.assertEquals(buffer2, expectedNearbyPlayerNames2);
    }

    @Test
    public void setSinglePlayerPositionTest() {
        // given
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(50)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(3)
            .distanceOfInterest(1 + RandomUtil.randomSmallInt())
            .build();

        Vec3 expectedPosition = new Vec3(40, 40, 40);
        MMOPlayer player = new MMOPlayer("player");
        
        // when
        room.setPlayerPosition(player, expectedPosition);
        
        // then
        Asserts.assertEquals(player.getPosition(), expectedPosition);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setMultiplePlayerPositionsTest() {
        // given
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(50)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(5)
            .distanceOfInterest(1 + RandomUtil.randomSmallInt())
            .build();
        
        for (int i = 0; i < room.getMaxPlayer(); ++i) {
            MMOPlayer player = new MMOPlayer("player" + i);
            room.addPlayer(player);
        }
        List<MMOPlayer> players = room.getPlayerManager().getPlayerList();
        int numberOfRandomMoves = 1000;
        
        for (int i = 0; i < numberOfRandomMoves; ++i) {
            // when
            for (MMOPlayer player : players) {
                room.setPlayerPosition(
                    player,
                    new Vec3(
                        RandomUtil.randomFloat(0, room.getMaxX()),
                        RandomUtil.randomFloat(0, room.getMaxY()),
                        RandomUtil.randomFloat(0, room.getMaxZ())
                    )
                );
            }
            
            // then
            Map<String, List<String>> expectedNearbyPlayerNamesByPlayerName =
                computeExpectedNearbyPlayers(
                    players,
                    room.getCellSize(),
                    room.getCellRangeOfInterest()
                );
            
            for (MMOPlayer thisPlayer : players) {
                Asserts.assertEquals(
                    thisPlayer.getNearbyPlayerNames(),
                    expectedNearbyPlayerNamesByPlayerName.get(thisPlayer.getName())
                );
            }
        }
    }
    
    private Map<String, List<String>> computeExpectedNearbyPlayers(
        List<MMOPlayer> players,
        float cellSize,
        int cellRangeOfInterest
    ) {
        Map<String, List<String>> nearbyPlayerNamesByPlayerName = new HashMap<>();
        for (int i = 0; i < players.size(); ++i) {
            MMOPlayer thisPlayer = players.get(i);
            int cellX = (int) (thisPlayer.getPosition().x / cellSize);
            int cellY = (int) (thisPlayer.getPosition().y / cellSize);
            int cellZ = (int) (thisPlayer.getPosition().z / cellSize);
            for (int j = i; j < players.size(); ++j) {
                MMOPlayer otherPlayer = players.get(j);
                int otherCellX = (int) (otherPlayer.getPosition().x / cellSize);
                int otherCellY = (int) (otherPlayer.getPosition().y / cellSize);
                int otherCellZ = (int) (otherPlayer.getPosition().z / cellSize);
                
                int cellDistance = Stream.of(
                        Math.abs(cellX - otherCellX),
                        Math.abs(cellY - otherCellY),
                        Math.abs(cellZ - otherCellZ)
                    )
                    .max(Integer::compareTo).get();
                
                if (cellDistance <= cellRangeOfInterest) {
                    addNearbyPlayers(nearbyPlayerNamesByPlayerName, thisPlayer, otherPlayer);
                }
            }
        }
        return nearbyPlayerNamesByPlayerName;
    }
    
    private void addNearbyPlayers(
        Map<String, List<String>> nearbyPlayerNamesByPlayerName,
        MMOPlayer thisPlayer,
        MMOPlayer otherPlayer
    ) {
        List<String> thisNearbyPlayers = nearbyPlayerNamesByPlayerName
            .computeIfAbsent(thisPlayer.getName(), s -> new ArrayList<>());
        thisNearbyPlayers.add(otherPlayer.getName());
        
        if (thisPlayer != otherPlayer) {
            List<String> otherNearbyPlayers = nearbyPlayerNamesByPlayerName
                .computeIfAbsent(otherPlayer.getName(), s -> new ArrayList<>());
            otherNearbyPlayers.add(thisPlayer.getName());
        }
    }
    
    @Test
    public void setPlayerPositionOutsideAreaTest() {
        // given
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(50)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(3)
            .distanceOfInterest(1 + RandomUtil.randomSmallInt())
            .build();
        
        List<Vec3> invalidPositions = Lists.newArrayList(
            new Vec3(50.1f, 50, 50),
            new Vec3(50.1f, 50.1f, 50),
            new Vec3(50.1f, 50, 50.1f),
            new Vec3(50.1f, 50.1f, 50.1f),
            new Vec3(50, 50.1f, 50),
            new Vec3(50, 50.1f, 50.1f),
            new Vec3(50, 50, 50.1f),
            new Vec3(-50, 50, 50),
            new Vec3(50, -50, 50),
            new Vec3(50, 50, -50)
        );
        MMOPlayer player = new MMOPlayer("player");
        
        for (Vec3 invalidPosition : invalidPositions) {
            // when
            Throwable e = Asserts.assertThrows(() -> room.setPlayerPosition(player, invalidPosition));
            
            // then
            Asserts.assertEquals(IllegalArgumentException.class.toString(), e.getClass().toString());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void updateMMOGridRoomTest() {
        // given
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(50)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(3)
            .distanceOfInterest(1 + RandomUtil.randomSmallInt())
            .build();

        MMOPlayer player1 = new MMOPlayer("player1");
        room.addPlayer(player1);

        MMOPlayer player2 = new MMOPlayer("player2");
        room.addPlayer(player2);

        MMOPlayer player3 = new MMOPlayer("player3");
        room.addPlayer(player3);
        
        // when
        room.update();

        // then
        List<String> buffer1 = new ArrayList<>();
        List<String> buffer2 = new ArrayList<>();
        List<String> buffer3 = new ArrayList<>();

        player1.getNearbyPlayerNames(buffer1);
        player2.getNearbyPlayerNames(buffer2);
        player3.getNearbyPlayerNames(buffer3);

        List<String> expectedNearbyPlayerNames1 =
            new ArrayList<>(((Map<String, MMOPlayer>) FieldUtil.getFieldValue(player1, "nearbyPlayers"))
                .keySet());
        List<String> expectedNearbyPlayerNames2 =
            new ArrayList<>(((Map<String, MMOPlayer>) FieldUtil.getFieldValue(player2, "nearbyPlayers"))
                .keySet());
        List<String> expectedNearbyPlayerNames3 =
            new ArrayList<>(((Map<String, MMOPlayer>) FieldUtil.getFieldValue(player3, "nearbyPlayers"))
                .keySet());
        
        Asserts.assertEquals(buffer1, expectedNearbyPlayerNames1);
        Asserts.assertEquals(buffer2, expectedNearbyPlayerNames2);
        Asserts.assertEquals(buffer3, expectedNearbyPlayerNames3);
    }

    @Test
    public void updateMMOGridRoomWithChangingNearbyPlayersTest() {
        // given
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(500)
            .maxY(500)
            .maxZ(500)
            .cellSize(50)
            .maxPlayer(3)
            .distanceOfInterest(1)
            .build();

        MMOPlayer player1 = new MMOPlayer("player1");
        player1.setPosition(new Vec3(0, 0, 0));
        room.addPlayer(player1);

        MMOPlayer player2 = new MMOPlayer("player2");
        player2.setPosition(new Vec3(0, 0, 0));
        room.addPlayer(player2);

        MMOPlayer player3 = new MMOPlayer("player3");
        player3.setPosition(new Vec3(0, 0, 0));
        room.addPlayer(player3);

        // when
        room.setPlayerPosition(player1, new Vec3(55, 55, 0));
        room.setPlayerPosition(player2, new Vec3(125, 125, 0));
        room.setPlayerPosition(player3, new Vec3(0, 0, 0));
        room.update();

        // then
        List<String> buffer1 = new ArrayList<>();
        List<String> buffer2 = new ArrayList<>();
        List<String> buffer3 = new ArrayList<>();

        player1.getNearbyPlayerNames(buffer1);
        player2.getNearbyPlayerNames(buffer2);
        player3.getNearbyPlayerNames(buffer3);

        List<String> expectedNearbyPlayerNames1 = Arrays.asList("player1", "player2", "player3");
        List<String> expectedNearbyPlayerNames2 = Arrays.asList("player1", "player2");
        List<String> expectedNearbyPlayerNames3 = Arrays.asList("player1", "player3");

        Assert.assertEquals(buffer1, expectedNearbyPlayerNames1);
        Assert.assertEquals(buffer2, expectedNearbyPlayerNames2);
        Assert.assertEquals(buffer3, expectedNearbyPlayerNames3);
    }

    @Test
    public void removePlayerWithNearbyPlayerNotInRoomTest() {
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(50)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(2)
            .distanceOfInterest(1)
            .build();

        MMOPlayer player = new MMOPlayer("player");
        player.setPosition(new Vec3(0, 0, 0));
        room.addPlayer(player);

        MMOPlayer outsidePlayer = new MMOPlayer("outsidePlayer");
        Map<String, MMOPlayer> nearbyPlayers = FieldUtil.getFieldValue(player, "nearbyPlayers");
        nearbyPlayers.put(outsidePlayer.getName(), outsidePlayer);

        room.removePlayer(player);

        Asserts.assertTrue(player.getNearbyPlayerNames().isEmpty());
        Asserts.assertFalse(room.getPlayerManager().containsPlayer(player.getName()));
    }

    @Test
    public void setPlayerPositionAtMaxBoundTest() {
        // given
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(52)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(3)
            .distanceOfInterest(1)
            .build();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");

        // when
        room.setPlayerPosition(player1, new Vec3(52, 50, 50));
        room.setPlayerPosition(player2, new Vec3(51, 49, 49));

        // then
        Asserts.assertEquals(
            new HashSet<>(player1.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("player1", "player2"))
        );
    }

    @Test
    public void setPlayerPositionAddsPlayerToRoomTest() {
        // given
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(50)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(3)
            .distanceOfInterest(1)
            .build();
        MMOPlayer player = new MMOPlayer("player");

        // when
        room.setPlayerPosition(player, new Vec3(10, 10, 10));

        // then
        Asserts.assertTrue(room.getPlayerManager().containsPlayer(player));
    }

    @Test
    public void addPlayerOutsideAreaTest() {
        // given
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(50)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(3)
            .distanceOfInterest(1)
            .build();
        MMOPlayer player = new MMOPlayer("player");
        player.setPosition(new Vec3(60, 0, 0));

        // when
        Throwable e = Asserts.assertThrows(() -> room.addPlayer(player));

        // then
        Asserts.assertEquals(IllegalArgumentException.class, e.getClass());
        Asserts.assertFalse(room.getPlayerManager().containsPlayer(player));
    }

    @Test
    public void addPlayerTwiceTest() {
        // given
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(50)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(3)
            .distanceOfInterest(1)
            .build();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        room.addPlayer(player1);
        player1.setPosition(new Vec3(40, 40, 40));

        // when
        room.addPlayer(player1);
        room.removePlayer(player1);
        room.addPlayer(player2);

        // then
        Asserts.assertEquals(
            player2.getNearbyPlayerNames(),
            Collections.singletonList("player2"),
            false
        );
    }

    @Test
    public void removeNotAddedPlayerTest() {
        // given
        final MMOGridRoom room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(50)
            .maxY(50)
            .maxZ(50)
            .cellSize(5)
            .maxPlayer(3)
            .distanceOfInterest(1)
            .build();
        MMOPlayer player = new MMOPlayer("player");

        // when
        room.removePlayer(player);

        // then
        Asserts.assertFalse(room.getPlayerManager().containsPlayer(player));
    }

    @Test
    public void nonInterestPlayerTest() {
        // given
        final MMOGridRoom room = newRoom();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer item = new MMOPlayer("item", false);
        MMOPlayer lateObserver = new MMOPlayer("lateObserver");

        // when
        room.setPlayerPosition(observer, new Vec3(5, 5, 5));
        room.setPlayerPosition(item, new Vec3(15, 5, 5));
        room.setPlayerPosition(lateObserver, new Vec3(25, 5, 5));

        // then
        Asserts.assertEquals(sortedNearbyNames(observer), Arrays.asList("item", "observer"));
        Asserts.assertEquals(sortedNearbyNames(lateObserver), Arrays.asList("item", "lateObserver"));
        Asserts.assertEquals(item.getNearbyPlayerCount(), 0);
    }

    @Test
    public void moveNonInterestPlayerTest() {
        // given
        final MMOGridRoom room = newRoom();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer item = new MMOPlayer("item", false);
        room.setPlayerPosition(observer, new Vec3(5, 5, 5));
        room.setPlayerPosition(item, new Vec3(15, 5, 5));

        // when
        room.setPlayerPosition(item, new Vec3(85, 5, 5));

        // then
        Asserts.assertEquals(sortedNearbyNames(observer), Collections.singletonList("observer"));
        Asserts.assertEquals(item.getNearbyPlayerCount(), 0);

        // when
        room.setPlayerPosition(item, new Vec3(12, 5, 5));

        // then
        Asserts.assertEquals(sortedNearbyNames(observer), Arrays.asList("item", "observer"));
        Asserts.assertEquals(item.getNearbyPlayerCount(), 0);
    }

    @Test
    public void removeNonInterestPlayerTest() {
        // given
        final MMOGridRoom room = newRoom();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer item = MMOPlayer.builder()
            .name("item")
            .interestEnabled(false)
            .build();
        room.setPlayerPosition(observer, new Vec3(5, 5, 5));
        room.setPlayerPosition(item, new Vec3(15, 5, 5));

        // when
        room.removePlayer(item);

        // then
        Asserts.assertEquals(sortedNearbyNames(observer), Collections.singletonList("observer"));
        Asserts.assertFalse(room.getPlayerManager().containsPlayer(item));
    }

    @Test
    public void removeObserverNearNonInterestPlayerTest() {
        // given
        final MMOGridRoom room = newRoom();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer item = new MMOPlayer("item", false);
        room.setPlayerPosition(observer, new Vec3(5, 5, 5));
        room.setPlayerPosition(item, new Vec3(15, 5, 5));

        // when
        room.removePlayer(observer);

        // then
        Asserts.assertEquals(observer.getNearbyPlayerCount(), 0);
        Asserts.assertEquals(item.getNearbyPlayerCount(), 0);
    }

    @Test
    public void searchPlayersTest() {
        // given
        final MMOGridRoom room = newRoom();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2", false);
        MMOPlayer player3 = new MMOPlayer("player3");
        room.setPlayerPosition(player1, new Vec3(5, 5, 5));
        room.setPlayerPosition(player2, new Vec3(12, 5, 5));
        room.setPlayerPosition(player3, new Vec3(50, 50, 50));

        // when
        List<MMOPlayer> result = room.searchPlayers(new Vec3(5, 5, 5), 8);

        // then
        Asserts.assertEquals(sortedNames(result), Arrays.asList("player1", "player2"));
    }

    @Test
    public void searchPlayersWithBufferTest() {
        // given
        final MMOGridRoom room = newRoom();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        room.setPlayerPosition(player1, new Vec3(5, 5, 5));
        room.setPlayerPosition(player2, new Vec3(95, 95, 95));
        List<MMOPlayer> buffer = new ArrayList<>();

        // when
        room.searchPlayers(new Vec3(100, 100, 100), 10, buffer);

        // then
        Asserts.assertEquals(sortedNames(buffer), Collections.singletonList("player2"));
    }

    @Test
    public void searchPlayersInvalidRangeOrOutsideTest() {
        // given
        final MMOGridRoom room = newRoom();
        MMOPlayer player = new MMOPlayer("player");
        room.setPlayerPosition(player, new Vec3(5, 5, 5));

        // when
        List<MMOPlayer> negativeRangeResult = room.searchPlayers(new Vec3(5, 5, 5), -1);
        List<MMOPlayer> outsideResult = room.searchPlayers(new Vec3(-50, -50, -50), 10);
        List<MMOPlayer> beyondMaxResult = room.searchPlayers(new Vec3(500, 500, 500), 10);

        // then
        Asserts.assertTrue(negativeRangeResult.isEmpty());
        Asserts.assertTrue(outsideResult.isEmpty());
        Asserts.assertTrue(beyondMaxResult.isEmpty());
    }

    private static MMOGridRoom newRoom() {
        return (MMOGridRoom) MMOGridRoom.builder()
            .maxX(100)
            .maxY(100)
            .maxZ(100)
            .cellSize(10)
            .maxPlayer(100)
            .distanceOfInterest(1)
            .build();
    }

    private static List<String> sortedNearbyNames(MMOPlayer player) {
        List<String> names = player.getNearbyPlayerNames();
        Collections.sort(names);
        return names;
    }

    private static List<String> sortedNames(List<MMOPlayer> players) {
        List<String> names = new ArrayList<>();
        for (MMOPlayer player : players) {
            names.add(player.getName());
        }
        Collections.sort(names);
        return names;
    }
}

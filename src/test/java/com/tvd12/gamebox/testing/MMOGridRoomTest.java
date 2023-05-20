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
}

package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMOOcTreeRoom;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.performance.Performance;
import com.tvd12.test.reflect.FieldUtil;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MMOOcTreeRoomTest {
    
    public static void main(String[] args) {
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(50, 50, 50))
            .maxPointsPerNode(5)
            .maxPlayer(800)
            .distanceOfInterest(7.5f)
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
                    RandomUtil.randomFloat(room.getLeftBottomBack().x, room.getRightTopFront().x),
                    RandomUtil.randomFloat(room.getLeftBottomBack().y, room.getRightTopFront().y),
                    RandomUtil.randomFloat(room.getLeftBottomBack().z, room.getRightTopFront().z)
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
                        RandomUtil.randomFloat(room.getLeftBottomBack().x, room.getRightTopFront().x),
                        RandomUtil.randomFloat(room.getLeftBottomBack().y, room.getRightTopFront().y),
                        RandomUtil.randomFloat(room.getLeftBottomBack().z, room.getRightTopFront().z)
                    )
                );
            })
            .getTime();
        System.out.println("elapsed time: " + elapsedTime);
    }
    
    @Test
    public void createRoomWithoutSettingMaxPointsPerNodeTest() {
        // given
        MMOOcTreeRoom.Builder roomBuilder = (MMOOcTreeRoom.Builder) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(2, 2, 2))
            .maxPlayer(3)
            .distanceOfInterest(0.5);
        
        // when
        Throwable e = Asserts.assertThrows(roomBuilder::build);
        
        // then
        Asserts.assertEquals(IllegalArgumentException.class.toString(), e.getClass().toString());
    }

    @Test
    public void setPlayerPositionOutsideRoomAreaTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(2, 2, 2))
            .maxPointsPerNode(3)
            .maxPlayer(3)
            .distanceOfInterest(0.5)
            .build();

        Vec3 expectedPlayer1Position = new Vec3(0, 0, 0);
        Vec3 expectedPlayer2Position = new Vec3(1, 1, 1);
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");

        // when
        room.setPlayerPosition(player1, new Vec3(2.1f, 1.5f, 1.5f));
        room.setPlayerPosition(player2, expectedPlayer2Position);
        room.setPlayerPosition(player2, new Vec3(2.1f, 1.5f, 1.5f));

        // then
        Asserts.assertEquals(player1.getPosition(), expectedPlayer1Position);
        Asserts.assertEquals(player2.getPosition(), expectedPlayer2Position);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void setMultiplePlayerPositionsTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(50, 50, 50))
            .maxPointsPerNode(2)
            .minNodeSize(1f)
            .maxPlayer(5)
            .distanceOfInterest(7.5)
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
                        RandomUtil.randomFloat(room.getLeftBottomBack().x, room.getRightTopFront().x),
                        RandomUtil.randomFloat(room.getLeftBottomBack().y, room.getRightTopFront().y),
                        RandomUtil.randomFloat(room.getLeftBottomBack().z, room.getRightTopFront().z)
                    )
                );
            }
        
            // then
            Map<String, List<String>> expectedNearbyPlayerNamesByPlayerName =
                computeExpectedNearbyPlayers(
                    players,
                    room.getDistanceOfInterest()
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
        double distanceOfInterest
    ) {
        Map<String, List<String>> nearbyPlayerNamesByPlayerName = new HashMap<>();
        for (int i = 0; i < players.size(); ++i) {
            MMOPlayer thisPlayer = players.get(i);
            for (int j = i; j < players.size(); ++j) {
                MMOPlayer otherPlayer = players.get(j);
                
                float maxAxisDistance = Stream.of(
                        Math.abs(thisPlayer.getPosition().x - otherPlayer.getPosition().x),
                        Math.abs(thisPlayer.getPosition().y - otherPlayer.getPosition().y),
                        Math.abs(thisPlayer.getPosition().z - otherPlayer.getPosition().z)
                    )
                    .max(Float::compareTo).get();
                
                if (maxAxisDistance <= distanceOfInterest) {
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
    
    @SuppressWarnings("unchecked")
    @Test
    public void updateMMOOcTreeRoomTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(50, 50, 50))
            .maxPointsPerNode(5)
            .maxPlayer(5)
            .distanceOfInterest(7.5)
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
    public void searchNearByPlayersTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(2, 2, 2))
            .maxPointsPerNode(3)
            .maxPlayer(4)
            .distanceOfInterest(0.3)
            .build();

        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        MMOPlayer player3 = new MMOPlayer("player3");
        MMOPlayer player4 = new MMOPlayer("player4");
        
        // when
        room.setPlayerPosition(player1, new Vec3(0.49f, 0.5f, 0.5f));
        room.setPlayerPosition(player2, new Vec3(0.3f, 0.3f, 0.3f));
        room.setPlayerPosition(player3, new Vec3(0.25f, 0.25f, 0.25f));
        room.setPlayerPosition(player4, new Vec3(0.4f, 0.3f, 0.2f));
        room.setPlayerPosition(player1, new Vec3(0.5f, 0.5f, 0.5f));
        
        // then
        Asserts.assertEquals(player3.getNearbyPlayerNames().size(), 4);
    }
}

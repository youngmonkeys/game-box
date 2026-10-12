package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMOGridRoom;
import com.tvd12.gamebox.entity.MMOOcTreeRoom;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.entity.Player;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.gamebox.octree.OcTree;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.performance.Performance;
import com.tvd12.test.reflect.FieldUtil;
import com.tvd12.test.util.RandomUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

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

        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");

        // when
        Throwable e1 = Asserts.assertThrows(() ->
            room.setPlayerPosition(player1, new Vec3(2.1f, 1.5f, 1.5f))
        );
        room.setPlayerPosition(player2, new Vec3(1, 1, 1));
        Throwable e2 = Asserts.assertThrows(() ->
            room.setPlayerPosition(player2, new Vec3(2.1f, 1.5f, 1.5f))
        );

        // then
        Asserts.assertEquals(IllegalArgumentException.class.toString(), e1.getClass().toString());
        Asserts.assertEquals(IllegalArgumentException.class.toString(), e2.getClass().toString());
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
    
    @Test
    public void createRoomWithMinNodeSizeEqualsZeroTest() {
        // given
        MMORoom.Builder builder = MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(2, 2, 2))
            .minNodeSize(0f)
            .maxPointsPerNode(3)
            .maxPlayer(4)
            .distanceOfInterest(0.3);
        
        // when
        Throwable e = Asserts.assertThrows(builder::build);
        
        // then
        Asserts.assertEquals(IllegalArgumentException.class.toString(), e.getClass().toString());
    }


    @Test
    public void removePlayerTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(10, 10, 10))
            .maxPointsPerNode(3)
            .maxPlayer(2)
            .distanceOfInterest(5)
            .build();

        MMOPlayer player1 = new MMOPlayer("player1");
        room.setPlayerPosition(player1, new Vec3(0, 0, 0));

        MMOPlayer player2 = new MMOPlayer("player2");
        room.setPlayerPosition(player2, new Vec3(1, 1, 1));

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
    public void setOutsidePositionForNewPlayerTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(10, 10, 10))
            .maxPointsPerNode(3)
            .maxPlayer(2)
            .distanceOfInterest(5)
            .build();
        MMOPlayer player = new MMOPlayer("player");

        // when
        Throwable e = Asserts.assertThrows(() ->
            room.setPlayerPosition(player, new Vec3(11, 0, 0))
        );

        // then
        Asserts.assertEquals(IllegalArgumentException.class, e.getClass());
        Asserts.assertFalse(room.getPlayerManager().containsPlayer(player));
    }

    @Test
    public void setOutsidePositionForExistingPlayerTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(10, 10, 10))
            .maxPointsPerNode(3)
            .maxPlayer(2)
            .distanceOfInterest(5)
            .build();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        room.setPlayerPosition(player1, new Vec3(1, 1, 1));
        room.setPlayerPosition(player2, new Vec3(2, 2, 2));

        // when
        Throwable e = Asserts.assertThrows(() ->
            room.setPlayerPosition(player2, new Vec3(11, 0, 0))
        );
        room.setPlayerPosition(player1, new Vec3(1, 1, 1));

        // then
        Asserts.assertEquals(IllegalArgumentException.class, e.getClass());
        Asserts.assertEquals(player2.getPosition(), new Vec3(2, 2, 2));
        Asserts.assertEquals(
            new HashSet<>(player1.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("player1", "player2"))
        );
    }

    @Test
    public void removeNotAddedPlayerTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(10, 10, 10))
            .maxPointsPerNode(3)
            .maxPlayer(2)
            .distanceOfInterest(5)
            .build();
        MMOPlayer player = new MMOPlayer("player");

        // when
        room.removePlayer(player);

        // then
        Asserts.assertFalse(room.getPlayerManager().containsPlayer(player));
    }

    @Test
    public void removePlayerWithNearbyPlayerNotInRoomTest() {
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(10, 10, 10))
            .maxPointsPerNode(3)
            .maxPlayer(2)
            .distanceOfInterest(5)
            .build();

        MMOPlayer player = new MMOPlayer("player");
        room.setPlayerPosition(player, new Vec3(0, 0, 0));

        MMOPlayer outsidePlayer = new MMOPlayer("outsidePlayer");
        Map<String, MMOPlayer> nearbyPlayers = FieldUtil.getFieldValue(player, "nearbyPlayers");
        nearbyPlayers.put(outsidePlayer.getName(), outsidePlayer);

        room.removePlayer(player);

        Asserts.assertTrue(player.getNearbyPlayerNames().isEmpty());
        Asserts.assertFalse(room.getPlayerManager().containsPlayer(player.getName()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setPositionForNewPlayerButInsertFailedTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(10, 10, 10))
            .maxPointsPerNode(3)
            .maxPlayer(2)
            .distanceOfInterest(5)
            .build();
        OcTree<MMOPlayer> ocTree = mock(OcTree.class);
        when(ocTree.contains(any())).thenReturn(false);
        when(ocTree.insert(any())).thenReturn(false);
        FieldUtil.setFieldValue(room, "ocTree", ocTree);
        MMOPlayer player = new MMOPlayer("player");

        // when
        Throwable e = Asserts.assertThrows(() ->
            room.setPlayerPosition(player, new Vec3(1, 1, 1))
        );

        // then
        Asserts.assertEquals(IllegalArgumentException.class, e.getClass());
        Asserts.assertFalse(room.getPlayerManager().containsPlayer(player));
        verify(ocTree, times(1)).contains(player);
        verify(ocTree, times(1)).insert(player);
        verify(ocTree, never()).search(any(MMOPlayer.class), anyFloat());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setPositionForExistingPlayerButInsertFailedTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(10, 10, 10))
            .maxPointsPerNode(3)
            .maxPlayer(2)
            .distanceOfInterest(5)
            .build();
        MMOPlayer player = new MMOPlayer("player");
        room.setPlayerPosition(player, new Vec3(1, 1, 1));

        OcTree<MMOPlayer> ocTree = mock(OcTree.class);
        when(ocTree.contains(any())).thenReturn(true);
        when(ocTree.isItemRemainingAtSameNode(any(), any())).thenReturn(false);
        when(ocTree.insert(any())).thenReturn(false);
        FieldUtil.setFieldValue(room, "ocTree", ocTree);

        // when
        Throwable e = Asserts.assertThrows(() ->
            room.setPlayerPosition(player, new Vec3(9, 9, 9))
        );

        // then
        Asserts.assertEquals(IllegalArgumentException.class, e.getClass());
        verify(ocTree, times(1)).remove(player);
        verify(ocTree, times(1)).insert(player);
        verify(ocTree, times(1)).search(any(MMOPlayer.class), anyFloat());
    }

    @Test
    public void removeNotMMOPlayerTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(10, 10, 10))
            .maxPointsPerNode(3)
            .maxPlayer(2)
            .distanceOfInterest(5)
            .build();
        Player player = new Player("player");

        // when
        Throwable e = Asserts.assertThrows(() -> room.removePlayer(player));

        // then
        Asserts.assertEquals(IllegalArgumentException.class, e.getClass());
    }

    @Test
    public void nonInterestPlayerTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(100, 100, 100))
            .maxPointsPerNode(3)
            .maxPlayer(10)
            .distanceOfInterest(10)
            .build();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer item = new MMOPlayer("item", false);

        // when
        room.setPlayerPosition(observer, new Vec3(1, 1, 1));
        room.setPlayerPosition(item, new Vec3(5, 1, 1));

        // then
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("observer", "item"))
        );
        Asserts.assertEquals(item.getNearbyPlayerCount(), 0);

        // when
        room.setPlayerPosition(item, new Vec3(80, 80, 80));

        // then
        Asserts.assertEquals(
            new HashSet<>(observer.getNearbyPlayerNames()),
            Collections.singleton("observer"),
            false
        );

        // when
        room.setPlayerPosition(item, new Vec3(3, 1, 1));
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
    public void movePlayersKeepsNearbyPlayersSymmetricTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(100, 100, 100))
            .maxPointsPerNode(3)
            .maxPlayer(10)
            .distanceOfInterest(10)
            .build();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        room.setPlayerPosition(player1, new Vec3(1, 1, 1));
        room.setPlayerPosition(player2, new Vec3(5, 1, 1));

        // when
        room.setPlayerPosition(player2, new Vec3(60, 60, 60));
        room.setPlayerPosition(player1, new Vec3(58, 60, 60));

        // then
        Asserts.assertEquals(
            new HashSet<>(player1.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("player1", "player2"))
        );
        Asserts.assertEquals(
            new HashSet<>(player2.getNearbyPlayerNames()),
            new HashSet<>(Arrays.asList("player1", "player2"))
        );
    }

    @Test
    public void searchPlayersUseSphereRangeTest() {
        // given
        final MMOOcTreeRoom room = newSearchRoom();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2", false);
        MMOPlayer player3 = new MMOPlayer("player3");
        room.setPlayerPosition(player1, new Vec3(10, 10, 10));
        room.setPlayerPosition(player2, new Vec3(17, 17, 17));
        room.setPlayerPosition(player3, new Vec3(50, 50, 50));

        // when
        List<MMOPlayer> insideBoxOutsideSphere = room.searchPlayers(new Vec3(10, 10, 10), 10);
        List<MMOPlayer> insideSphere = room.searchPlayers(new Vec3(10, 10, 10), 13);

        // then
        Asserts.assertEquals(insideBoxOutsideSphere, Collections.singletonList(player1), false);
        Asserts.assertEquals(
            new HashSet<>(insideSphere),
            new HashSet<>(Arrays.asList(player1, player2))
        );
    }

    @Test
    public void searchPlayersWithBufferKeepsExistingItemsTest() {
        // given
        final MMOOcTreeRoom room = newSearchRoom();
        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        MMOPlayer existing = new MMOPlayer("existing");
        room.setPlayerPosition(player1, new Vec3(10, 10, 10));
        room.setPlayerPosition(player2, new Vec3(17, 17, 17));
        List<MMOPlayer> buffer = new ArrayList<>();
        buffer.add(existing);

        // when
        room.searchPlayers(new Vec3(10, 10, 10), 10, buffer);

        // then
        Asserts.assertEquals(buffer, Arrays.asList(existing, player1), false);
    }

    @Test
    public void searchPlayersNegativeRangeTest() {
        // given
        final MMOOcTreeRoom room = newSearchRoom();
        MMOPlayer player = new MMOPlayer("player");
        room.setPlayerPosition(player, new Vec3(10, 10, 10));

        // when
        List<MMOPlayer> result = room.searchPlayers(new Vec3(10, 10, 10), -1);

        // then
        Asserts.assertTrue(result.isEmpty());
    }

    private static MMOOcTreeRoom newSearchRoom() {
        return (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(100, 100, 100))
            .maxPointsPerNode(2)
            .maxPlayer(10)
            .distanceOfInterest(5)
            .build();
    }

    @Test
    public void updateNearbyOnTickTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(100, 100, 100))
            .maxPointsPerNode(2)
            .updateNearbyOnTick(true)
            .maxPlayer(10)
            .distanceOfInterest(10)
            .build();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer item = new MMOPlayer("item", false);

        // when
        room.setPlayerPosition(observer, new Vec3(1, 1, 1));
        room.setPlayerPosition(item, new Vec3(5, 1, 1));

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
        room.setPlayerPosition(item, new Vec3(80, 80, 80));

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
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .leftBottomBack(new Vec3(0, 0, 0))
            .rightTopFront(new Vec3(100, 100, 100))
            .maxPointsPerNode(2)
            .updateNearbyOnTick(true)
            .maxPlayer(10)
            .distanceOfInterest(10)
            .build();
        MMOPlayer observer = new MMOPlayer("observer");
        MMOPlayer other = new MMOPlayer("other");
        room.setPlayerPosition(observer, new Vec3(1, 1, 1));
        room.setPlayerPosition(other, new Vec3(5, 1, 1));
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
}

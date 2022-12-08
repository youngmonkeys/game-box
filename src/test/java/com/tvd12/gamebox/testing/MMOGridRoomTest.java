package com.tvd12.gamebox.testing;

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
        
        for (int i = 0; i < room.getMaxPlayer(); ++i) {
            final MMOPlayer player = MMOPlayer.builder()
                .name("player" + i)
                .build();
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
            .loop(1)
            .test(room::update)
            .getTime();
        System.out.println("elapsed time: " + elapsedTime);
    }
    
    @Test
    public void createMMOGridRoomTest() {
        // given
        int maxX = RandomUtil.randomInt(50, 100);
        int maxY = RandomUtil.randomInt(50, 100);
        int maxZ = RandomUtil.randomInt(50, 100);
        int cellSize = 1 + RandomUtil.randomSmallInt();
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
    public void setPlayerPositionTest() {
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

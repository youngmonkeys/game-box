package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMOOcTreeRoom;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.performance.Performance;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class MMOOcTreeRoomTest {
    
    public static void main(String[] args) {
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .topLeftFront(new Vec3(0, 0, 0))
            .bottomRightBack(new Vec3(50, 50, 50))
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
                    RandomUtil.randomFloat(room.getTopLeftFront().x, room.getBottomRightBack().x),
                    RandomUtil.randomFloat(room.getTopLeftFront().y, room.getBottomRightBack().y),
                    RandomUtil.randomFloat(room.getTopLeftFront().z, room.getBottomRightBack().z)
                )
            );
            room.addPlayer(player);
        }
    
        final long elapsedTime = Performance
            .create()
            .loop(1000)
            .test(room::update)
            .getTime();
        System.out.println("elapsed time: " + elapsedTime);
    }

    @Test
    public void setPlayerPositionTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .topLeftFront(new Vec3(0, 0, 0))
            .bottomRightBack(new Vec3(2, 2, 2))
            .maxPointsPerNode(3)
            .maxPlayer(3)
            .distanceOfInterest(0.5)
            .build();

        Vec3 expectedPosition = new Vec3(1.5f, 1.5f, 1.5f);
        MMOPlayer player = new MMOPlayer("player");

        // when
        room.setPlayerPosition(player, expectedPosition);

        // then
        Asserts.assertEquals(player.getPosition(), expectedPosition);
    }
    
    @Test
    public void searchNearByPlayersTest() {
        // given
        final MMOOcTreeRoom room = (MMOOcTreeRoom) MMOOcTreeRoom.builder()
            .topLeftFront(new Vec3(0, 0, 0))
            .bottomRightBack(new Vec3(2, 2, 2))
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
        
        room.update();
        
        // then
        Asserts.assertEquals(player3.getNearbyPlayerNames().size(), 4);
    }
}

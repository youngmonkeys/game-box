package com.tvd12.gamebox.testing.entity.octree;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.octree.OcTree;
import com.tvd12.gamebox.entity.octree.OcVolume;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

public class OcTreeTest {

    @Test
    public void insertionTest() {
        // given
        int maxPoints = 3;
        Vec3 topLeftFront = new Vec3(0, 0, 0);
        Vec3 bottomRightBack = new Vec3(2, 2, 2);
        OcVolume ocVolume = new OcVolume(topLeftFront, bottomRightBack);
        OcTree ocTree = new OcTree(maxPoints, ocVolume);

        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
        
        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();

        // when
        boolean isPlayer1Added = ocTree.insert(player1, new Vec3(0.49f, 0.5f, 0.5f));
        boolean isPlayer2Added = ocTree.insert(player2, new Vec3(2.5f, 1.0f, 1.0f));

        // then
        Asserts.assertTrue(isPlayer1Added);
        Asserts.assertFalse(isPlayer2Added);
    }

    @Test
    public void removalTest() {
        // given
        int maxPoints = 3;
        Vec3 topLeftFront = new Vec3(0, 0, 0);
        Vec3 bottomRightBack = new Vec3(2, 2, 2);
        OcVolume ocVolume = new OcVolume(topLeftFront, bottomRightBack);
        OcTree ocTree = new OcTree(maxPoints, ocVolume);

        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();

        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();

        MMOPlayer player3 = MMOPlayer.builder()
            .name("player3")
            .build();

        MMOPlayer player4 = MMOPlayer.builder()
            .name("player4")
            .build();

        // when
        boolean isPlayer1Added = ocTree.insert(player1, new Vec3(0.49f, 0.5f, 0.5f));
        boolean isPlayer2Added = ocTree.insert(player2, new Vec3(0.3f, 0.3f, 0.3f));
        boolean isPlayer3Added = ocTree.insert(player3, new Vec3(0.2f, 0.2f, 0.3f));
        boolean isPlayer4Added = ocTree.insert(player4, new Vec3(0.4f, 0.3f, 0.2f));
        boolean isPlayer1Removed = ocTree.remove(player1);

        // then
        Asserts.assertTrue(isPlayer1Added);
        Asserts.assertTrue(isPlayer2Added);
        Asserts.assertTrue(isPlayer3Added);
        Asserts.assertTrue(isPlayer4Added);
        Asserts.assertTrue(isPlayer1Removed);
    }
}

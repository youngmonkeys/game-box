package com.tvd12.gamebox.testing.octree;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.gamebox.octree.OcTree;
import com.tvd12.gamebox.octree.SynchronizedOcTree;
import com.tvd12.test.assertion.Asserts;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class SynchronizedOcTreeTest {

    @Test
    public void insertionTest() {
        // given
        int maxPoints = 1;
        Vec3 topLeftFront = new Vec3(0, 0, 0);
        Vec3 bottomRightBack = new Vec3(2, 2, 2);
        OcTree<MMOPlayer> ocTree = new SynchronizedOcTree<>(
            topLeftFront,
            bottomRightBack,
            maxPoints,
            0.01f
        );

        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
        
        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();
    
        MMOPlayer player3 = MMOPlayer.builder()
            .name("player3")
            .build();

        // when
        boolean isPlayer1Added = ocTree.insert(player1, new Vec3(0.5f, 1.5f, 1.5f));
        boolean isPlayer2Added = ocTree.insert(player2, new Vec3(2.5f, 1.0f, 1.0f));
        boolean isPlayer3Added = ocTree.insert(player3, new Vec3(0.5f, 1.5f, 1.5f));

        // then
        Asserts.assertTrue(isPlayer1Added);
        Asserts.assertFalse(isPlayer2Added);
        Asserts.assertTrue(isPlayer3Added);
    }

    @Test
    public void removalTest() {
        // given
        int maxPoints = 3;
        Vec3 topLeftFront = new Vec3(0, 0, 0);
        Vec3 bottomRightBack = new Vec3(2, 2, 2);
        Vec3 outsidePosition = new Vec3(3, 3, 3);
        OcTree<MMOPlayer> ocTree = new SynchronizedOcTree<>(
            topLeftFront,
            bottomRightBack,
            maxPoints,
            0.01f
        );

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
        boolean isPlayer3Added = ocTree.insert(player3, new Vec3(0.3f, 0.3f, 0.3f));
        player3.setPosition(outsidePosition);
        boolean isPlayer1Removed = ocTree.remove(player1);
        boolean isPlayer3Removed = ocTree.remove(player3);
        boolean isPlayer4Removed = ocTree.remove(player4);

        // then
        Asserts.assertTrue(isPlayer1Added);
        Asserts.assertTrue(isPlayer2Added);
        Asserts.assertTrue(isPlayer3Added);
        Asserts.assertTrue(isPlayer1Removed);
        Asserts.assertFalse(isPlayer3Removed);
        Asserts.assertFalse(isPlayer4Removed);
    }
    
    @Test
    public void invalidRemovalTest() {
        // given
        int maxPoints = 1;
        Vec3 topLeftFront = new Vec3(0, 0, 0);
        Vec3 bottomRightBack = new Vec3(2, 2, 2);
        OcTree<MMOPlayer> ocTree = new SynchronizedOcTree<>(
            topLeftFront,
            bottomRightBack,
            maxPoints,
            0.01f
        );
        
        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
        
        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();
        
        // when
        boolean isPlayer1Added = ocTree.insert(player1, new Vec3(0.3f, 0.3f, 0.3f));
        boolean isPlayer2Added = ocTree.insert(player2, new Vec3(0.3f, 0.3f, 0.3f));
        player1.setPosition(new Vec3(1.5f, 1.5f, 1.5f));
        boolean isPlayer1Removed = ocTree.remove(player1);
        
        // then
        Asserts.assertTrue(isPlayer1Added);
        Asserts.assertTrue(isPlayer2Added);
        Asserts.assertFalse(isPlayer1Removed);
    }
    
    @Test
    public void searchingTest() {
        // given
        int maxPoints = 3;
        Vec3 topLeftFront = new Vec3(0, 0, 0);
        Vec3 bottomRightBack = new Vec3(2, 2, 2);
        OcTree<MMOPlayer> ocTree = new SynchronizedOcTree<>(
            topLeftFront,
            bottomRightBack,
            maxPoints,
            0.01f
        );
    
        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
    
        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();
        
        boolean isPlayer1Added = ocTree.insert(player1, new Vec3(0.5f, 0.5f, 0.5f));
        boolean isPlayer2Added = ocTree.insert(player2, new Vec3(0.3f, 0.3f, 0.3f));
        float searchRange = 0.2f;
    
        // when
        List<MMOPlayer> nearbyPlayers = ocTree.search(player1, searchRange);
    
        // then
        Asserts.assertTrue(isPlayer1Added);
        Asserts.assertTrue(isPlayer2Added);
        Assert.assertEquals(nearbyPlayers, Arrays.asList(player1, player2));
    }
}

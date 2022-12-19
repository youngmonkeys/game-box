package com.tvd12.gamebox.testing.octree;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.gamebox.octree.OcTree;
import com.tvd12.gamebox.octree.SynchronizedOcTree;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

public class OcTree2DTest {

    @Test
    public void insertAndSearchTest() {
        // given
        OcTree<MMOPlayer> ocTree = new SynchronizedOcTree<>(
            new Vec3(0, 0, 0),
            new Vec3(10, 10, 0),
            2,
            0
        );

        MMOPlayer player1 = new MMOPlayer("player1");
        MMOPlayer player2 = new MMOPlayer("player2");
        MMOPlayer player3 = new MMOPlayer("player3");

        // when
        ocTree.insert(player1, new Vec3(0, 5, 0));
        ocTree.insert(player2, new Vec3(0, 10.1F, 0));
        ocTree.insert(player3, new Vec3(0, 0, 0));

        // then
        Asserts.assertTrue(ocTree.contains(player1));
        Asserts.assertFalse(ocTree.contains(player2));
        Asserts.assertTrue(ocTree.contains(player3));
    }
}

package com.tvd12.gamebox.testing.octree;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.gamebox.octree.OcTree;
import com.tvd12.gamebox.octree.SynchronizedOcTree;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

public class OcTreeTest {
    
    @Test
    public void insertSamePositionsTest() {
        // given
        MMOPlayer player1 = new MMOPlayer("player1");
        player1.setPosition(new Vec3(6, 0, 6));
        MMOPlayer player2 = new MMOPlayer("player2");
        player2.setPosition(new Vec3(0, 0, 0));
        MMOPlayer player3 = new MMOPlayer("player3");
        player3.setPosition(new Vec3(0, 0, 0));
        
        OcTree<MMOPlayer> ocTree = new SynchronizedOcTree<>(
            new Vec3(0, 0, 0),
            new Vec3(10, 0, 10),
            1,
            5f
        );
    
        String expectedOcTreeString = 
            "{" +
                "root=(" +
                    "bounds=(0.0, 0.0, 0.0)->(10.0, 0.0, 10.0), " +
                    "items=[], children=[" +
                        "(bounds=(0.0, 0.0, 0.0)->(5.0, 0.0, 5.0), items=[player2, player3], children=[]), " +
                        "(bounds=(0.0, 0.0, 5.0)->(5.0, 0.0, 10.0), items=[], children=[]), " +
                        "(bounds=(0.0, 0.0, 0.0)->(5.0, 0.0, 5.0), items=[], children=[]), " +
                        "(bounds=(0.0, 0.0, 5.0)->(5.0, 0.0, 10.0), items=[], children=[]), " +
                        "(bounds=(5.0, 0.0, 0.0)->(10.0, 0.0, 5.0), items=[], children=[]), " +
                        "(bounds=(5.0, 0.0, 5.0)->(10.0, 0.0, 10.0), items=[player1], children=[]), " +
                        "(bounds=(5.0, 0.0, 0.0)->(10.0, 0.0, 5.0), items=[], children=[]), " +
                        "(bounds=(5.0, 0.0, 5.0)->(10.0, 0.0, 10.0), items=[], children=[])" +
                    "]" +
                "), " +
                "items=[player1, player2, player3]" +
            "}";
        
        // when
        ocTree.insert(player1);
        ocTree.insert(player2);
        ocTree.insert(player3);
    
        // then
        Asserts.assertEquals(ocTree.toString(), expectedOcTreeString);
        System.out.println(ocTree.toPrettyString());
    }
}

package com.tvd12.gamebox.testing.octree;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.PositionAware;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.gamebox.octree.OcTreeNode;
import com.tvd12.gamebox.octree.OcVolume;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.reflect.MethodUtil;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class OcTreeNodeTest {
    
    @Test
    public void insertItemToChildrenWithNullOutputTest() throws NoSuchMethodException {
        // given
        Vec3 topLeftFront = new Vec3(0, 0, 0);
        Vec3 bottomRightBack = new Vec3(2, 2, 2);
        Vec3 outsidePosition = new Vec3(3, 3, 3);
        OcVolume ocVolume = new OcVolume(topLeftFront, bottomRightBack);
        OcTreeNode<MMOPlayer> node = new OcTreeNode<>(ocVolume, 1);
        
        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
        
        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();
        
        MMOPlayer player3 = MMOPlayer.builder()
            .name("player3")
            .build();
        
        node.insert(player1, new Vec3(0.5f, 0.5f, 0.5f));
        node.insert(player2, new Vec3(1.5f, 1.5f, 1.5f));
        
        // when
        Method method = OcTreeNode.class.getDeclaredMethod(
            "insertItemToChildren",
            PositionAware.class,
            Vec3.class
        );
        method.setAccessible(true);
        Object nodeContainingInsertedItem = MethodUtil.invokeMethod(
            method,
            node,
            player3,
            outsidePosition
        );
        
        // then
        Asserts.assertNull(nodeContainingInsertedItem);
    }
    
    @Test
    public void findNodeContainingPositionFromChildrenWithNullOutputTest()
        throws NoSuchMethodException {
        // given
        Vec3 topLeftFront = new Vec3(0, 0, 0);
        Vec3 bottomRightBack = new Vec3(2, 2, 2);
        Vec3 outsidePosition = new Vec3(3, 3, 3);
        OcVolume ocVolume = new OcVolume(topLeftFront, bottomRightBack);
        OcTreeNode<MMOPlayer> node = new OcTreeNode<>(ocVolume, 1);
        
        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
        
        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();
        
        node.insert(player1, new Vec3(0.5f, 0.5f, 0.5f));
        node.insert(player2, new Vec3(1.5f, 1.5f, 1.5f));
        
        // when
        Method method = OcTreeNode.class.getDeclaredMethod(
            "findNodeContainingPositionFromChildren",
            Vec3.class
        );
        method.setAccessible(true);
        Object nodeContainingPosition = MethodUtil.invokeMethod(
            method,
            node,
            outsidePosition
        );
        
        // then
        Asserts.assertNull(nodeContainingPosition);
    }
}

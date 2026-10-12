package com.tvd12.gamebox.testing.octree;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.PositionAware;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.gamebox.octree.OcTreeNode;
import com.tvd12.gamebox.math.Bounds;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.reflect.MethodUtil;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class OcTreeNodeTest {
    
    @Test
    public void insertItemToChildrenWithNullOutputTest() throws NoSuchMethodException {
        // given
        Vec3 leftBottomBack = new Vec3(0, 0, 0);
        Vec3 rightTopFront = new Vec3(2, 2, 2);
        Vec3 outsidePosition = new Vec3(3, 3, 3);
        Bounds bounds = new Bounds(leftBottomBack, rightTopFront);
        OcTreeNode<MMOPlayer> node = new OcTreeNode<>(bounds, 1, 0.01f);
        
        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
        player1.setPosition(new Vec3(0.5f, 0.5f, 0.5f));
        
        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();
        player2.setPosition(new Vec3(1.5f, 1.5f, 1.5f));
        
        MMOPlayer player3 = MMOPlayer.builder()
            .name("player3")
            .build();
        player3.setPosition(outsidePosition);
        
        node.insert(player1);
        node.insert(player2);
        
        // when
        Method method = OcTreeNode.class.getDeclaredMethod(
            "insertItemToChildren",
            PositionAware.class
        );
        method.setAccessible(true);
        Object nodeContainingInsertedItem = MethodUtil.invokeMethod(
            method,
            node,
            player3
        );
        
        // then
        Asserts.assertNull(nodeContainingInsertedItem);
    }
    
    @Test
    public void findNodeContainingPositionFromChildrenWithNullOutputTest()
        throws NoSuchMethodException {
        // given
        Vec3 leftBottomBack = new Vec3(0, 0, 0);
        Vec3 rightTopFront = new Vec3(2, 2, 2);
        Vec3 outsidePosition = new Vec3(3, 3, 3);
        Bounds bounds = new Bounds(leftBottomBack, rightTopFront);
        OcTreeNode<MMOPlayer> node = new OcTreeNode<>(bounds, 1, 0.01f);
        
        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
        player1.setPosition(new Vec3(0.5f, 0.5f, 0.5f));
        
        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();
        player2.setPosition(new Vec3(1.5f, 1.5f, 1.5f));
        
        node.insert(player1);
        node.insert(player2);
        
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

    @Test
    public void removeFromAnyNodeWithMovedItemTest() throws NoSuchMethodException {
        // given
        Bounds bounds = new Bounds(new Vec3(0, 0, 0), new Vec3(2, 2, 2));
        OcTreeNode<MMOPlayer> node = new OcTreeNode<>(bounds, 1, 0.01f);

        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
        player1.setPosition(new Vec3(0.5f, 0.5f, 0.5f));

        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();
        player2.setPosition(new Vec3(1.5f, 1.5f, 1.5f));

        node.insert(player1);
        node.insert(player2);
        player2.setPosition(new Vec3(0.6f, 0.6f, 0.6f));

        // when
        boolean isRemovedByPosition = node.remove(player2);
        Object isRemovedFromAnyNode = MethodUtil.invokeMethod(
            getRemoveFromAnyNodeMethod(),
            node,
            player2
        );

        // then
        Asserts.assertFalse(isRemovedByPosition);
        Asserts.assertEquals(isRemovedFromAnyNode, true);
        Asserts.assertEquals(node.countItems(), 1);
        Asserts.assertTrue(node.isLeaf());
    }

    @Test
    public void removeFromAnyNodeWithNotExistedItemTest() throws NoSuchMethodException {
        // given
        Bounds bounds = new Bounds(new Vec3(0, 0, 0), new Vec3(2, 2, 2));
        OcTreeNode<MMOPlayer> node = new OcTreeNode<>(bounds, 1, 0.01f);

        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
        player1.setPosition(new Vec3(0.5f, 0.5f, 0.5f));

        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();
        player2.setPosition(new Vec3(1.5f, 1.5f, 1.5f));

        MMOPlayer player3 = MMOPlayer.builder()
            .name("player3")
            .build();
        player3.setPosition(new Vec3(0.5f, 0.5f, 0.5f));

        node.insert(player1);
        node.insert(player2);

        // when
        Object isRemoved = MethodUtil.invokeMethod(
            getRemoveFromAnyNodeMethod(),
            node,
            player3
        );

        // then
        Asserts.assertEquals(isRemoved, false);
        Asserts.assertEquals(node.countItems(), 2);
        Asserts.assertFalse(node.isLeaf());
    }

    @Test
    public void removeFromAnyNodeWithLeafNodeTest() throws NoSuchMethodException {
        // given
        Bounds bounds = new Bounds(new Vec3(0, 0, 0), new Vec3(2, 2, 2));
        OcTreeNode<MMOPlayer> node = new OcTreeNode<>(bounds, 2, 0.01f);

        MMOPlayer player1 = MMOPlayer.builder()
            .name("player1")
            .build();
        player1.setPosition(new Vec3(0.5f, 0.5f, 0.5f));

        MMOPlayer player2 = MMOPlayer.builder()
            .name("player2")
            .build();
        player2.setPosition(new Vec3(1.5f, 1.5f, 1.5f));

        node.insert(player1);

        // when
        Method method = getRemoveFromAnyNodeMethod();
        Object isPlayer2Removed = MethodUtil.invokeMethod(method, node, player2);
        Object isPlayer1Removed = MethodUtil.invokeMethod(method, node, player1);

        // then
        Asserts.assertEquals(isPlayer2Removed, false);
        Asserts.assertEquals(isPlayer1Removed, true);
        Asserts.assertEquals(node.countItems(), 0);
        Asserts.assertTrue(node.isLeaf());
    }

    private Method getRemoveFromAnyNodeMethod() throws NoSuchMethodException {
        Method method = OcTreeNode.class.getDeclaredMethod(
            "removeFromAnyNode",
            PositionAware.class
        );
        method.setAccessible(true);
        return method;
    }
}

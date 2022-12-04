package com.tvd12.gamebox.entity.octree;

import com.tvd12.gamebox.entity.PositionAware;
import com.tvd12.gamebox.math.Vec3;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OcTree<T extends PositionAware> {

    private final OcTreeNode<T> root;
    private final Map<T, OcTreeNode<T>> nodeByItem = new ConcurrentHashMap<>();

    @Getter
    private final int maxItemsPerNode;

    public OcTree(int maxItemsPerNode, OcVolume ocVolume) {
        this.maxItemsPerNode = maxItemsPerNode;
        this.root = new OcTreeNode<>(ocVolume, maxItemsPerNode);
    }
    
    public boolean insert(T item) {
        return insert(item, item.getPosition());
    }

    public boolean insert(T item, Vec3 position) {
        OcTreeNode<T> nodeHavingInsertedItem = this.root.insert(item, position);
        if (nodeHavingInsertedItem != null) {
            nodeByItem.put(item, nodeHavingInsertedItem);
        }
        return (nodeHavingInsertedItem != null);
    }
    
    public boolean remove(T item) {
        if (!nodeByItem.containsKey(item)) {
            return false;
        }
        boolean isItemRemoved = this.root.remove(item);
        if (isItemRemoved) {
            nodeByItem.remove(item);
        }
        return isItemRemoved;
    }

    public List<T> search(T item, float range) {
        Vec3 topLeftFront = new Vec3(item.getPosition());
        topLeftFront.subtract(new Vec3(range, range, range));
        Vec3 bottomRightBack = new Vec3(item.getPosition());
        bottomRightBack.add(new Vec3(range, range, range));
        OcVolume searchVolume = new OcVolume(topLeftFront, bottomRightBack);
        List<T> matches = new ArrayList<>();
        return this.root.search(searchVolume, matches);
    }
    
    public boolean contains(T item) {
        return nodeByItem.containsKey(item);
    }
    
    public OcTreeNode<T> getNodeContainingItem(T item) {
        return nodeByItem.get(item);
    }

    public OcTreeNode<T> findNodeContainingPosition(Vec3 position) {
        return this.root.findNodeContainingPosition(position);
    }
    
    public boolean checkItemRemainingAtSameNode(T item, Vec3 newPosition) {
        OcTreeNode<T> currentNode = getNodeContainingItem(item);
        OcTreeNode<T> newNode = findNodeContainingPosition(newPosition);
        return (currentNode == newNode);
    }
}

package com.tvd12.gamebox.octree;

import com.tvd12.gamebox.entity.PositionAware;
import com.tvd12.gamebox.math.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OcTree<T extends PositionAware> {
    
    private final OcTreeNode<T> root;
    private final Set<T> items;

    public OcTree(int maxItemsPerNode, OcVolume ocVolume) {
        this.root = new OcTreeNode<>(ocVolume, maxItemsPerNode);
        this.items = new HashSet<>();
    }
    
    public boolean insert(T item, Vec3 position) {
        OcTreeNode<T> nodeContainingInsertedItem = this.root.insert(item, position);
        if (nodeContainingInsertedItem != null) {
            items.add(item);
        }
        return (nodeContainingInsertedItem != null);
    }
    
    public boolean remove(T item) {
        if (!items.contains(item)) {
            return false;
        }
        boolean isItemRemoved = this.root.remove(item);
        if (isItemRemoved) {
            items.remove(item);
        }
        return isItemRemoved;
    }

    public List<T> search(T item, float range) {
        OcVolume searchVolume = OcVolume.fromCenterAndRange(item.getPosition(), range);
        List<T> matches = new ArrayList<>();
        return this.root.search(searchVolume, matches);
    }
    
    public boolean contains(T item) {
        return items.contains(item);
    }
    
    public OcTreeNode<T> findNodeContainingPosition(Vec3 position) {
        return this.root.findNodeContainingPosition(position);
    }
    
    public boolean isItemRemainingAtSameNode(T item, Vec3 newPosition) {
        OcTreeNode<T> currentNode = findNodeContainingPosition(item.getPosition());
        OcTreeNode<T> newNode = findNodeContainingPosition(newPosition);
        return (currentNode == newNode);
    }
}

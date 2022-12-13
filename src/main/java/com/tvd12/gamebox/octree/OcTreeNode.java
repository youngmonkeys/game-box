package com.tvd12.gamebox.octree;

import com.tvd12.gamebox.entity.PositionAware;
import com.tvd12.gamebox.math.Vec3;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OcTreeNode<T extends PositionAware> {

    private static final int NUM_CHILDREN = 8;
    private final Set<T> items = ConcurrentHashMap.newKeySet();
    private final List<OcTreeNode<T>> children = new ArrayList<>();
    @Setter
    private OcTreeNode<T> parentNode = null;
    private final OcVolume ocVolume;
    private final int maxItems;

    public OcTreeNode(OcVolume ocVolume, int maxItems) {
        this.ocVolume = ocVolume;
        this.maxItems = maxItems;
    }

    public OcTreeNode<T> insert(T newItem, Vec3 position) {
        if (!this.ocVolume.containsPosition(position)) {
            return null;
        }

        if (isLeaf()) {
            if (this.items.size() < maxItems) {
                this.items.add(newItem);
                return this;
            }
            createChildren();
            passItemsToChildren();
        }

        return insertItemToChildren(newItem, position);
    }

    private void createChildren() {
        for (int i = 0; i < NUM_CHILDREN; i++) {
            OcVolume volume = this.ocVolume.getOctant(OcLocation.of(i));
            OcTreeNode<T> child = new OcTreeNode<>(volume, maxItems);
            this.children.add(child);
            child.setParentNode(this);
        }
    }

    private void passItemsToChildren() {
        this.items.forEach(
            item -> insertItemToChildren(item, item.getPosition())
        );
        this.items.clear();
    }

    private OcTreeNode<T> insertItemToChildren(T item, Vec3 position) {
        for (int i = 0; i < NUM_CHILDREN; i++) {
            OcTreeNode<T> nodeContainingInsertedItem = this.children.get(i)
                .insert(item, position);
            if (nodeContainingInsertedItem != null) {
                return nodeContainingInsertedItem;
            }
        }
        return null;
    }

    public boolean remove(T item) {
        if (!this.ocVolume.containsPosition(item.getPosition())) {
            return false;
        }
        if (isLeaf()) {
            return removeItemFromThisLeaf(item);
        }
        return removeFromChildren(item);
    }

    private boolean removeItemFromThisLeaf(T item) {
        if (!this.items.contains(item)) {
            return false;
        }
        this.items.remove(item);
        tryMergingChildrenOfParentNode();
        return true;
    }

    private boolean removeFromChildren(T item) {
        for (int i = 0; i < NUM_CHILDREN; i++) {
            boolean isPlayerRemoved = this.children.get(i)
                .remove(item);
            if (isPlayerRemoved) {
                return true;
            }
        }
        return false;
    }

    private void tryMergingChildrenOfParentNode() {
        if (this.parentNode != null && this.parentNode.countItems() <= maxItems) {
            this.parentNode.mergeChildren();
        }
    }

    private void mergeChildren() {
        List<T> itemsInChildren = new ArrayList<>();
        getItemsInChildren(itemsInChildren);
        this.items.addAll(itemsInChildren);
        this.children.clear();
        tryMergingChildrenOfParentNode();
    }

    private void getItemsInChildren(List<T> players) {
        if (isLeaf()) {
            players.addAll(this.items);
            this.items.clear();
            return;
        }
        for (int i = 0; i < NUM_CHILDREN; i++) {
            this.children.get(i).getItemsInChildren(players);
        }
    }

    public int countItems() {
        if (isLeaf()) {
            return this.items.size();
        }
        return countItemsFromChildren();
    }

    private int countItemsFromChildren() {
        int count = 0;
        for (int i = 0; i < NUM_CHILDREN; i++) {
            count += this.children.get(i).countItems();
        }
        return count;
    }

    public List<T> search(OcVolume searchVolume, List<T> matches) {
        if (!this.ocVolume.doesOverlap(searchVolume)) {
            return matches;
        }
        if (isLeaf()) {
            return searchFromThisLeaf(searchVolume, matches);
        }
        return searchFromChildren(searchVolume, matches);
    }

    private List<T> searchFromThisLeaf(OcVolume searchVolume, List<T> matches) {
        for (T item : this.items) {
            if (searchVolume.containsPosition(item.getPosition())) {
                matches.add(item);
            }
        }
        return matches;
    }

    private List<T> searchFromChildren(OcVolume searchVolume, List<T> matches) {
        for (int i = 0; i < NUM_CHILDREN; i++) {
            this.children.get(i)
                .search(searchVolume, matches);
        }
        return matches;
    }

    public OcTreeNode<T> findNodeContainingPosition(Vec3 position) {
        if (!this.ocVolume.containsPosition(position)) {
            return null;
        }
        if (isLeaf()) {
            return this;
        }
        return findNodeContainingPositionFromChildren(position);
    }

    private OcTreeNode<T> findNodeContainingPositionFromChildren(Vec3 position) {
        for (int i = 0; i < NUM_CHILDREN; i++) {
            OcTreeNode<T> node = this.children.get(i)
                .findNodeContainingPosition(position);
            if (node != null) {
                return node;
            }
        }
        return null;
    }
    
    public boolean isLeaf() {
        return this.children.size() == 0;
    }
}

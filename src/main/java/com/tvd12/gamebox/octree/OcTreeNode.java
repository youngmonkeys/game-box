package com.tvd12.gamebox.octree;

import com.tvd12.gamebox.entity.PositionAware;
import com.tvd12.gamebox.math.Bounds;
import com.tvd12.gamebox.math.Vec3;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OcTreeNode<T extends PositionAware> {
    
    @Setter
    private OcTreeNode<T> parentNode = null;
    private final int maxItems;
    private final float minNodeSize;
    private final Bounds bounds;
    private final Set<T> items = new HashSet<>();
    private final List<OcTreeNode<T>> children = new ArrayList<>();
    
    private static final int NUM_CHILDREN = 8;

    public OcTreeNode(Bounds bounds, int maxItems, float minNodeSize) {
        if (minNodeSize <= 0) {
            throw new IllegalArgumentException(
                "minNodeSize must > 0 to avoid StackOverflow"
            );
        }
        
        this.bounds = bounds;
        this.maxItems = maxItems;
        this.minNodeSize = minNodeSize;
    }

    public OcTreeNode<T> insert(T newItem) {
        if (!this.bounds.containsPosition(newItem.getPosition())) {
            return null;
        }

        if (isLeaf()) {
            if (this.items.size() < maxItems || this.bounds.getMaxDimension() < 2 * minNodeSize) {
                this.items.add(newItem);
                return this;
            }
            createChildren();
            passItemsToChildren();
        }

        return insertItemToChildren(newItem);
    }

    private void createChildren() {
        for (int i = 0; i < NUM_CHILDREN; ++i) {
            Bounds bounds = this.bounds.getOctant(i);
            OcTreeNode<T> child = new OcTreeNode<>(bounds, maxItems, minNodeSize);
            this.children.add(child);
            child.setParentNode(this);
        }
    }

    private void passItemsToChildren() {
        this.items.forEach(
            this::insertItemToChildren
        );
        this.items.clear();
    }

    private OcTreeNode<T> insertItemToChildren(T item) {
        for (OcTreeNode<T> child : this.children) {
            OcTreeNode<T> nodeContainingInsertedItem = child.insert(item);
            if (nodeContainingInsertedItem != null) {
                return nodeContainingInsertedItem;
            }
        }
        return null;
    }

    public boolean remove(T item) {
        if (!this.bounds.containsPosition(item.getPosition())) {
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
        for (OcTreeNode<T> child : this.children) {
            boolean isPlayerRemoved = child.remove(item);
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
        for (OcTreeNode<T> child : this.children) {
            child.getItemsInChildren(players);
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
        for (OcTreeNode<T> child : this.children) {
            count += child.countItems();
        }
        return count;
    }

    public List<T> search(Bounds searchBounds, List<T> matches) {
        if (!this.bounds.doesOverlap(searchBounds)) {
            return matches;
        }
        if (isLeaf()) {
            return searchFromThisLeaf(searchBounds, matches);
        }
        return searchFromChildren(searchBounds, matches);
    }

    private List<T> searchFromThisLeaf(Bounds searchBounds, List<T> matches) {
        for (T item : this.items) {
            if (searchBounds.containsPosition(item.getPosition())) {
                matches.add(item);
            }
        }
        return matches;
    }

    private List<T> searchFromChildren(Bounds searchBounds, List<T> matches) {
        for (OcTreeNode<T> child : this.children) {
            child.search(searchBounds, matches);
        }
        return matches;
    }

    protected OcTreeNode<T> findNodeContainingPosition(Vec3 position) {
        if (!this.bounds.containsPosition(position)) {
            return null;
        }
        if (isLeaf()) {
            return this;
        }
        return findNodeContainingPositionFromChildren(position);
    }

    private OcTreeNode<T> findNodeContainingPositionFromChildren(Vec3 position) {
        for (OcTreeNode<T> child : this.children) {
            OcTreeNode<T> node = child.findNodeContainingPosition(position);
            if (node != null) {
                return node;
            }
        }
        return null;
    }
    
    public boolean isLeaf() {
        return this.children.isEmpty();
    }
    
    @Override
    public String toString() {
        return '(' +
            "bounds=" + bounds +
            ", items=" + items +
            ", children=" + children +
            ')';
    }
    
    public String toPrettyString(int level) {
        String spaces = level <= 0
            ? ""
            : String.format("%" + level * 2 + 's', "");
        return spaces + "(\n" +
            spaces + "  bounds=" + bounds + ",\n" +
            spaces + "  items=" + items + ",\n" +
            spaces + (
                children.isEmpty()
                    ? "  children=[]\n"
                    : "  children=[\n" + children.stream()
                        .map(it -> it.toPrettyString(level + 1))
                        .collect(Collectors.joining(",\n")) +
                        '\n' + spaces + "  ]\n"
            ) + spaces + ')';
    }
}

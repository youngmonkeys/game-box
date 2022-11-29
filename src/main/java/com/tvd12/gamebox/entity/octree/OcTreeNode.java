package com.tvd12.gamebox.entity.octree;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OcTreeNode {

    private final Map<String, MMOPlayer> playerByName = new ConcurrentHashMap<>();
    private final List<OcTreeNode> children = new ArrayList<>();
    @Setter
    private OcTreeNode parentNode = null;
    private final OcVolume ocVolume;
    private final OcTree ocTree;
    private final int maxPoints;

    public OcTreeNode(OcTree ocTree, OcVolume ocVolume) {
        this.ocTree = ocTree;
        this.ocVolume = ocVolume;
        this.maxPoints = ocTree.getMaxPoints();
    }

    public OcTreeNode(
        OcTree ocTree,
        Vec3 topLeftFront,
        Vec3 bottomRightBack
    ) {
        this(ocTree, new OcVolume(topLeftFront, bottomRightBack));
    }

    public boolean insert(MMOPlayer player, Vec3 position) {
        if (!this.ocVolume.containsPoint(position)) {
            return false;
        }

        if (this.playerByName.size() < maxPoints) {
            player.setPosition(position);
            this.playerByName.put(player.getName(), player);
            this.ocTree.saveNodeByPlayerName(player.getName(), this);
            return true;
        }

        if (this.children.size() == 0) {
            createOctants();
        }
        
        this.playerByName.values().forEach(
            mmoPlayer -> insertPlayerToOneOctant(mmoPlayer, mmoPlayer.getPosition())
        );
        this.playerByName.clear();
        return insertPlayerToOneOctant(player, position);
    }

    public boolean remove(MMOPlayer player) {
        if (!this.ocVolume.containsPoint(player.getPosition())) {
            return false;
        }
        if (this.children.size() > 0) {
            for (int i = 0; i < 8; i++) {
                boolean isPlayerDeleted = this.children.get(i)
                    .remove(player);
                if (isPlayerDeleted) {
                    return true;
                }
            }
            return false;
        }
        if (!playerByName.containsKey(player.getName())) {
            return false;
        } else {
            playerByName.remove(player.getName());
            tryMergingChildrenOfParentNode();
            return true;
        }
    }

    private void tryMergingChildrenOfParentNode() {
        if (parentNode != null && parentNode.countPoints() <= maxPoints) {
            this.parentNode.merge();
        }
    }
    
    private void merge() {
        List<MMOPlayer> players = new ArrayList<>();
        merge(players);
        players.forEach(player -> this.playerByName.put(player.getName(), player));
        this.children.clear();
        tryMergingChildrenOfParentNode();
    }
    
    private void merge(List<MMOPlayer> players) {
        if (this.children.size() == 0) {
            players.addAll(this.playerByName.values());
            playerByName.clear();
            return;
        }
        for (int i = 0; i < 8; i++) {
            this.children.get(i).merge(players);
        }
    }

    public int countPoints() {
        if (this.children.size() == 0) {
            return playerByName.size();
        }
        int count = 0;
        for (int i = 0; i < 8; i++) {
            count += this.children.get(i).countPoints();
        }
        return count;
    }
    
    public List<MMOPlayer> search(OcVolume searchVolume, List<MMOPlayer> matches) {
        if (matches == null) {
            matches = new ArrayList<>();
        }
        if (!this.ocVolume.doesOverlap(searchVolume)) {
            return matches;
        } else {
            for (MMOPlayer player : playerByName.values()) {
                if (searchVolume.containsPoint(player.getPosition())) {
                    matches.add(player);
                }
            }
            if (this.children.size() > 0) {
                for (int i = 0; i < 8; i++) {
                    children.get(i)
                        .search(searchVolume, matches);
                }
            }
        }
        return matches;
    }
    
    private boolean insertPlayerToOneOctant(MMOPlayer player, Vec3 position) {
        for (int i = 0; i < 8; i++) {
            boolean isPlayerAdded = this.children.get(i)
                .insert(player, position);
            if (isPlayerAdded) {
                return true;
            }
        }
        return false;
    }
   
    private void createOctants() {
        for (int i = 0; i < 8; i++) {
            OcVolume volume = this.ocVolume.getOctant(OcLocation.of(i));
            OcTreeNode child = new OcTreeNode(ocTree, volume);
            children.add(child);
            child.setParentNode(this);
        }
    }

    public OcTreeNode findNodeByPosition(Vec3 position) {
        if (!ocVolume.containsPoint(position)) {
            return null;
        }
        if (this.children.size() == 0) {
            return this;
        }
        for (int i = 0; i < 8; i++) {
            OcTreeNode node = this.children.get(i)
                .findNodeByPosition(position);
            if (node != null) {
                return node;
            }
        }
        return null;
    }
}

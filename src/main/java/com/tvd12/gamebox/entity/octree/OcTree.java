package com.tvd12.gamebox.entity.octree;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OcTree {

    private final OcTreeNode root;
    private final Map<String, OcTreeNode> nodeByPlayerName = new ConcurrentHashMap<>();

    @Getter
    private final int maxPoints;

    public OcTree(int maxPoints, OcVolume ocVolume) {
        this.maxPoints = maxPoints;
        this.root = new OcTreeNode(this, ocVolume);
    }
    
    public boolean insert(MMOPlayer player) {
        return insert(player, player.getPosition());
    }

    public boolean insert(MMOPlayer player, Vec3 position) {
        return this.root.insert(player, position);
    }
    
    public boolean remove(MMOPlayer player) {
        if (!nodeByPlayerName.containsKey(player.getName())) {
            return false;
        }
        boolean isPlayerRemoved = this.root.remove(player);
        if (isPlayerRemoved) {
            nodeByPlayerName.remove(player.getName());
        }
        return isPlayerRemoved;
    }

    public List<MMOPlayer> search(MMOPlayer player, float range) {
        Vec3 topLeftFront = new Vec3(player.getPosition());
        topLeftFront.subtract(new Vec3(range, range, range));
        Vec3 bottomRightBack = new Vec3(player.getPosition());
        bottomRightBack.add(new Vec3(range, range, range));
        OcVolume searchVolume = new OcVolume(topLeftFront, bottomRightBack);
        List<MMOPlayer> matches = new ArrayList<>();
        return this.root.search(searchVolume, matches);
    }
    
    public void saveNodeByPlayerName(String playerName, OcTreeNode node) {
        nodeByPlayerName.put(playerName, node);
    }
    
    public boolean containsPlayer(MMOPlayer player) {
        return nodeByPlayerName.containsKey(player.getName());
    }
    
    public OcTreeNode getNodeByPlayerName(String playerName) {
        return nodeByPlayerName.get(playerName);
    }

    public OcTreeNode findNodeByPosition(Vec3 position) {
        return this.root.findNodeByPosition(position);
    }
}

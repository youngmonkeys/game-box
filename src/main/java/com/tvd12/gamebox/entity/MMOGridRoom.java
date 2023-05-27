package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Vec3;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MMOGridRoom extends MMORoom {

    @Getter
    private final float maxX;
    
    @Getter
    private final float maxY;
    
    @Getter
    private final float maxZ;
    
    @Getter
    private final float cellSize;
    
    @Getter
    private final int cellRangeOfInterest;
    
    private final Map<Player, Cell> cellByPlayer;
    private final Cell[][][] cells;
    private final int maxCellX;
    private final int maxCellY;
    private final int maxCellZ;

    public MMOGridRoom(Builder builder) {
        super(builder);
        this.maxX = builder.maxX;
        this.maxY = builder.maxY;
        this.maxZ = builder.maxZ;
        this.cellSize = builder.cellSize;
        this.cellByPlayer = new ConcurrentHashMap<>();
        this.cellRangeOfInterest = (int) (builder.distanceOfInterest);
        maxCellX = Math.max(1, (int) (maxX / cellSize));
        maxCellY = Math.max(1, (int) (maxY / cellSize));
        maxCellZ = Math.max(1, (int) (maxZ / cellSize));
        this.cells = new Cell[maxCellX][maxCellY][maxCellZ];
        initializeCells();
    }

    private void initializeCells() {
        for (int ix = 0; ix < maxCellX; ++ix) {
            for (int iy = 0; iy < maxCellY; ++iy) {
                for (int iz = 0; iz < maxCellZ; ++iz) {
                    Cell cell = new Cell();
                    cell.setCellX(ix);
                    cell.setCellY(iy);
                    cell.setCellZ(iz);
                    this.cells[ix][iy][iz] = cell;
                }
            }
        }
    }

    @Override
    public void addPlayer(Player player) {
        super.addPlayer(player);
        addPlayerToCell((MMOPlayer) player);
    }

    @Override
    public void removePlayer(Player player) {
        cellByPlayer.get(player)
            .removePlayer((MMOPlayer) player);
        cellByPlayer.remove(player);
        clearCurrentNearbyPlayers((MMOPlayer) player);
        super.removePlayer(player);
    }

    private void clearCurrentNearbyPlayers(MMOPlayer player) {
        for (String otherPlayerName : player.getNearbyPlayerNames()) {
            MMOPlayer otherPlayer = (MMOPlayer) playerManager.getPlayer(otherPlayerName);
            if (otherPlayer != null) {
                otherPlayer.removeNearByPlayer(player);
            }
        }
        player.clearNearByPlayers();
    }

    private void addPlayerToCell(MMOPlayer player) {
        int cellX = (int) (player.getPosition().x / cellSize);
        int cellY = (int) (player.getPosition().y / cellSize);
        int cellZ = (int) (player.getPosition().z / cellSize);
        addPlayerToCell(player, cellX, cellY, cellZ);
    }
    
    private void addPlayerToCell(MMOPlayer player, int cellX, int cellY, int cellZ) {
        Cell cell = this.cells[cellX][cellY][cellZ];
        cell.addPlayer(player);
        cellByPlayer.put(player, cell);

        updateNearbyPlayers(player, cellX, cellY, cellZ);
    }

    private void updateNearbyPlayers(MMOPlayer player, int cellX, int cellY, int cellZ) {
        clearCurrentNearbyPlayers(player);
        handleNeighboringCells(player, cellX, cellY, cellZ);
    }

    private void handleNeighboringCells(
        MMOPlayer currentPlayer,
        int cellX,
        int cellY,
        int cellZ
    ) {
        final int cellOfInterestEndX = Math.min(maxCellX - 1, cellX + cellRangeOfInterest);
        final int cellOfInterestEndY = Math.min(maxCellY - 1, cellY + cellRangeOfInterest);
        final int cellOfInterestEndZ = Math.min(maxCellZ - 1, cellZ + cellRangeOfInterest);
        final int cellOfInterestStartX = Math.max(0, cellX - cellRangeOfInterest);
        final int cellOfInterestStartY = Math.max(0, cellY - cellRangeOfInterest);
        final int cellOfInterestStartZ = Math.max(0, cellZ - cellRangeOfInterest);

        for (int ix = cellOfInterestStartX; ix <= cellOfInterestEndX; ++ix) {
            for (int iy = cellOfInterestStartY; iy <= cellOfInterestEndY; ++iy) {
                for (int iz = cellOfInterestStartZ; iz <= cellOfInterestEndZ; ++iz) {
                    addNearbyPlayersInCell(currentPlayer, cells[ix][iy][iz]);
                }
            }
        }
    }

    private void addNearbyPlayersInCell(MMOPlayer currentPlayer, Cell cell) {
        for (MMOPlayer nearByPlayer : cell.players) {
            currentPlayer.addNearbyPlayer(nearByPlayer);
            nearByPlayer.addNearbyPlayer(currentPlayer);
        } 
    }

    public void setPlayerPosition(MMOPlayer player, Vec3 position) {
        if (!isPositionInsideRoom(position)) {
            throw new IllegalArgumentException("Position is outside of the room's area");
        }

        final Cell oldCell = cellByPlayer.get(player);

        if (oldCell == null) {
            player.setPosition(position);
            addPlayerToCell(player);
            return;
        }

        int cellX = (int) (position.x / cellSize);
        int cellY = (int) (position.y / cellSize);
        int cellZ = (int) (position.z / cellSize);

        player.setPosition(position);

        if (oldCell.cellX != cellX || oldCell.cellY != cellY || oldCell.cellZ != cellZ) {
            oldCell.removePlayer(player);
            addPlayerToCell(player, cellX, cellY, cellZ);
        }
    }

    private boolean isPositionInsideRoom(Vec3 position) {
        return position.x >= 0 && position.x <= maxX
            && position.y >= 0 && position.y <= maxY
            && position.z >= 0 && position.z <= maxZ;
    }

    @Override
    protected void updatePlayers() {
        // do nothing
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends MMORoom.Builder {

        private float maxX;
        private float maxY;
        private float maxZ;
        private float cellSize;

        public Builder maxX(float maxX) {
            this.maxX = maxX;
            return this;
        }

        public Builder maxY(float maxY) {
            this.maxY = maxY;
            return this;
        }

        public Builder maxZ(float maxZ) {
            this.maxZ = maxZ;
            return this;
        }

        public Builder cellSize(float cellSize) {
            this.cellSize = cellSize;
            return this;
        }

        @Override
        public Builder distanceOfInterest(double distance) {
            this.distanceOfInterest = distance;
            return this;
        }

        @Override
        protected MMORoom newProduct() {
            return new MMOGridRoom(this);
        }
    }

    private static class Cell {
        
        @Setter
        private int cellX;
        
        @Setter
        private int cellY;
        
        @Setter
        private int cellZ;
        
        private final Set<MMOPlayer> players = ConcurrentHashMap.newKeySet();
        
        public void addPlayer(MMOPlayer player) {
            players.add(player);
        }
        
        public void removePlayer(MMOPlayer player) {
            players.remove(player);
        }
    }
}

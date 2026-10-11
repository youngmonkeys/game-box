package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Vec3;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

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
        this.cellByPlayer = new HashMap<>();
        this.cellRangeOfInterest = (int) (builder.distanceOfInterest);
        maxCellX = Math.max(1, (int) Math.ceil(maxX / cellSize));
        maxCellY = Math.max(1, (int) Math.ceil(maxY / cellSize));
        maxCellZ = Math.max(1, (int) Math.ceil(maxZ / cellSize));
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
        synchronized (this) {
            MMOPlayer mmoPlayer = (MMOPlayer) player;
            if (!isPositionInsideRoom(mmoPlayer.getPosition())) {
                throw new IllegalArgumentException("Position is outside of the room's area");
            }
            super.addPlayer(player);
            if (!cellByPlayer.containsKey(player)) {
                addPlayerToCell(mmoPlayer);
            }
        }
    }

    @Override
    public void removePlayer(Player player) {
        synchronized (this) {
            MMOPlayer mmoPlayer = (MMOPlayer) player;
            Cell cell = cellByPlayer.remove(player);
            if (cell != null) {
                cell.removePlayer(mmoPlayer);
            }
            clearCurrentNearbyPlayers(mmoPlayer);
            super.removePlayer(player);
        }
    }

    private void clearCurrentNearbyPlayers(MMOPlayer player) {
        for (MMOPlayer otherPlayer : player.getNearbyPlayers()) {
            otherPlayer.removeNearByPlayer(player);
        }
        player.clearNearByPlayers();
    }

    private void addPlayerToCell(MMOPlayer player) {
        Vec3 position = player.getPosition();
        addPlayerToCell(
            player,
            toCellIndex(position.x, maxCellX),
            toCellIndex(position.y, maxCellY),
            toCellIndex(position.z, maxCellZ)
        );
    }
    
    private void addPlayerToCell(
        MMOPlayer player,
        int cellX,
        int cellY,
        int cellZ
    ) {
        Cell cell = this.cells[cellX][cellY][cellZ];
        cell.addPlayer(player);
        cellByPlayer.put(player, cell);

        updateNearbyPlayers(player, cellX, cellY, cellZ);
    }

    private int toCellIndex(float value, int maxCell) {
        return Math.min(maxCell - 1, (int) (value / cellSize));
    }

    private void updateNearbyPlayers(
        MMOPlayer player,
        int cellX,
        int cellY,
        int cellZ
    ) {
        clearCurrentNearbyPlayers(player);
        handleNeighboringCells(player, cellX, cellY, cellZ);
    }

    private void handleNeighboringCells(
        MMOPlayer currentPlayer,
        int cellX,
        int cellY,
        int cellZ
    ) {
        final int cellOfInterestEndX = toCellOfInterestEnd(cellX, maxCellX);
        final int cellOfInterestEndY = toCellOfInterestEnd(cellY, maxCellY);
        final int cellOfInterestEndZ = toCellOfInterestEnd(cellZ, maxCellZ);
        final int cellOfInterestStartX = toCellOfInterestStart(cellX);
        final int cellOfInterestStartY = toCellOfInterestStart(cellY);
        final int cellOfInterestStartZ = toCellOfInterestStart(cellZ);

        for (int ix = cellOfInterestStartX; ix <= cellOfInterestEndX; ++ix) {
            for (int iy = cellOfInterestStartY; iy <= cellOfInterestEndY; ++iy) {
                for (int iz = cellOfInterestStartZ; iz <= cellOfInterestEndZ; ++iz) {
                    addNearbyPlayersInCell(currentPlayer, cells[ix][iy][iz]);
                }
            }
        }
    }

    private void moveNearbyPlayers(
        MMOPlayer player,
        Cell oldCell,
        int cellX,
        int cellY,
        int cellZ
    ) {
        final int oldEndX = toCellOfInterestEnd(oldCell.cellX, maxCellX);
        final int oldEndY = toCellOfInterestEnd(oldCell.cellY, maxCellY);
        final int oldEndZ = toCellOfInterestEnd(oldCell.cellZ, maxCellZ);
        for (int ix = toCellOfInterestStart(oldCell.cellX); ix <= oldEndX; ++ix) {
            for (int iy = toCellOfInterestStart(oldCell.cellY); iy <= oldEndY; ++iy) {
                for (int iz = toCellOfInterestStart(oldCell.cellZ); iz <= oldEndZ; ++iz) {
                    if (!isCellOfInterest(ix, iy, iz, cellX, cellY, cellZ)) {
                        removeNearbyPlayersInCell(player, cells[ix][iy][iz]);
                    }
                }
            }
        }

        final int newEndX = toCellOfInterestEnd(cellX, maxCellX);
        final int newEndY = toCellOfInterestEnd(cellY, maxCellY);
        final int newEndZ = toCellOfInterestEnd(cellZ, maxCellZ);
        for (int ix = toCellOfInterestStart(cellX); ix <= newEndX; ++ix) {
            for (int iy = toCellOfInterestStart(cellY); iy <= newEndY; ++iy) {
                for (int iz = toCellOfInterestStart(cellZ); iz <= newEndZ; ++iz) {
                    if (!isCellOfInterest(
                        ix,
                        iy,
                        iz,
                        oldCell.cellX,
                        oldCell.cellY,
                        oldCell.cellZ
                    )) {
                        addNearbyPlayersInCell(player, cells[ix][iy][iz]);
                    }
                }
            }
        }
    }

    private int toCellOfInterestStart(int cell) {
        return Math.max(0, cell - cellRangeOfInterest);
    }

    private int toCellOfInterestEnd(int cell, int maxCell) {
        return Math.min(maxCell - 1, cell + cellRangeOfInterest);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isCellOfInterest(
        int cellX,
        int cellY,
        int cellZ,
        int centerCellX,
        int centerCellY,
        int centerCellZ
    ) {
        return Math.abs(cellX - centerCellX) <= cellRangeOfInterest
            && Math.abs(cellY - centerCellY) <= cellRangeOfInterest
            && Math.abs(cellZ - centerCellZ) <= cellRangeOfInterest;
    }

    private void addNearbyPlayersInCell(
        MMOPlayer currentPlayer,
        Cell cell
    ) {
        for (MMOPlayer nearByPlayer : cell.players) {
            currentPlayer.addNearbyPlayer(nearByPlayer);
            nearByPlayer.addNearbyPlayer(currentPlayer);
        }
    }

    private void removeNearbyPlayersInCell(
        MMOPlayer currentPlayer,
        Cell cell
    ) {
        for (MMOPlayer nearByPlayer : cell.players) {
            currentPlayer.removeNearByPlayer(nearByPlayer);
            nearByPlayer.removeNearByPlayer(currentPlayer);
        }
    }

    public void setPlayerPosition(
        MMOPlayer player,
        Vec3 position
    ) {
        if (!isPositionInsideRoom(position)) {
            throw new IllegalArgumentException(
                "Position is outside of the room's area"
            );
        }

        synchronized (this) {
            final Cell oldCell = cellByPlayer.get(player);

            if (oldCell == null) {
                player.setPosition(position);
                addPlayer(player);
                return;
            }

            int cellX = toCellIndex(position.x, maxCellX);
            int cellY = toCellIndex(position.y, maxCellY);
            int cellZ = toCellIndex(position.z, maxCellZ);

            player.setPosition(position);

            if (oldCell.cellX != cellX
                || oldCell.cellY != cellY
                || oldCell.cellZ != cellZ
            ) {
                oldCell.removePlayer(player);
                moveNearbyPlayers(player, oldCell, cellX, cellY, cellZ);
                Cell newCell = cells[cellX][cellY][cellZ];
                newCell.addPlayer(player);
                cellByPlayer.put(player, newCell);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
        
        private final Set<MMOPlayer> players = new HashSet<>();
        
        public void addPlayer(MMOPlayer player) {
            players.add(player);
        }
        
        public void removePlayer(MMOPlayer player) {
            players.remove(player);
        }
    }
}

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
    private final Cell[] cells;
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
        this.cellRangeOfInterest = builder.cellRangeOfInterest > 0
            ? builder.cellRangeOfInterest
            : (int) Math.ceil(builder.distanceOfInterest / builder.cellSize);
        this.maxCellX = Math.max(1, (int) Math.ceil(maxX / cellSize));
        this.maxCellY = Math.max(1, (int) Math.ceil(maxY / cellSize));
        this.maxCellZ = Math.max(1, (int) Math.ceil(maxZ / cellSize));
        this.cells = new Cell[
            Math.multiplyExact(Math.multiplyExact(maxCellX, maxCellY), maxCellZ)
        ];
        initializeCells();
    }

    private void initializeCells() {
        for (int ix = 0; ix < maxCellX; ++ix) {
            for (int iy = 0; iy < maxCellY; ++iy) {
                final int rowIndex = toCellRowIndex(ix, iy);
                for (int iz = 0; iz < maxCellZ; ++iz) {
                    Cell cell = new Cell();
                    cell.setCellX(ix);
                    cell.setCellY(iy);
                    cell.setCellZ(iz);
                    this.cells[rowIndex + iz] = cell;
                }
            }
        }
    }

    private int toCellRowIndex(int cellX, int cellY) {
        return (cellX * maxCellY + cellY) * maxCellZ;
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
                if (!mmoPlayer.isInterestEnabled()) {
                    removeFromNeighboringObservers(mmoPlayer, cell);
                }
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

    private void removeFromNeighboringObservers(MMOPlayer player, Cell cell) {
        final int endX = toCellOfInterestEnd(cell.cellX, maxCellX);
        final int endY = toCellOfInterestEnd(cell.cellY, maxCellY);
        final int endZ = toCellOfInterestEnd(cell.cellZ, maxCellZ);
        final int startY = toCellOfInterestStart(cell.cellY);
        final int startZ = toCellOfInterestStart(cell.cellZ);
        for (int ix = toCellOfInterestStart(cell.cellX); ix <= endX; ++ix) {
            for (int iy = startY; iy <= endY; ++iy) {
                final int rowIndex = toCellRowIndex(ix, iy);
                for (int iz = startZ; iz <= endZ; ++iz) {
                    for (MMOPlayer observer : cells[rowIndex + iz].players) {
                        if (observer.isInterestEnabled()) {
                            observer.removeNearByPlayer(player);
                        }
                    }
                }
            }
        }
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
        Cell cell = this.cells[toCellRowIndex(cellX, cellY) + cellZ];
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
                final int rowIndex = toCellRowIndex(ix, iy);
                for (int iz = cellOfInterestStartZ; iz <= cellOfInterestEndZ; ++iz) {
                    addNearbyPlayersInCell(currentPlayer, cells[rowIndex + iz]);
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
        final int oldStartY = toCellOfInterestStart(oldCell.cellY);
        final int oldStartZ = toCellOfInterestStart(oldCell.cellZ);
        for (int ix = toCellOfInterestStart(oldCell.cellX); ix <= oldEndX; ++ix) {
            final boolean inNewX = isCellOfInterest(ix, cellX);
            for (int iy = oldStartY; iy <= oldEndY; ++iy) {
                final boolean inNewXY = inNewX && isCellOfInterest(iy, cellY);
                final int rowIndex = toCellRowIndex(ix, iy);
                for (int iz = oldStartZ; iz <= oldEndZ; ++iz) {
                    if (!inNewXY || !isCellOfInterest(iz, cellZ)) {
                        removeNearbyPlayersInCell(player, cells[rowIndex + iz]);
                    }
                }
            }
        }

        final int newEndX = toCellOfInterestEnd(cellX, maxCellX);
        final int newEndY = toCellOfInterestEnd(cellY, maxCellY);
        final int newEndZ = toCellOfInterestEnd(cellZ, maxCellZ);
        final int newStartY = toCellOfInterestStart(cellY);
        final int newStartZ = toCellOfInterestStart(cellZ);
        for (int ix = toCellOfInterestStart(cellX); ix <= newEndX; ++ix) {
            final boolean inOldX = isCellOfInterest(ix, oldCell.cellX);
            for (int iy = newStartY; iy <= newEndY; ++iy) {
                final boolean inOldXY = inOldX && isCellOfInterest(iy, oldCell.cellY);
                final int rowIndex = toCellRowIndex(ix, iy);
                for (int iz = newStartZ; iz <= newEndZ; ++iz) {
                    if (!inOldXY || !isCellOfInterest(iz, oldCell.cellZ)) {
                        addNearbyPlayersInCell(player, cells[rowIndex + iz]);
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

    private boolean isCellOfInterest(int cell, int centerCell) {
        return Math.abs(cell - centerCell) <= cellRangeOfInterest;
    }

    private void addNearbyPlayersInCell(
        MMOPlayer currentPlayer,
        Cell cell
    ) {
        for (MMOPlayer nearByPlayer : cell.players) {
            MMOPlayer.link(currentPlayer, nearByPlayer);
        }
    }

    private void removeNearbyPlayersInCell(
        MMOPlayer currentPlayer,
        Cell cell
    ) {
        for (MMOPlayer nearByPlayer : cell.players) {
            MMOPlayer.unlink(currentPlayer, nearByPlayer);
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
                Cell newCell = cells[toCellRowIndex(cellX, cellY) + cellZ];
                newCell.addPlayer(player);
                cellByPlayer.put(player, newCell);
            }
        }
    }

    public List<MMOPlayer> searchPlayers(Vec3 center, float range) {
        List<MMOPlayer> buffer = new ArrayList<>();
        searchPlayers(center, range, buffer);
        return buffer;
    }

    public void searchPlayers(Vec3 center, float range, List<MMOPlayer> buffer) {
        if (range < 0) {
            return;
        }
        final int startX = toSearchCellStart(center.x - range);
        final int startY = toSearchCellStart(center.y - range);
        final int startZ = toSearchCellStart(center.z - range);
        final int endX = toSearchCellEnd(center.x + range, maxCellX);
        final int endY = toSearchCellEnd(center.y + range, maxCellY);
        final int endZ = toSearchCellEnd(center.z + range, maxCellZ);
        final float rangeSquare = range * range;
        synchronized (this) {
            for (int ix = startX; ix <= endX; ++ix) {
                for (int iy = startY; iy <= endY; ++iy) {
                    final int rowIndex = toCellRowIndex(ix, iy);
                    for (int iz = startZ; iz <= endZ; ++iz) {
                        for (MMOPlayer player : cells[rowIndex + iz].players) {
                            if (center.distanceSquare(player.getPosition()) <= rangeSquare) {
                                buffer.add(player);
                            }
                        }
                    }
                }
            }
        }
    }

    private int toSearchCellStart(float value) {
        return Math.max(0, (int) Math.floor(value / cellSize));
    }

    private int toSearchCellEnd(float value, int maxCell) {
        return Math.min(maxCell - 1, (int) Math.floor(value / cellSize));
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
        private int cellRangeOfInterest;

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

        public Builder cellRangeOfInterest(int cellRangeOfInterest) {
            this.cellRangeOfInterest = cellRangeOfInterest;
            return this;
        }

        @Override
        public Builder distanceOfInterest(double distance) {
            this.distanceOfInterest = distance;
            return this;
        }

        @Override
        public Builder maxPlayer(int maxPlayer) {
            this.maxPlayer = maxPlayer;
            return this;
        }

        @Override
        protected void preBuild() {
            if (cellSize <= 0) {
                throw new IllegalArgumentException("cellSize must be greater than 0");
            }
            if (cellRangeOfInterest > 0 && distanceOfInterest <= 0) {
                distanceOfInterest = cellRangeOfInterest * cellSize;
            }
            super.preBuild();
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

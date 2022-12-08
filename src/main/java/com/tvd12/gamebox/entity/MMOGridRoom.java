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

    private final Map<Player, Cell> cellByPlayer;
    private final int cellRangeOfInterest;
    private final Cell[][][] cells;
    private final List<MMOPlayer> cellPlayerBuffer;
    private final Set<Cell> visitedCells;

    public MMOGridRoom(Builder builder) {
        super(builder);
        this.maxX = builder.maxX;
        this.maxY = builder.maxY;
        this.maxZ = builder.maxZ;
        this.cellSize = builder.cellSize;
        this.cellByPlayer = new ConcurrentHashMap<>();
        this.cellRangeOfInterest = (int) (builder.distanceOfInterest);
        final int maxCellX = Math.max(1, (int) (maxX / cellSize));
        final int maxCellY = Math.max(1, (int) (maxY / cellSize));
        final int maxCellZ = Math.max(1, (int) (maxZ / cellSize));
        this.cells = new Cell[maxCellX][maxCellY][maxCellZ];
        this.cellPlayerBuffer = new ArrayList<>();
        this.visitedCells = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void addPlayer(Player player) {
        super.addPlayer(player);
        addPlayerToCell((MMOPlayer) player);
    }

    private void addPlayerToCell(MMOPlayer player) {
        int cellX = (int) (player.getPosition().x / cellSize);
        int cellY = (int) (player.getPosition().y / cellSize);
        int cellZ = (int) (player.getPosition().z / cellSize);
        addPlayerToCell(player, cellX, cellY, cellZ);
    }
    
    private void addPlayerToCell(MMOPlayer player, int cellX, int cellY, int cellZ) {
        Cell cell = createCellIfAbsent(cellX, cellY, cellZ);
        cell.addPlayer(player);
        cellByPlayer.put(player, cell);
    }

    private Cell createCellIfAbsent(int cellX, int cellY, int cellZ) {
        if (this.cells[cellX][cellY][cellZ] == null) {
            Cell cell = new Cell();
            cell.setCellX(cellX);
            cell.setCellY(cellY);
            cell.setCellZ(cellZ);
            this.cells[cellX][cellY][cellZ] = cell;
        }
        return this.cells[cellX][cellY][cellZ];
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
    public void updatePlayers() {
        for (MMOPlayer player : playerBuffer) {
            player.clearNearByPlayers();
        }
        visitedCells.clear();
        for (MMOPlayer player : playerBuffer) {
            Cell cell = cellByPlayer.get(player);
            if (visitedCells.contains(cell)) {
                continue;
            }
            handleCell(cell);
            visitedCells.add(cell);
        }
    }
    
    private void handleCell(Cell cell) {
        final int cellOfInterestEndX = Math.min(cells.length - 1, cell.cellX + cellRangeOfInterest);
        final int cellOfInterestEndY = Math.min(cells.length - 1, cell.cellY + cellRangeOfInterest);
        final int cellOfInterestEndZ = Math.min(cells.length - 1, cell.cellZ + cellRangeOfInterest);
        final int cellOfInterestStartY = Math.max(0, cell.cellY - cellRangeOfInterest);
        final int cellOfInterestStartZ = Math.max(0, cell.cellZ - cellRangeOfInterest);
        
        cellPlayerBuffer.clear();
        cell.getPlayerList(cellPlayerBuffer);
        
        for (int i = 0; i < cellPlayerBuffer.size(); ++i) {
            MMOPlayer currentPlayer = cellPlayerBuffer.get(i);
            
            // Handle players in the current cell
            for (int j = i; j < cellPlayerBuffer.size(); ++j) {
                MMOPlayer nearByPlayer = cellPlayerBuffer.get(j);
                currentPlayer.addNearbyPlayer(nearByPlayer);
                nearByPlayer.addNearbyPlayer(currentPlayer);
            }
            
            // Handle players in neighboring cells
            for (int ix = cell.cellX + 1; ix <= cellOfInterestEndX; ++ix) {
                for (int iy = cellOfInterestStartY; iy <= cellOfInterestEndY; ++iy) {
                    for (int iz = cellOfInterestStartZ; iz <= cellOfInterestEndZ; ++iz) {
                        addNearbyPlayersInCell(currentPlayer, cells[ix][iy][iz]);
                    }
                }
            }
            for (int iy = cell.cellY + 1; iy <= cellOfInterestEndY; ++iy) {
                for (int iz = cellOfInterestStartZ; iz <= cellOfInterestEndZ; ++iz) {
                    addNearbyPlayersInCell(currentPlayer, cells[cell.cellX][iy][iz]);
                }
            }
            for (int iz = cellOfInterestStartZ; iz <= cellOfInterestEndZ; ++iz) {
                if (iz != cell.cellZ) {
                    addNearbyPlayersInCell(currentPlayer, cells[cell.cellX][cell.cellY][iz]);
                }
            }
        }
    }
    
    public void addNearbyPlayersInCell(MMOPlayer currentPlayer, Cell cell) {
        if (cell == null || cell.getNumberOfPlayers() == 0) {
            return;
        }
        for (MMOPlayer nearByPlayer : cell.players) {
            currentPlayer.addNearbyPlayer(nearByPlayer);
            nearByPlayer.addNearbyPlayer(currentPlayer);
        } 
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
        
        public void getPlayerList(List<MMOPlayer> playerList) {
            playerList.addAll(players);
        }
        
        public int getNumberOfPlayers() {
            return players.size();
        }
    }
}

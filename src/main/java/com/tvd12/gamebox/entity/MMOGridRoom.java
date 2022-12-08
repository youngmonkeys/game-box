package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Vec3;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MMOGridRoom extends MMORoom {

    @Getter
    private final int maxX;
    
    @Getter
    private final int maxY;
    
    @Getter
    private final int maxZ;
    
    @Getter
    private final int cellSize;

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
        final int maxCellX = Math.max(1, maxX / cellSize);
        final int maxCellY = Math.max(1, maxY / cellSize);
        final int maxCellZ = Math.max(1, maxZ / cellSize);
        this.cells = new Cell[maxCellX][maxCellY][maxCellZ];
        this.cellPlayerBuffer = new ArrayList<>();
        this.visitedCells = ConcurrentHashMap.newKeySet();;
    }

    @Override
    public void addPlayer(Player player) {
        super.addPlayer(player);
        addPlayerToCell((MMOPlayer) player);
    }

    private void addPlayerToCell(MMOPlayer player) {
        int cellX = (int) player.getPosition().x / cellSize;
        int cellY = (int) player.getPosition().y / cellSize;
        int cellZ = (int) player.getPosition().z / cellSize;
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
        if (position.x >= 0 && position.x <= maxX
            && position.y >= 0 && position.y <= maxY
            && position.z >= 0 && position.z <= maxZ
        ) {
            final Cell oldCell = cellByPlayer.get(player);
            
            if (oldCell == null) {
                player.setPosition(position);
                addPlayerToCell(player);
                return;
            }

            int cellX = (int) position.x / cellSize;
            int cellY = (int) position.y / cellSize;
            int cellZ = (int) position.z / cellSize;
            
            player.setPosition(position);

            if (oldCell.cellX != cellX || oldCell.cellY != cellY || oldCell.cellZ != cellZ) {
                oldCell.removePlayer(player.getName());
                addPlayerToCell(player, cellX, cellY, cellZ);
            }
        }
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
        Collection<MMOPlayer> nearByPlayers = cell.getPlayerCollection();
        for (MMOPlayer nearByPlayer : nearByPlayers) {
            currentPlayer.addNearbyPlayer(nearByPlayer);
            nearByPlayer.addNearbyPlayer(currentPlayer);
        } 
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends MMORoom.Builder {

        private int maxX;
        private int maxY;
        private int maxZ;
        private int cellSize;

        public Builder maxX(int maxX) {
            this.maxX = maxX;
            return this;
        }

        public Builder maxY(int maxY) {
            this.maxY = maxY;
            return this;
        }

        public Builder maxZ(int maxZ) {
            this.maxZ = maxZ;
            return this;
        }

        public Builder cellSize(int cellSize) {
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
        
        private final Map<String, MMOPlayer> playerByName = new ConcurrentHashMap<>();
        
        public void addPlayer(MMOPlayer player) {
            playerByName.put(player.getName(), player);
        }
        
        public void removePlayer(String playerName) {
            playerByName.remove(playerName);
        }
        
        public void getPlayerList(List<MMOPlayer> players) {
            players.addAll(playerByName.values());
        }
        
        public Collection<MMOPlayer> getPlayerCollection() {
            return playerByName.values();
        }
        
        public int getNumberOfPlayers() {
            return playerByName.size();
        }
    }
}

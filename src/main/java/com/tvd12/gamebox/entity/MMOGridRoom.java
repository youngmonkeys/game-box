package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.math.Vec3;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
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
    
    private final Unit[][][] cells;
    private final int cellRangeOfInterest;
    private final Map<Player, Unit> unitByPlayer;

    public MMOGridRoom(Builder builder) {
        super(builder);
        this.maxX = builder.maxX;
        this.maxY = builder.maxY;
        this.maxZ = builder.maxZ;
        this.cellSize = builder.cellSize;
        this.unitByPlayer = new ConcurrentHashMap<>();
        this.cellRangeOfInterest = 1 + (int) (builder.distanceOfInterest / this.cellSize);
        final int maxCellX = Math.max(1, maxX / cellSize);
        final int maxCellY = Math.max(1, maxY / cellSize);
        final int maxCellZ = Math.max(1, maxZ / cellSize);
        this.cells = new Unit[maxCellX][maxCellY][maxCellZ];
    }

    @Override
    public void addPlayer(Player player) {
        super.addPlayer(player);
        addUnit((MMOPlayer) player);
    }

    private void addUnit(MMOPlayer player) {
        int cellX = (int) player.getPosition().x / cellSize;
        int cellY = (int) player.getPosition().y / cellSize;
        int cellZ = (int) player.getPosition().z / cellSize;
        Unit unit = new Unit();
        unit.setPlayer(player);
        unit.prev = null;
        unit.next = cells[cellX][cellY][cellZ];
        cells[cellX][cellY][cellZ] = unit;
        if (unit.next != null) {
            unit.next.prev = unit;
        }
        unitByPlayer.put(player, unit);
    }

    public void setPlayerPosition(MMOPlayer player, Vec3 position) {
        if (position.x >= 0 && position.x <= maxX
            && position.y >= 0 && position.y <= maxY
            && position.z >= 0 && position.z <= maxZ
        ) {
            final Unit oldUnit = unitByPlayer.get(player);
            
            if (oldUnit == null) {
                addUnit(player);
                player.setPosition(position);
                return;
            }
            
            int oldCellX = (int) oldUnit.getPlayer().getPosition().x / cellSize;
            int oldCellY = (int) oldUnit.getPlayer().getPosition().y / cellSize;
            int oldCellZ = (int) oldUnit.getPlayer().getPosition().z / cellSize;

            int cellX = (int) position.x / cellSize;
            int cellY = (int) position.y / cellSize;
            int cellZ = (int) position.z / cellSize;

            if (oldCellX != cellX || oldCellY != cellY || oldCellZ != cellZ) {
                if (oldUnit.prev != null) {
                    oldUnit.prev.next = oldUnit.next;
                }
                if (oldUnit.next != null) {
                    oldUnit.next.prev = oldUnit.prev;
                }
                if (cells[oldCellX][oldCellY][oldCellZ] == oldUnit) {
                    cells[oldCellX][oldCellY][oldCellZ] = oldUnit.next;
                }
                addUnit(player);
            }
            player.setPosition(position);
        }
    }

    @Override
    public void updatePlayers() {
        for (int ix = 0; ix < cells.length; ++ix) {
            for (int iy = 0; iy < cells[ix].length; ++iy) {
                for (int iz = 0; iz < cells[ix][iy].length; ++iz) {
                    handleCell(ix, iy, iz);
                }
            }
        }
    }

    private void handleCell(int posX, int posY, int posZ) {
        final int cellOfInterestEndX = Math.min(cells.length - 1, posX + cellRangeOfInterest);
        final int cellOfInterestEndY = Math.min(cells.length - 1, posY + cellRangeOfInterest);
        final int cellOfInterestEndZ = Math.min(cells.length - 1, posZ + cellRangeOfInterest);
        final int cellOfInterestStartY = Math.max(0, posY - cellRangeOfInterest);
        final int cellOfInterestStartZ = Math.max(0, posZ - cellRangeOfInterest);

        Unit unit = cells[posX][posY][posZ];
        while (unit != null) {
            // Handle other units in this cell
            handleUnit(unit, unit.next);

            // Also try the neighboring cells
            for (int ix = posX + 1; ix <= cellOfInterestEndX; ++ix) {
                for (int iy = cellOfInterestStartY; iy <= cellOfInterestEndY; ++iy) {
                    for (int iz = cellOfInterestStartZ; iz <= cellOfInterestEndZ; ++iz) {
                        handleUnit(unit, cells[ix][iy][iz]);
                    }
                }
            }

            for (int iy = posY + 1; iy <= cellOfInterestEndY; ++iy) {
                for (int iz = cellOfInterestStartZ; iz <= cellOfInterestEndZ; ++iz) {
                    handleUnit(unit, cells[posX][iy][iz]);
                }
            }

            for (int iz = cellOfInterestStartZ; iz <= cellOfInterestEndZ; ++iz) {
                if (iz != posZ) {
                    handleUnit(unit, cells[posX][posY][iz]);
                }
            }
            unit = unit.next;
        }
    }

    private void handleUnit(Unit unit, Unit other) {
        MMOPlayer player = unit.getPlayer();
        while (other != null) {
            double distance = player.getPosition().distance(
                other.getPlayer().getPosition()
            );
            if (distance <= this.distanceOfInterest) {
                player.addNearbyPlayer(other.getPlayer());
                other.getPlayer().addNearbyPlayer(player);
            }
            other = other.next;
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

        public Builder maxZ(int height) {
            this.maxZ = height;
            return this;
        }

        public Builder cellSize(int cellSize) {
            this.cellSize = cellSize;
            return this;
        }

        @Override
        public MMORoom.Builder distanceOfInterest(double distance) {
            this.distanceOfInterest = distance;
            return this;
        }

        @Override
        protected MMORoom newProduct() {
            return new MMOGridRoom(this);
        }
    }

    private static class Unit {
        @Getter
        @Setter
        private MMOPlayer player;
        public Unit next;
        public Unit prev;
    }
}

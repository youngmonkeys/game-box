package com.tvd12.gamebox.grid;

import com.tvd12.gamebox.entity.PositionAware;
import com.tvd12.gamebox.math.Vec3;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UniformGrid<T extends PositionAware> {

    @Getter
    private final float cellSize;
    private final Map<CellKey, Set<T>> itemsByCell;
    private final Map<T, CellKey> cellByItem;

    public UniformGrid(float cellSize) {
        if (cellSize <= 0) {
            throw new IllegalArgumentException("cellSize must > 0");
        }
        this.cellSize = cellSize;
        this.itemsByCell = new HashMap<>();
        this.cellByItem = new HashMap<>();
    }

    public boolean insert(T item) {
        if (cellByItem.containsKey(item)) {
            return false;
        }
        addToCell(item, cellOf(item.getPosition()));
        return true;
    }

    public boolean remove(T item) {
        CellKey cell = cellByItem.remove(item);
        if (cell == null) {
            return false;
        }
        removeFromCell(item, cell);
        return true;
    }

    public boolean update(T item) {
        CellKey oldCell = cellByItem.get(item);
        if (oldCell == null) {
            return false;
        }
        CellKey newCell = cellOf(item.getPosition());
        if (!newCell.equals(oldCell)) {
            removeFromCell(item, oldCell);
            addToCell(item, newCell);
        }
        return true;
    }

    public List<T> search(T item, float range) {
        return search(item.getPosition(), range);
    }

    public List<T> search(Vec3 center, float range) {
        List<T> matches = new ArrayList<>();
        search(center, range, matches);
        return matches;
    }

    public void search(Vec3 center, float range, List<T> matches) {
        if (range < 0) {
            return;
        }
        int minX = toCellIndex(center.x - range);
        int maxX = toCellIndex(center.x + range);
        int minY = toCellIndex(center.y - range);
        int maxY = toCellIndex(center.y + range);
        int minZ = toCellIndex(center.z - range);
        int maxZ = toCellIndex(center.z + range);
        float rangeSquare = range * range;
        long cellsInRange = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        if (cellsInRange > itemsByCell.size()) {
            for (Map.Entry<CellKey, Set<T>> entry : itemsByCell.entrySet()) {
                CellKey cell = entry.getKey();
                if (cell.x >= minX && cell.x <= maxX
                    && cell.y >= minY && cell.y <= maxY
                    && cell.z >= minZ && cell.z <= maxZ
                ) {
                    addMatches(entry.getValue(), center, rangeSquare, matches);
                }
            }
            return;
        }
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Set<T> items = itemsByCell.get(new CellKey(x, y, z));
                    if (items != null) {
                        addMatches(items, center, rangeSquare, matches);
                    }
                }
            }
        }
    }

    public boolean contains(T item) {
        return cellByItem.containsKey(item);
    }

    public int size() {
        return cellByItem.size();
    }

    public int getCellCount() {
        return itemsByCell.size();
    }

    public boolean isItemRemainingAtSameCell(T item, Vec3 newPosition) {
        CellKey cell = cellByItem.get(item);
        return cell != null && cell.equals(cellOf(newPosition));
    }

    private void addMatches(Set<T> items, Vec3 center, float rangeSquare, List<T> matches) {
        for (T item : items) {
            if (center.distanceSquare(item.getPosition()) <= rangeSquare) {
                matches.add(item);
            }
        }
    }

    private void addToCell(T item, CellKey cell) {
        cellByItem.put(item, cell);
        itemsByCell.computeIfAbsent(cell, k -> new HashSet<>()).add(item);
    }

    private void removeFromCell(T item, CellKey cell) {
        Set<T> items = itemsByCell.get(cell);
        if (items == null) {
            return;
        }
        items.remove(item);
        if (items.isEmpty()) {
            itemsByCell.remove(cell);
        }
    }

    private CellKey cellOf(Vec3 position) {
        return new CellKey(
            toCellIndex(position.x),
            toCellIndex(position.y),
            toCellIndex(position.z)
        );
    }

    private int toCellIndex(float value) {
        return (int) Math.floor(value / cellSize);
    }

    @Override
    public String toString() {
        return '{' +
            "cellSize=" + cellSize +
            ", cellCount=" + itemsByCell.size() +
            ", itemCount=" + cellByItem.size() +
            '}';
    }

    private static final class CellKey {

        private final int x;
        private final int y;
        private final int z;

        private CellKey(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CellKey)) {
                return false;
            }
            CellKey other = (CellKey) obj;
            return x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            int hashCode = 31 + x;
            hashCode = 31 * hashCode + y;
            return 31 * hashCode + z;
        }
    }
}

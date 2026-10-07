package com.tvd12.gamebox.grid;

import com.tvd12.gamebox.entity.PositionAware;
import com.tvd12.gamebox.math.Vec3;

import java.util.List;

public class SynchronizedUniformGrid<T extends PositionAware> extends UniformGrid<T> {

    public SynchronizedUniformGrid(float cellSize) {
        super(cellSize);
    }

    @Override
    public boolean insert(T item) {
        synchronized (this) {
            return super.insert(item);
        }
    }

    @Override
    public boolean remove(T item) {
        synchronized (this) {
            return super.remove(item);
        }
    }

    @Override
    public boolean update(T item) {
        synchronized (this) {
            return super.update(item);
        }
    }

    @Override
    public List<T> search(T item, float range) {
        synchronized (this) {
            return super.search(item, range);
        }
    }

    @Override
    public List<T> search(Vec3 center, float range) {
        synchronized (this) {
            return super.search(center, range);
        }
    }

    @Override
    public boolean contains(T item) {
        synchronized (this) {
            return super.contains(item);
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            return super.size();
        }
    }

    @Override
    public int getCellCount() {
        synchronized (this) {
            return super.getCellCount();
        }
    }

    @Override
    public boolean isItemRemainingAtSameCell(T item, Vec3 newPosition) {
        synchronized (this) {
            return super.isItemRemainingAtSameCell(item, newPosition);
        }
    }

    @Override
    public String toString() {
        synchronized (this) {
            return super.toString();
        }
    }
}

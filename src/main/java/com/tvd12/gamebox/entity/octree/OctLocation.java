package com.tvd12.gamebox.entity.octree;

public enum OctLocation {
    TOP_LEFT_FRONT(0),
    TOP_RIGHT_FRONT(1),
    BOTTOM_RIGHT_FRONT(2),
    BOTTOM_LEFT_FRONT(3),
    TOP_LEFT_BACK(4),
    TOP_RIGHT_BACK(5),
    BOTTOM_RIGHT_BACK(6),
    BOTTOM_LEFT_BACK(7);
    
    int location;

    OctLocation(int location) {
        this.location = location;
    }

    public int getLocation() {
        return location;
    }
}

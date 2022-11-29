package com.tvd12.gamebox.entity.octree;

import com.tvd12.ezyfox.util.EzyEnums;

import java.util.Map;

public enum OcLocation {
    TOP_LEFT_FRONT(0),
    TOP_RIGHT_FRONT(1),
    BOTTOM_RIGHT_FRONT(2),
    BOTTOM_LEFT_FRONT(3),
    TOP_LEFT_BACK(4),
    TOP_RIGHT_BACK(5),
    BOTTOM_RIGHT_BACK(6),
    BOTTOM_LEFT_BACK(7);

    private final int location;

    private static final Map<Integer, OcLocation> OC_LOCATION_BY_INDEX =
        EzyEnums.enumMap(OcLocation.class, it -> it.location);

    OcLocation(int location) {
        this.location = location;
    }

    public static OcLocation of(int index) {
        if (index < 0 || index > 7) {
            return null;
        }
        return OC_LOCATION_BY_INDEX.get(index);
    }
}

package com.tvd12.gamebox.math;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.factory.EzyEntityFactory;

public final class Vec3s {
	
	private Vec3s() {
	}
	
	public static EzyArray toArray(Vec3 value) {
		EzyArray array = EzyEntityFactory.newArray();
		array.add(value.getX());
		array.add(value.getY());
		array.add(value.getZ());
		return array;
	}
	
}

package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.util.RandomUtil;

public class TestHelper {
	
	private TestHelper() {
	}
	
	public static Vec3 randomVec3() {
		return new Vec3(
				RandomUtil.randomFloat(),
				RandomUtil.randomFloat(),
				RandomUtil.randomFloat()
		);
	}
}

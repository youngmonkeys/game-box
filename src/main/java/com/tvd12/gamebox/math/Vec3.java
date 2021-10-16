package com.tvd12.gamebox.math;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.factory.EzyEntityFactory;
import lombok.Getter;

@Getter
public class Vec3 {
	public float x;
	public float y;
	public float z;
	
	public static final Vec3 ZERO = new Vec3();
	public static final Vec3 up = new Vec3(0.0f, 1f, 0.0f);
	public static final Vec3 down = new Vec3(0.0f, -1f, 0.0f);
	public static final Vec3 left = new Vec3(-1f, 0.0f, 0.0f);
	public static final Vec3 right = new Vec3(1f, 0.0f, 0.0f);
	public static final Vec3 forward = new Vec3(0.0f, 0.0f, 1f);
	public static final Vec3 backward = new Vec3(0.0f, 0.0f, -1f);

	public Vec3() {
		this(0.0f, 0.0f, 0.0f);
	}
	
	public Vec3(Vec3 v) {
		this(v.x, v.y, v.z);
	}

	public Vec3(float[] array) {
		this(array[0], array[1], array[2]);
	}

	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void add(Vec3 v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}

	public double distance(Vec3 v) {
		double dx = v.x - x;
		double dy = v.y - y;
		double dz = v.z - z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public void negate() {
		x = -x;
		y = -y;
		z = -z;
	}

	public void set(float xx, float yy, float zz) {
		this.x = xx;
		this.y = yy;
		this.z = zz;
	}
	
	public void set(double xx, double yy, double zz) {
		this.x = (float)xx;
		this.y = (float)yy;
		this.z = (float)zz;
	}

	public void set(double[] array) {
		x = (float)array[0];
		y = (float)array[1];
		z = (float)array[2];
	}

	public void set(Vec3 v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	public void subtract(Vec3 v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
	}
	
	public void multiply(double value) {
		x *= value;
		y *= value;
		z *= value;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append("(")
					.append(x).append(", ")
					.append(y).append(", ")
					.append(z)
				.append(")")
				.toString();
	}
	
	public static EzyArray toArray(Vec3 value) {
		EzyArray array = EzyEntityFactory.newArray();
		array.add(value.getX());
		array.add(value.getY());
		array.add(value.getZ());
		return array;
	}
}
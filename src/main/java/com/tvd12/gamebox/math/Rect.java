package com.tvd12.gamebox.math;

public class Rect {

	public double x;
	public double y;
	public double width;
	public double height;
	
	public Rect() {
	}
	
	public Rect(Rect r) {
		this.x = r.x;
		this.y = r.y;
		this.width = r.width;
		this.height = r.height;
	}
	
	public Rect(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public double getTopY() {
		double value = y + height;
		return value;
	}
	
	public double getRightX() {
		double value = x + width;
		return value;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append("(")
					.append(x).append(", ")
					.append(y).append(" | ")
					.append(width).append(", ")
					.append(height)
				.append(")")
				.toString();
	}
}

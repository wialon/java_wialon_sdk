package com.wialon.render;

public class Layer {
	private String name;
	private boolean enabled=true;
	private double[] bounds;
	/**
	 * Get layer name
	 * @return {String} layer name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Get layer bounds
	 * @return {Array} layer bounds
	 */
	public double[] getBounds() {
		return this.bounds;
	}

	public boolean isEnabled() {
		return enabled;
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}

package com.wialon.extra;

public class UpdateSpec {
	private String type;
	private Object data;
	private long flags;
	private int mode;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public long getFlags() {
		return flags;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public void setFlags(long flags) {
		this.flags = flags;
	}
}

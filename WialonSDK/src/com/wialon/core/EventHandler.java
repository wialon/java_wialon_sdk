package com.wialon.core;

public interface EventHandler {
	public void onEvent(Enum event, Object object, Object oldData, Object newData);
}

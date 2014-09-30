package com.wialon.core;

public interface EventHandler {
	/**
	 * Method which calls when listener receiving event
	 * @param event enum event constant
	 * @param object object which fired event, maybe null
	 * @param oldData old data of event content
	 * @param newData new data of event content
	 */
	public void onEvent(Enum event, Object object, Object oldData, Object newData);
}

package com.wialon.core;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EventProvider {
	private Map<Enum, List<EventHandler>> eventHandlers=new HashMap<Enum, List<EventHandler>>();

	public void addListener(EventHandler eventHandler, Enum event) {
		List<EventHandler> handlers=eventHandlers.get(event);
		if (handlers==null) {
			handlers=new ArrayList<EventHandler>();
			eventHandlers.put(event, handlers);
		}
		handlers.add(eventHandler);
	}

	public void removeListener(EventHandler eventHandler, Enum event){
		List<EventHandler> handlers=eventHandlers.get(event);
		if (handlers!=null)
			handlers.remove(eventHandler);
	}

	public void removeListeners(Enum event) {
		eventHandlers.remove(event);
	}

	public void clearListeners() {
		eventHandlers.clear();
	}

	protected void fireEvent(Enum event, Object object, Object oldData, Object newData) {
		if (oldData!=null && newData!=null)
			if (oldData.equals(newData))
				return;
		if (eventHandlers.get(events.All)!=null)
			for (EventHandler handler : eventHandlers.get(events.All))
				handler.onEvent(event, (object==null ? this : object), oldData, newData);
		if(eventHandlers.get(event)!=null) {
			for (EventHandler handler : eventHandlers.get(event))
				handler.onEvent(event, (object==null ? this : object), oldData, newData);
		}
	}

	protected void fireEvent(Enum event, Object oldData, Object newData) {
		fireEvent(event, null, oldData, newData);
	}

	public static enum events {
		All
	}
}

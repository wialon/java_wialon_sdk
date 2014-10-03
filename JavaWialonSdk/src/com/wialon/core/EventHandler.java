/*
 * Copyright 2014 Gurtam
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 */

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

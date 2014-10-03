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

package com.wialon.messages;

public class UnitCmd extends Message {
	/* command name */
	private String ca;
	/* command type */
	private String cn;
	/* command parameters */
	private String cp;
	/* user ID */
	private String ui;
	/* link name */
	private String ln;
	/* link type */
	private String lt;
	/* execution time */
	private String et;

	public UnitCmd() {
		this.messageType=MessageType.UnitCmd;
	}

	public String getCommandName() {
		return ca;
	}

	public String getCommandType() {
		return cn;
	}

	public String getCommandParameters() {
		return cp;
	}

	public String getUserID() {
		return ui;
	}

	public String getLinkName() {
		return ln;
	}

	public String getLinkType() {
		return lt;
	}

	public String getExecutionTime() {
		return et;
	}
}

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

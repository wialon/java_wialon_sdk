package com.wialon.messages;

public class UnitSMS extends Message {
	private String st;
	private String mp;

	public UnitSMS() {
		this.messageType=MessageType.UnitSMS;
	}
	public String getMessageText() {
		return st;
	}

	public String getModemPhoneNumber() {
		return mp;
	}
}

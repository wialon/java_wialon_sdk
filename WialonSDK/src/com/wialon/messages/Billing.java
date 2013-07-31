package com.wialon.messages;

public class Billing extends Message {
	public Billing(){
		this.messageType=MessageType.Balance;
	}
}

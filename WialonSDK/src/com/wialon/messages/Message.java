package com.wialon.messages;

import java.util.Map;

public class Message {
	protected long t;
	private long f;
	protected MessageType messageType;
	private Map<String, Object> p;

	public long getTime() {
		return t;
	}

	public long getFlags() {
		return f;
	}

	public Map<String, Object> getParameters() {
		return p;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public static enum MessageType {
		/** Unit data message */
		UnitData(UnitData.class, Message.messageFlag.typeUnitData, "ud"),
		/** Unit SMS */
		UnitSMS (UnitSMS.class, Message.messageFlag.typeUnitSMS, "us"),
		/** Unit command */
		UnitCmd(UnitCmd.class, Message.messageFlag.typeUnitCmd, "ucr"),
		/** Unit event */
		UnitEvent (UnitEvent.class, Message.messageFlag.typeUnitEvent, "evt"),
		/** Notification */
		Notification (Notification.class, Message.messageFlag.typeNotification, "xx"),
		/** Billing balance update */
		Balance(Billing.class, Message.messageFlag.typeBalance, "xx"),
		/** SMS from driver */
		DriverSMS (DriverSMS.class, Message.messageFlag.typeDriverSMS, "xx");

		private Class messageClass;
		private messageFlag messageFlag;
		private String value;

		private MessageType(Class messageClass, messageFlag messageFlag, String value) {
			this.messageClass=messageClass;
			this.messageFlag=messageFlag;
			this.value=value;
		}

		public static Class getMessageClass(messageFlag messageFlag, String tp) {
			for (MessageType type :values())
				if (tp.equals(type.getValue()) && (type.getMessageFlag().getValue()==messageFlag.getValue()))
					return type.messageClass;
			return null;
		}

		public Message.messageFlag getMessageFlag() {
			return messageFlag;
		}

		public String getValue() {
			return value;
		}
	}

	/** Message flags constants, generic */
	public static enum messageFlag {
		/** AVL message flags mask determining message type */
		typeMask(0xFF00),
		/** AVL message flag, set when message represents data sent from unit */
		typeUnitData(0x0000),
		/** AVL message flag, set when message represents incoming SMS */
		typeUnitSMS(0x0100),
		/** AVL message flag, set when message represents command over unit */
		typeUnitCmd(0x0200),
		/** AVL message flag, set when message represents event */
		typeUnitEvent(0x0600),
		/** Storage message flag, set when message represents log of user actions */
		typeUserLog(0x0400),
		/** AVL message flag, set when message type is user notification */
		typeNotification(0x0300),
		/** Billing message flag, set when message represents billing balance update */
		typeBalance(0x0500),
		/** Storage message flag, set when message represents plot cultivation */
		typeAgroCultivation(0x0700),
		/** AVL message type, set when message type is SMS from driver */
		typeDriverSMS(0x0900),
		/** AVL message flag, set when represents unit log messages */
		typeLogRecord(0x1000),
		/** AVL message flag, set when message type is something different */
		typeOther(0xFF00);
		/** Flag value */
		private long value;

		private messageFlag (long value) {
			this.value=value;
		}

		public static messageFlag getMessageFlag(long flagValue){
			for (messageFlag flag : values()) {
				if ((flag.getValue()&typeMask.getValue())==(flagValue&typeMask.getValue()))
					return flag;
			}
			return null;
		}

		public long getValue() {
			return value;
		}
	}
}

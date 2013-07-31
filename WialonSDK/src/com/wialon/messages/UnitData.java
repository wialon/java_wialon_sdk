package com.wialon.messages;

public class UnitData extends Message {
	private Position pos;
	private long i;
	private long o;

	public UnitData(){
		this.messageType=MessageType.UnitData;
	}

	public Position getPosition() {
		return pos;
	}

	public long getInputData() {
		return i;
	}

	public long getOutputData() {
		return o;
	}

	public class Position {
		private long t;
		private double y;
		private double x;
		private int z;
		private int s;
		private int c;
		private int sc;

		public void setTime(long time) {
			this.t=time;
		}

		public double getLatitude () {
			return y;
		}

		public double getLongitude () {
			return x;
		}

		public int getAltitude () {
			return z;
		}

		public int getSpeed() {
			return s;
		}

		public int getCourse() {
			return c;
		}

		public int getSatellitesCount() {
			return sc;
		}

		public long getTime() {
			return t;
		}
	}
	/** Message flags constants, unit data messages only */
	public static enum dataMessageFlag{
		/** AVL data message flag, set when position info is available */
		position(0x1),
		/** AVL data message flag, set when digital inputs data available */
		inputs(0x2),
		/** AVL data message flag, set when digital outputs data available */
		outputs(0x4),
		/** AVL data message flag, set when message has alarm bit */
		alarm(0x10),
		/** AVL data message flag, set when message has driver code information */
		driverCode(0x20);
		/** Flag value */
		private long value;

		private dataMessageFlag (long value) {
			this.value=value;
		}

		public long getValue() {
			return value;
		}
	}
}

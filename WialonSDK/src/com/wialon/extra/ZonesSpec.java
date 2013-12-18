package com.wialon.extra;

public class ZonesSpec {
	private final long resourceId;
	private final long[] zoneId;

	private ZonesSpec(long resourceId, long[] zoneId){
		this.resourceId=resourceId;
		this.zoneId=zoneId;
	}

	public static class Builder{
		private long resourceId;
		private long[] zoneId;

		public Builder resourceId(long resourceId) {
			this.resourceId = resourceId;
			return this;
		}

		public Builder zoneId(long[] zoneId) {
			this.zoneId = zoneId;
			return this;
		}

		public ZonesSpec build(){
			return new ZonesSpec(resourceId, zoneId);
		}
	}
}

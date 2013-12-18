package com.wialon.extra;

public class POISpec {
	private final long resourceId;
	private final long[] poiId;

	private POISpec(long resourceId, long[] poiId){
		this.resourceId=resourceId;
		this.poiId=poiId;
	}

	public static class Builder{
		private long resourceId;
		private long[] poiId;

		public Builder resourceId(long resourceId) {
			this.resourceId = resourceId;
			return this;
		}

		public Builder poiId(long[] poiId) {
			this.poiId = poiId;
			return this;
		}

		public POISpec build(){
			return new POISpec(resourceId, poiId);
		}
	}
}

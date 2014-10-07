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

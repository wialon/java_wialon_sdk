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

package com.wialon.render;

import java.util.List;

public class MessagesLayer extends Layer {
	private List<TrackUnit> units;

	/**
	 * Get unit Id for which this layer has been created
	 * @return {Integer} unit Id
	 */
	public Long getUnitId() {
		if (units != null && units.size() > 0) {
			return this.units.get(0).getId();
		}
		return null;
	}

	/**
	 * Get maximum speed reached in layer's messages
	 * @return {Integer} maximum speed, km/h
	 */
	public Double getMaxSpeed() {
		if (units != null && units.size() > 0) {
			return this.units.get(0).getMaxSpeed();
		}
		return null;
	}

	/**
	 * Get mileage reached in layer's messages
	 * @return {Double} mileage, km
	 */
	public Double getMileage() {
		if (units != null && units.size() > 0) {
			return this.units.get(0).getMileage();
		}
		return null;
	}

	/**
	 * Get total messages count layer's messages
	 * @return {Integer} count
	 */
	public Long getMessagesCount() {
		if (units != null && units.size() > 0) {
			return this.units.get(0).getMsgs().getCount();
		}
		return null;
	}

	/**
	 * Get first point information
	 * @return {Object} point information in form {time: X, lat: X, lon: X}
	 */
	public TrackPoint getFirstPoint() {
		if (units != null && units.size() > 0) {
			return this.units.get(0).getMsgs().getFirst();
		}
		return null;
	}

	/**
	 * Get last point information
	 * @return {Object} point information in form {time: X, lat: X, lon: X}
	 */
	public TrackPoint getLastPoint() {
		if (units != null && units.size() > 0) {
			return this.units.get(0).getMsgs().getLast();
		}
		return null;
	}

	private class TrackUnit{
		private Long id;
		private Double max_speed;
		private Double mileage;
		private Msgs msgs;

		public Long getId(){
			return this.id;
		}
		public void setId(Long id){
			this.id = id;
		}
		public Double getMaxSpeed(){
			return this.max_speed;
		}
		public void setMaxSpeed(Double max_speed){
			this.max_speed = max_speed;
		}
		public Double getMileage(){
			return this.mileage;
		}
		public void setMileage(Double mileage){
			this.mileage = mileage;
		}
		public Msgs getMsgs(){
			return this.msgs;
		}
		public void setMsgs(Msgs msgs){
			this.msgs = msgs;
		}
	}

	private class Msgs{
		private Long count;
		private TrackPoint first;
		private TrackPoint last;

		public Long getCount(){
			return this.count;
		}
		public void setCount(Long count){
			this.count = count;
		}
		public TrackPoint getFirst(){
			return this.first;
		}
		public void setFirst(TrackPoint first){
			this.first = first;
		}
		public TrackPoint getLast(){
			return this.last;
		}
		public void setLast(TrackPoint last){
			this.last = last;
		}
	}

	public class TrackPoint {
		private Double lat;
		private Double lon;
		private Long time;

		public Double getLat(){
			return this.lat;
		}
		public void setLat(Double lat){
			this.lat = lat;
		}
		public Double getLon(){
			return this.lon;
		}
		public void setLon(Double lon){
			this.lon = lon;
		}
		public Long getTime(){
			return this.time;
		}
		public void setTime(Long time){
			this.time = time;
		}
	}
}

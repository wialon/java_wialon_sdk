package com.wialon.extra;

public class LayerSpec {
	private final String layerName;
	private final long itemId;
	private final long timeFrom;
	private final long timeTo;
	private final boolean tripDetector;
	private final String trackColor;
	private final int trackWidth;
	private final boolean arrows;
	private final String style;
	private final boolean annotations;
	private final boolean points;
	private final String pointColor;

	private LayerSpec(String layerName, long itemId, long timeFrom, long timeTo, boolean tripDetector, String trackColor, int trackWidth, boolean arrows, String style, boolean annotations, boolean points, String pointColor){
		this.layerName=layerName;
		this.itemId=itemId;
		this.timeFrom=timeFrom;
		this.timeTo=timeTo;
		this.tripDetector=tripDetector;
		this.trackColor=trackColor;
		this.trackWidth=trackWidth;
		this.arrows=arrows;
		this.style=style;
		this.annotations=annotations;
		this.points=points;
		this.pointColor=pointColor;
	}

	public static class Builder{
		private String layerName;
		private long itemId;
		private long timeFrom;
		private long timeTo;
		private boolean tripDetector;
		private String trackColor;
		private int trackWidth;
		private boolean arrows;
		private String style;
		private boolean annotations;
		private boolean points;
		private String pointColor;

		public Builder layerName(String layerName) {
			this.layerName = layerName;
			return this;
		}

		public Builder itemId(long itemId) {
			this.itemId = itemId;
			return this;
		}

		public Builder timeFrom(long timeFrom) {
			this.timeFrom = timeFrom;
			return this;
		}

		public Builder timeTo(long timeTo) {
			this.timeTo = timeTo;
			return this;
		}

		public Builder tripDetector(boolean tripDetector) {
			this.tripDetector = tripDetector;
			return this;
		}

		public Builder trackColor(String trackColor) {
			this.trackColor = trackColor;
			return this;
		}

		public Builder trackWidth(int trackWidth) {
			this.trackWidth = trackWidth;
			return this;
		}

		public Builder arrows(boolean arrows) {
			this.arrows = arrows;
			return this;
		}

		public Builder style(String style) {
			this.style = style;
			return this;
		}

		public Builder annotations(boolean annotations) {
			this.annotations = annotations;
			return this;
		}

		public Builder points(boolean points) {
			this.points = points;
			return this;
		}

		public Builder pointColor(String pointColor) {
			this.pointColor = pointColor;
			return this;
		}

		public LayerSpec build(){
			return new LayerSpec(layerName, itemId, timeFrom, timeTo, tripDetector, trackColor, trackWidth, arrows, style, annotations, points, pointColor);
		}
	}
}

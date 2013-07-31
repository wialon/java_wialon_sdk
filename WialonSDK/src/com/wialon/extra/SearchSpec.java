package com.wialon.extra;

import com.wialon.item.Item;

public class SearchSpec {
	private Item.ItemType itemsType;
	private String propName;
	private String propValueMask;
	private String sortType;
	private String propType;

	public Item.ItemType getItemType() {
		return itemsType;
	}

	public void setItemsType(Item.ItemType itemsType) {
		this.itemsType = itemsType;
	}

	public String getPropName() {
		return propName;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public String getPropValueMask() {
		return propValueMask;
	}

	public void setPropValueMask(String propValueMask) {
		this.propValueMask = propValueMask;
	}

	public String getSortType() {
		return sortType;
	}

	public void setSortType(String sortType) {
		this.sortType = sortType;
	}

	public String getPropType() {
		return propType;
	}

	public void setPropType(String propType) {
		this.propType = propType;
	}
}

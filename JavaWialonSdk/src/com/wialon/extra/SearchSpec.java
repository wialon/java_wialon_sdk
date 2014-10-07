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

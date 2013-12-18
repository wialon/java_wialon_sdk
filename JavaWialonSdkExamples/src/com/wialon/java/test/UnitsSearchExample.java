package com.wialon.java.test;


import com.wialon.core.Errors;
import com.wialon.core.Session;
import com.wialon.extra.SearchSpec;
import com.wialon.item.Item;
import com.wialon.remote.handlers.ResponseHandler;
import com.wialon.remote.handlers.SearchResponseHandler;

public class UnitsSearchExample implements Runnable {
	private Session session;

	// Login to server
	private void login(){
		// initialize Wialon session
		session.initSession("http://hst-api.wialon.com");
		// trying login
		session.login("wialon_test", "test", new ResponseHandler() {
			@Override
			public void onSuccess(String response) {
				super.onSuccess(response);
				// login succeed
				System.out.println(String.format("Logged successfully. User name is %s", session.getCurrUser().getName()));
				//call search units
				searchUnits();
			}

			@Override
			public void onFailure(int errorCode, Throwable throwableError) {
				super.onFailure(errorCode, throwableError);
				// login failed, print error
				System.out.println(Errors.getErrorText(errorCode));
			}
		});
	}

	private void searchUnits(){
		//Create new search specification
		SearchSpec searchSpec=new SearchSpec();
		//Set items type to search avl_units
		searchSpec.setItemsType(Item.ItemType.avl_unit);
		//Set property name to search
		searchSpec.setPropName("sys_name");
		//Set property value mask to search all units
		searchSpec.setPropValueMask("*");
		//Set sort type by units name
		searchSpec.setSortType("sys_name");
		//Send search by created search specification with items base data flag and from 0 to maximum number
		session.searchItems(searchSpec, 1, Item.dataFlag.base.getValue(), 0, Integer.MAX_VALUE, new SearchResponseHandler() {
			@Override
			public void onSuccessSearch(Item... items) {
				super.onSuccessSearch(items);
				// Search succeed
				System.out.println("Search items is successful");
				printUnitsNames(items);
				logout();
			}
			@Override
			public void onFailure(int errorCode, Throwable throwableError) {
				super.onFailure(errorCode, throwableError);
				// search item failed, print error
				System.out.println(Errors.getErrorText(errorCode));
				logout();
			}
		});
	}

	private void printUnitsNames(Item... items){
		if (items!=null && items.length>0) {
			System.out.println(String.format("%d units found\r\nPrinting their names...", items.length));
			//Print items names
			for (Item item : items)
				System.out.println(String.format("\t%s", item.getName()));
		}
	}
	// Logout
	private void logout(){
		session.logout(new ResponseHandler() {
			@Override
			public void onSuccess(String response) {
				super.onSuccess(response);
				// logout succeed
				System.out.println("Logout successfully");
				System.exit(0);
			}

			@Override
			public void onFailure(int errorCode, Throwable throwableError) {
				super.onFailure(errorCode, throwableError);
				// logout failed, print error
				System.out.println(Errors.getErrorText(errorCode));
				System.exit(0);
			}
		});
	}

	@Override
	public void run() {
		// get instance of current Session
		session=Session.getInstance();
		login();
	}

	public static void main(String[] args){
		new Thread(new UnitsSearchExample()).start();
	}
}

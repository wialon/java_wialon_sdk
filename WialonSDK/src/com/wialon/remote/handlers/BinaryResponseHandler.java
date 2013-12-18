package com.wialon.remote.handlers;

public class BinaryResponseHandler extends ResponseHandler {

	public void onSuccessBinary(byte[] data){
		if (callback!=null && callback instanceof BinaryResponseHandler)
			((BinaryResponseHandler)callback).onSuccessBinary(data);
	}
}

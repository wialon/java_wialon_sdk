package com.wialon.remote.handlers;

import com.wialon.render.Layer;

public class LayerResponseHandler extends ResponseHandler {
	public void onSuccessLayer(Layer layer){
		if (callback!=null && callback instanceof LayerResponseHandler)
			((LayerResponseHandler)callback).onSuccessLayer(layer);
	}
}

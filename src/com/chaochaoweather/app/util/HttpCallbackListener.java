package com.chaochaoweather.app.util;

public interface HttpCallbackListener {

	void finish(String response);
	
	void onError(Exception e);
}

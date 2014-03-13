package com.xively.android.cloudservice;

import java.util.ArrayList;

public interface ICloudService {
	ArrayList<String> getFeeds();
	Response putDatapoint(double dVal, String sDatastreamName);
	
}

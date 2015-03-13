package com.zozot.OEM.cloudservice;

import com.zozot.OEM.cloudservice.Response;

interface IHttpService {
    void setApiKey(in String apiKey);

     Response getFeed();
     Response createDatastream(in String body);
 }
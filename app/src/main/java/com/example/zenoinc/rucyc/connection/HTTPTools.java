package com.example.zenoinc.rucyc.connection;

import java.net.URLEncoder;
import java.util.Map;

public class HTTPTools {
    public static String PostParamaters(Map<String, Object> params){
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            try{
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            } catch (Exception e){
                //Log.e(, e.getMessage());
            }
        }
        return postData.toString();
    }
}
package com.rxcode.rxdownload.util;

import retrofit2.Response;

public class HttpHelper {

    public static String getFileName(Response response){
        String v = response.headers().get("Content-Disposition");
        String rfileName = "";
        if (v != null) {
            String[] rs;
            if (v.contains("=")) {
                rs = v.split("=");
                if(rs.length>1)
                    rfileName = rs[1];
                if (rfileName.contains("'")) {
                    rs = v.split("'");
                    if(rs.length>2)
                        rfileName = rs[2];
                }
            }
        }
        return rfileName;
    }
}

package com.rxcode.rxdownload.api;

import io.reactivex.Flowable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.*;

public interface HttpGetService {
    @Streaming
    @GET
    Flowable<Response<ResponseBody>> getFile(@Url String url,@Header("Range")String range);

    @Streaming
    @GET
    Flowable<Response<ResponseBody>> getFile(@Url String url);

    @HEAD
    @Headers({"Connection:close","Range:bytes=0-"})
    Flowable<Response<Void>> checkHeader(@Url String url);


}

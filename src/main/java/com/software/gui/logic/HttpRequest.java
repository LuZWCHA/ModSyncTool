package com.software.gui.logic;

import com.software.beans.jsonbean.VersionJsonData;
import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface HttpRequest{
    @GET
    Flowable<VersionJsonData> getVersion(@Url String url);
}

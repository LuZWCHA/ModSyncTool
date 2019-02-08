package com.software.gui.logic;

import com.software.beans.jsonbean.VersionData;
import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface HttpRequest{
    @GET
    Flowable<VersionData> getVersion(@Url String url);
}

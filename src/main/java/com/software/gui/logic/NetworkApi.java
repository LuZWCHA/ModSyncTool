package com.software.gui.logic;

import com.software.beans.TransMod;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;

import java.util.List;

public interface NetworkApi {
    @GET
    Flowable<List<TransMod>> getModList(@Url String url);
}

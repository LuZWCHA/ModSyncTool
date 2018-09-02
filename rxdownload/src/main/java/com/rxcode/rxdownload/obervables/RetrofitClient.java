package com.rxcode.rxdownload.obervables;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import io.reactivex.annotations.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;

public enum RetrofitClient {
    INSTANCE;
    private Retrofit retrofit;
    RetrofitClient(){
        List<Protocol> protocols = new ArrayList<>();
        protocols.add(Protocol.HTTP_2);//(Protocol.HTTP_2);
        protocols.add(Protocol.HTTP_1_1);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .protocols(protocols)
                .retryOnConnectionFailure(true)
                .build();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("http://luzhengwei")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public<T> T create(Class<T> tClass){
        return retrofit.create(tClass);
    }


    public String getBaseUrl(){
        return retrofit.baseUrl().toString();
    }
}

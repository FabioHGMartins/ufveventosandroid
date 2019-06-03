package com.labd2m.vma.ufveventos.util;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by vma on 26/08/2017.
 */

public class RetrofitAPI {
    public Retrofit retrofit(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new retrofit2.Retrofit.Builder()
                //.baseUrl("http://meettest.esy.es/API/api.php/")
                .baseUrl("http://www.siseventos.ufv.br/API/api.php/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit;
    }

    public Retrofit retrofit2(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl("http://www.siseventos.ufv.br/esqueciasenha.php/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit;
    }
}
package com.example.hackernews;

import android.icu.text.CaseMap;

import com.google.gson.Gson;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActionsApi {
    Integer quantity = -1;
    Request instanceQuantityRequest
        = new Request.Builder()
                     .url("https://hacker-news.firebaseio.com/v0/maxitem.json?print=pretty")
                     .build();
    OkHttpClient httpClient;
    Gson gson;

    ActionsApi(OkHttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    Observable<Integer> getQuantityReq() {
        return Observable.create((ObservableOnSubscribe<Response>) emitter -> {
            Call req = httpClient.newCall(instanceQuantityRequest);
            emitter.setCancellable(req::cancel);

            emitter.onNext(req.execute());
        })
        .subscribeOn(Schedulers.io())
        .map(e -> e.body().string())
        .map(e -> gson.fromJson(e, Integer.class));
    }
}

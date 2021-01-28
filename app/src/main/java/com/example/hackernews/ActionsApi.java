package com.example.hackernews;

import android.icu.text.CaseMap;
import android.util.TimeUtils;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Emitter;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableObserver;
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

    public Observable<Integer> liveQuantity() {
        return Observable.create(e -> {
            updateQuantityWidthDaley(e, new Disposable[] {null});
        });
    }
    private void updateQuantityWidthDaley(Emitter<Integer> e, Disposable[] disposable) {
        disposable[0]
        = updateQuantity()
            .delay(1, TimeUnit.SECONDS)
            .subscribeWith(new DisposableObserver<Integer>() {
                @Override
                public void onNext(@NonNull Integer integer) {
                    e.onNext(integer);
                    updateQuantityWidthDaley(e, disposable);
                }

                @Override
                public void onError(@NonNull Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
    }

    public Observable<Integer> updateQuantity() {
        return getQuantityReq().observeOn(AndroidSchedulers.mainThread()).map(e -> {
            this.quantity = e;
            return e;
        });
    }

    private Observable<Integer> getQuantityReq() {
        return Observable.create((ObservableOnSubscribe<Response>) emitter -> {
            Call req = httpClient.newCall(instanceQuantityRequest);
            emitter.setCancellable(req::cancel);

            emitter.onNext(req.execute());
            emitter.onComplete();
        })
        .subscribeOn(Schedulers.io())
        .map(e -> e.body().string())
        .map(e -> gson.fromJson(e, Integer.class));
    }
}

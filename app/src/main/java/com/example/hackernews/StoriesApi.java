package com.example.hackernews;

import android.util.Log;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.operators.observable.ObservableCreate;
import io.reactivex.rxjava3.observers.DefaultObserver;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StoriesApi {
    private OkHttpClient httpClient;
    private Gson gson;

    private List<Integer> ids = new ArrayList<>();
    private Integer currentStep = 1;
    private final Integer sizeStep = 50;

    StoriesApi(OkHttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    private Observable<List<Integer>> getIds() {
        return Observable.create(e -> {

            if(!ids.isEmpty()) {
                e.onNext(ids);
                e.onComplete();

                return;
            }

            Request idsRequest
                    = new Request.Builder()
                    .url("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty")
                    .build();

            Call call = httpClient.newCall(idsRequest);

            e.setCancellable(call::cancel);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException error) {
                    e.onError(error);
                }

                @Override
                public void onResponse(
                        @NotNull Call call,
                        @NotNull Response response
                ) throws IOException {
                    Integer[] idsList = gson.fromJson(response.body().string(),Integer[].class);
                    ids = Arrays.asList(idsList);
                    e.onNext(ids);

                    e.onComplete();
                }
            });
        });
    }

    public boolean hasNext() {
        if(ids.isEmpty()) {
            return true;
        } else return currentStep * sizeStep <= ids.size();
    }
    public Observable<List<Story>> nextStories() {
        return Observable.create(e -> {
            CompositeDisposable disposable = new CompositeDisposable();

            disposable.add(
                    getIds()
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableObserver<List<Integer>>() {
                            @Override
                            public void onNext(@NonNull List<Integer> idsList) {
                                disposable.add(
                                        getStories(idsList).subscribeWith(new DisposableObserver<List<Story>>() {
                                            @Override
                                            public void onNext(@NonNull List<Story> stories) {
                                                ++currentStep;

                                                e.onNext(stories);
                                                e.onComplete();
                                            }

                                            @Override
                                            public void onError(@NonNull Throwable err) {
                                                e.onError(err);
                                            }

                                            @Override
                                            public void onComplete() {

                                            }
                                        })
                                );
                            }

                            @Override
                            public void onError(@NonNull Throwable err) {
                                e.onError(err);
                            }

                            @Override
                            public void onComplete() {

                            }
                        })
            );

            e.setCancellable(disposable::dispose);
        });
    }

    private Observable<List<Story>> getStories(List<Integer> ids) {
        Log.v(
                "tag",
                "" + ids.size() + " " + (currentStep-1)*sizeStep + " " + currentStep*sizeStep
        );
        return new ObservableCreate<>((event) -> {
            List<Story> stories = new ArrayList<>();
            List<Observable<String>> storiesObservables = new ArrayList<>();

            for(int t=(currentStep-1)*sizeStep; t<currentStep*sizeStep && t<ids.size(); t++) {
                Request storyReq = new Request.Builder()
                        .url("https://hacker-news.firebaseio.com/v0/item/" + ids.get(t) + ".json?print=pretty")
                        .build();

                storiesObservables.add(Observable.create(e -> {
                    Call call = httpClient.newCall(storyReq);

                    e.setCancellable(call::cancel);

                    httpClient.newCall(storyReq).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Log.v("Error", e.toString());
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            e.onNext(response.body().string());
                            e.onComplete();
                        }
                    });
                }).subscribeOn(Schedulers.io()).map(e -> (String) e));
            }

            Disposable disposable
                = Observable.merge(storiesObservables)
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(@NonNull String s) {
                        stories.add(gson.fromJson(s, Story.class));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.v("Error", e.toString());
                        event.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        event.onNext(stories);
                        event.onComplete();
                    }
                });

            event.setCancellable(disposable::dispose);
        });
    }
}

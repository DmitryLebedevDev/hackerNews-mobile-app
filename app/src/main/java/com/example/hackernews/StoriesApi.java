package com.example.hackernews;

import android.util.Log;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.internal.operators.single.SingleToObservable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StoriesApi {
    private final OkHttpClient httpClient;
    private final Gson gson;

    private List<Integer> ids = new ArrayList<>();
    private Integer currentStep = 0;
    private final Integer sizeStep = 50;

    StoriesApi(OkHttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    public boolean hasNext() {
        if(ids.isEmpty()) {
            return true;
        } else return (currentStep + 1) * sizeStep <= ids.size();
    }

    public Single<List<Story>> nextStories() {
        return Single.create(e -> {
            CompositeDisposable disposable = new CompositeDisposable();

            disposable.add(
                getIds()
                    .observeOn(Schedulers.io())
                    .subscribeWith(new DisposableSingleObserver<List<Integer>>() {
                        @Override
                        public void onSuccess(@NonNull List<Integer> idsList) {
                            disposable.add(
                                getStories(idsList).subscribeWith(
                                    new DisposableSingleObserver<List<Story>>() {
                                        @Override
                                        public void onSuccess(@NonNull List<Story> stories) {
                                            ++currentStep;

                                            e.onSuccess(stories);
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable err) {
                                            e.onError(err);
                                        }
                                    })
                            );
                        }

                        @Override
                        public void onError(@NonNull Throwable err) {
                            e.onError(err);
                        }
                    })
            );

            e.setCancellable(disposable::dispose);
        });
    }

    private Single<List<Integer>> getIds() {
        return Single.create(e -> {
            if(!ids.isEmpty()) {
                e.onSuccess(ids);

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
                    e.onSuccess(ids);
                }
            });
        });
    }



    private @NonNull Single<ArrayList<Story>> getStories(List<Integer> ids) {
        Log.v(
            "tag",
            "" + ids.size() + " " + currentStep*sizeStep + " " + (currentStep+1)*sizeStep
        );

        Integer[] stepIds = (Integer[]) Arrays.copyOfRange(
            ids.toArray(), currentStep*sizeStep,(currentStep+1)*sizeStep
        );

        return Observable
        .fromArray(stepIds)
        .concatMapEager(id -> Single.create((SingleOnSubscribe<Story>) e -> {
                Story.getInstanceOfApiInObservableEmit(
                    id, e, Story.class, httpClient, gson
                );
            })
            .subscribeOn(Schedulers.io())
            .toObservable())
        .reduceWith(
            ArrayList::new,
            (storiesList, story) -> {
                storiesList.add(story);
                return storiesList;
            }
        );
    }
}

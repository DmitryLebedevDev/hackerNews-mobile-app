package com.example.hackernews;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
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
        Request idsRequest
            = new Request.Builder()
                         .url("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty")
                         .build();

        return Observable.create(e -> {
            Call call = httpClient.newCall(idsRequest);

            e.setCancellable(call::cancel);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException error) {
                    e.onError(error);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    e.onNext(response.body());
                }
            });
        });
    }

    public List<Story> getStories() {

    }
}

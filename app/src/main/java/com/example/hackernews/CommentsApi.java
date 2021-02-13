package com.example.hackernews;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class CommentsApi {
    Gson gson;
    OkHttpClient http;
    Integer[] parentComments;

    private final Integer stepSize = 10;
    private final Integer currentStep = 0;

    CommentsApi(Integer[] parentComments, Gson gson, OkHttpClient http) {
        this.gson = gson;
        this.http = http;
        this.parentComments = parentComments;
    }

    boolean hasNextParentComments() {
        return currentStep * stepSize < parentComments.length;
    }

    Single<ArrayList<Comment>> getNextParentComments() {
        int startIndex = currentStep*stepSize;
        int endIndex = (currentStep+1)*stepSize;
        return
        Observable.fromArray(
            Arrays.copyOfRange(parentComments, startIndex, endIndex)
        ).concatMapEager(idComment -> Observable.create((ObservableOnSubscribe<Comment>) e -> {
            Call req = http.newCall(
                ActionType.createRequestForGet(idComment)
            );
            e.setCancellable(req::cancel);

            req.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException err) {
                    e.onComplete();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    e.onNext(
                        gson.fromJson(response.body().string(), Comment.class)
                    );
                    e.onComplete();
                }
            });
        })).subscribeOn(Schedulers.io()).reduceWith(
                ArrayList::new,
                (comments, comment) -> {
                    comments.add(comment);
                    return comments;
                }
        );
    }
}

package com.example.hackernews;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.internal.operators.single.SingleToObservable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class CommentsApi {
    private final Gson gson;
    private final OkHttpClient http;
    private final Integer[] parentComments;

    private final Integer stepSize = 20;
    private Integer currentStep = 0;

    public CommentsApi(Integer[] parentComments, Gson gson, OkHttpClient http) {
        this.gson = gson;
        this.http = http;
        this.parentComments = parentComments;
    }

    public boolean hasNextParentComments() {
        return (currentStep + 1) * stepSize <= parentComments.length;
    }

    public Single<ArrayList<Comment>> getNextParentComments() {
        int startIndex = currentStep*stepSize;
        int endIndex = (currentStep+1)*stepSize;
        return
        getCommentsOfArrayIds(
            Arrays.copyOfRange(parentComments, startIndex, endIndex)
        )
        .doOnSuccess(e -> ++currentStep);
    }

    private Single<ArrayList<Comment>> getCommentsOfArrayIds(Integer[] ids) {
        return Observable.fromArray(ids).concatMapEager(idComment ->
            Single.create((SingleOnSubscribe<Comment>) e -> {
                ActionType.getInstanceOfApiInObservableEmit(
                    idComment, e, Comment.class, http, gson
                );
            })
            .subscribeOn(Schedulers.io())
            .toObservable()
        ).reduceWith(
            ArrayList::new,
            (comments, comment) -> {
                comments.add(comment);
                return  comments;
            }
        );
    }
}

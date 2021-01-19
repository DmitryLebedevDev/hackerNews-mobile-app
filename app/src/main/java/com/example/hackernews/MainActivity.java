package com.example.hackernews;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Inflater;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.operators.observable.ObservableCreate;
import io.reactivex.rxjava3.observers.DefaultObserver;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    final OkHttpClient httpClient = new OkHttpClient();
    final Gson gson = new Gson();
    ViewGroup vList;
    ProgressBar vListLoading;

    @Override
    @SuppressLint("ResourceType")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vList = findViewById(R.id.story_list);
        vListLoading = findViewById(R.id.story_list_loading);

        Observable<Integer[]> ids = Observable.fromCallable(() -> {
            Request storiesIdsReq = new Request.Builder()
                    .url("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty")
                    .build();
            return httpClient.newCall(storiesIdsReq).execute();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map((e) -> gson.fromJson(e.body().string(), Integer[].class));

        ids.subscribeWith(new DisposableObserver<Integer[]>() {
            @Override
            public void onNext(@NonNull Integer[] s) {
                Log.v("data", Arrays.toString(s));
                getStories(s).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<List<Story>>() {
                    @Override
                    public void onNext(@NonNull List<Story> stories) {
                        Log.v("new", "list");
                        LayoutInflater inf = getLayoutInflater();

                        for(Story storyData : stories) {
                            View storyView = inf.inflate(R.layout.story_item_list, vList, false);
                            TextView story = storyView.findViewById(R.id.story_item);
                            story.setText(storyData.title);

                            vList.addView(storyView, vList.getChildCount()-1);
                        }

                        vListLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.v("ERRRRRRRRRRRRRRRRRRRRR", e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.v("data", e.toString());
            }

            @Override
            public void onComplete() {
                Log.v("data", "ids");
            }
        });
    }

    public Observable<List<Story>> getStories(Integer[] ids) {
        Log.v("start", Arrays.toString(ids));
        Log.v("start", "" + ids.length);
        return new ObservableCreate<>((event) -> {
            List<Story> stories = new ArrayList<>();
            List<Observable<String>> storiesObservables = new ArrayList<>();

            for(int t=0; t<50; t++) {
                Request storyReq = new Request.Builder()
                        .url("https://hacker-news.firebaseio.com/v0/item/" + ids[t] + ".json?print=pretty")
                        .build();

                storiesObservables.add(Observable.create(e -> {
                   Call call = new OkHttpClient().newCall(storyReq);

                   e.setCancellable(call::cancel);

                   httpClient.newCall(storyReq).enqueue(new Callback() {
                       @Override
                       public void onFailure(@NotNull Call call, @NotNull IOException e) {
                           Log.v("Error", e.toString());
                       }

                       @Override
                       public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                           //Gson gson = new Gson();
                           //Integer[] ids = gson.fromJson(response.body().string(), Integer[].class);
                           e.onNext(response.body().string());
                           e.onComplete();
                       }
                   });
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(e -> (String) e));
            }

            Observable.merge(storiesObservables)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DefaultObserver<String>() {
                @Override
                public void onNext(@NonNull String s) {
                    Log.v("", "new");
                    stories.add(gson.fromJson(s, Story.class));
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    Log.v("Error", e.toString());
                    event.onError(e);
                }

                @Override
                public void onComplete() {
                    Log.v("End", stories.get(0).title);
                    event.onNext(stories);
                    event.onComplete();
                }
            });
        });
    }
}
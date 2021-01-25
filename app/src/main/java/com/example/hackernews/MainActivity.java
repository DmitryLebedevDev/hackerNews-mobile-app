package com.example.hackernews;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.service.controls.templates.ControlButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    final StoriesApi storiesApi = new StoriesApi(httpClient, gson);
    ViewGroup vList;
    ProgressBar vListLoading;
    Button vListLoadButton;

    @Override
    @SuppressLint("ResourceType")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vList = findViewById(R.id.story_list);
        vListLoading = findViewById(R.id.story_list_loading);
        vListLoadButton = findViewById(R.id.load_stories);

        loadStories();
    }

    public void loadStories(View view) {
        Log.v("tag", "start loading");
        loadStories();
    }
    public void loadStories() {
        vListLoadButton.setVisibility(View.GONE);
        vListLoading.setVisibility(View.VISIBLE);

        storiesApi.nextStories().subscribeWith(new DefaultObserver<List<Story>>() {
            @Override
            public void onNext(@NonNull List<Story> stories) {
                LayoutInflater inf = getLayoutInflater();

                for(Story storyData : stories) {
                    View storyView = inf.inflate(R.layout.story_item_list, vList, false);
                    TextView story = storyView.findViewById(R.id.story_item);
                    story.setText(storyData.title);

                    vList.addView(storyView, vList.getChildCount()-2);
                }

                vListLoading.setVisibility(View.GONE);

                if (storiesApi.hasNext())
                    vListLoadButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
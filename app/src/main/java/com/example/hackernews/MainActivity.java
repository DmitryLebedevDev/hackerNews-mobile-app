package com.example.hackernews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.controls.templates.ControlButton;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hackernews.R;
import com.example.hackernews.StoriesActivity;
import com.example.hackernews.StoriesApi;
import com.example.hackernews.Story;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.Inflater;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.operators.observable.ObservableCreate;
import io.reactivex.rxjava3.observers.DefaultObserver;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String OPEN_STORY = "com.example.hackernews.OPEN_STORY";

    final OkHttpClient httpClient = new OkHttpClient();
    final Gson gson = new Gson();
    final StoriesApi storiesApi = new StoriesApi(httpClient, gson);

    Disposable disposable;
    
    ViewGroup   vList;
    ProgressBar vListLoading;
    Button      vListLoadButton;

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

        this.disposable
                = storiesApi.nextStories()
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribeWith(new DisposableSingleObserver<List<Story>>() {
              @Override
              public void onSuccess(@NonNull List<Story> stories) {
                  LayoutInflater inf = getLayoutInflater();

                  for(Story story : stories) {
                      View storyView = StoryItem.inflateStoryItem(
                              inf, vList, story, openStoryActivity(story), MainActivity.this
                      );

                      ViewGroup.MarginLayoutParams marginParams
                              = (ViewGroup.MarginLayoutParams) storyView.getLayoutParams();

                      marginParams.setMargins(
                              0,
                              0,
                              0,
                              getResources().getDimensionPixelSize(R.dimen.story_default_margin)
                      );

                      vList.addView(storyView, vList.getChildCount()-2);
                  }

                  vListLoading.setVisibility(View.GONE);

                  if (storiesApi.hasNext())
                    vListLoadButton.setVisibility(View.VISIBLE);
              }

            @Override
            public void onError(@NonNull Throwable e) {}
        });
    }

    View.OnClickListener openStoryActivity(Story story) {
        return v -> {
            Intent intent = new Intent(this, StoryActivity.class);
            intent.putExtra(MainActivity.OPEN_STORY, gson.toJson(story));

            startActivity(intent);
        };
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();

        super.onDestroy();
    }
}

class CustomTypefaceSpan extends TypefaceSpan {
    private final Typeface newType;

    public CustomTypefaceSpan(String family, Typeface type) {
        super(family);
        newType = type;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        applyCustomTypeFace(ds, newType);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        applyCustomTypeFace(paint, newType);
    }

    private static void applyCustomTypeFace(Paint paint, Typeface tf) {
        paint.setTypeface(tf);
    }
}
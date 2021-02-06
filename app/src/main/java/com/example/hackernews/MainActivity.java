package com.example.hackernews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
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

    public void toStories(View view) {
        Intent intent = new Intent(this, StoriesActivity.class);

        startActivity(intent);
    }

    public void loadStories(View view) {
        Log.v("tag", "start loading");
        loadStories();
    }
    public void loadStories() {
        vListLoadButton.setVisibility(View.GONE);
        vListLoading.setVisibility(View.VISIBLE);

        storiesApi.nextStories()
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribeWith(new DefaultObserver<List<Story>> () {
            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(@NonNull List<Story> stories) {
                LayoutInflater inf = getLayoutInflater();

                for(Story storyData : stories) {
                    View storyView = inf.inflate(R.layout.new_story_item, vList, false);
                    TextView vStoryTitle    = storyView.findViewById(R.id.story_title);
                    TextView vStoryLikes    = storyView.findViewById(R.id.story_likes);
                    TextView vStoryComments = storyView.findViewById(R.id.story_comments);
                    TextView vStoryAgo      = storyView.findViewById(R.id.story_ago);
                    TextView vStoryBy       = storyView.findViewById(R.id.story_by);
                    Long currentDate        = new Date().getTime();

                    SpannableString storyTitle = new SpannableString(storyData.title);
                    LeadingMarginSpan startStrMargin = new LeadingMarginSpan.Standard(50, 0);
                    storyTitle.setSpan(
                            startStrMargin, 0, storyTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    vStoryTitle.setText(storyTitle);
                    vStoryLikes.setText(storyData.score.toString());

                    if(storyData.descendants != null) {
                        vStoryComments.setText(storyData.descendants.toString());
                    } else {
                        vStoryComments.setText("0");
                    }

                    vStoryAgo.setText(" | " + storyData.getTimeAgo(currentDate) + " | ");
                    vStoryBy.setText("by " + storyData.by);

                    vList.addView(storyView, vList.getChildCount()-2);
                }

                vListLoading.setVisibility(View.GONE);

                if (storiesApi.hasNext())
                    vListLoadButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() {}
        });
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
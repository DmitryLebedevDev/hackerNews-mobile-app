package com.example.hackernews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.Inflater;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public class StoryActivity extends AppCompatActivity {
    Gson gson = new Gson();
    OkHttpClient http = new OkHttpClient();
    CommentsApi commentsApi;

    View vStory;
    ViewGroup vCommentList;
    ProgressBar vCommentListLoadingIcon;
    Button vCommentsLoadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        vStory = this.findViewById(R.id.story_template_activity_story);
        vCommentList = this.findViewById(R.id.story_comments_list);
        vCommentsLoadBtn = this.findViewById(R.id.story_comments_list_load_btn);
        vCommentListLoadingIcon = this.findViewById(R.id.story_comments_list_load_icon);

        Intent intent = getIntent();
        Story story = gson.fromJson(
            intent.getStringExtra(MainActivity.OPEN_STORY), Story.class
        );

        StoryItem.inflateStoryItem(
            vStory, story, StoryActivity.this
        );

        commentsApi = new CommentsApi(story.kids, gson, http);

        loadComments();
    }

    void loadComments() {
        if(commentsApi.hasNextParentComments()) {
            vCommentListLoadingIcon.setVisibility(View.VISIBLE);
            vCommentsLoadBtn.setVisibility(View.GONE);

            LayoutInflater inf = getLayoutInflater();

            commentsApi.getNextParentComments()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    new DisposableSingleObserver<ArrayList<Comment>>() {
                        @Override
                        public void onSuccess(@NonNull ArrayList<Comment> comments) {
                            for(Comment comment : comments) {
                                View vComment = CommentItem.inflateCommentTemplate(
                                    inf, vCommentList, comment, StoryActivity.this
                                );

                                vCommentList.addView(vComment, vCommentList.getChildCount()-2);
                            }

                            vCommentListLoadingIcon.setVisibility(View.GONE);
                            if(commentsApi.hasNextParentComments()) {
                                vCommentsLoadBtn.setVisibility(View.VISIBLE);
                            }
                        }
                        @Override
                        public void onError(@NonNull Throwable e) {}
                    });
        } else {
            vCommentsLoadBtn.setVisibility(View.GONE);
            vCommentListLoadingIcon.setVisibility(View.GONE);
        }
    }
    public void loadComments(View btn) {
        loadComments();
    }
}
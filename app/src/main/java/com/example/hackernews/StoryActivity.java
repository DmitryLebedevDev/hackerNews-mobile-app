package com.example.hackernews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

public class StoryActivity extends AppCompatActivity {
    Gson gson = new Gson();
    View vStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        vStory = this.findViewById(R.id.story_template_activity_story);
        LayoutInflater inf = getLayoutInflater();

        Intent intent = getIntent();
        Story story = gson.fromJson(
            intent.getStringExtra(MainActivity.OPEN_STORY), Story.class
        );

        StoryItem.inflateStoryItem(
                vStory, story
        );
    }
}
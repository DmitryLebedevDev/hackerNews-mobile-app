package com.example.hackernews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;

public class StoryActivity extends AppCompatActivity {
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        Intent intent = getIntent();
        Story story = gson.fromJson(
            intent.getStringExtra(MainActivity.OPEN_STORY), Story.class
        );
    }
}
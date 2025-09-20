package com.example.taptheball;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
    private String difficulty;
    private int gameTime = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("GameActivity", "GameActivity started!");

        // Get difficulty from Intent
        difficulty = getIntent().getStringExtra("DIFFICULTY");
        if ("Medium".equals(difficulty)) gameTime = 45;
        else if ("Hard".equals(difficulty)) gameTime = 30;

        Log.d("GameActivity", "Difficulty: " + difficulty + ", Game Time: " + gameTime + " seconds");

        // ✅ Create GameView directly and set as content view
        gameView = new GameView(this, gameTime, difficulty);
        setContentView(gameView);
        Log.d("GameActivity", "GameView added as content view!");
    }
}

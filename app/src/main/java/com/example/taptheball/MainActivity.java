package com.example.taptheball;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button easyButton = findViewById(R.id.easyButton);
        Button mediumButton = findViewById(R.id.mediumButton);
        Button hardButton = findViewById(R.id.hardButton);

        easyButton.setOnClickListener(view -> startGame("Easy"));
        mediumButton.setOnClickListener(view -> startGame("Medium"));
        hardButton.setOnClickListener(view -> startGame("Hard"));
    }

    private void startGame(String difficulty) {
        Log.d("MainActivity", "Starting game with difficulty: " + difficulty);

        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra("DIFFICULTY", difficulty);
        startActivity(intent);
    }
}











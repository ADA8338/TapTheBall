package com.example.taptheball;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private int ballX, ballY, ballRadius = 50;
    private int ball2X, ball2Y, ball2Radius = 50; // Second ball for Medium
    private Paint paint;
    private String difficulty;
    private Random random;
    private boolean isRunning = true;
    private Thread gameThread;
    private SurfaceHolder holder;
    private int score = 0;
    private int gameTime;
    private long startTime;
    private boolean gameOver = false;
    private int highScore;
    private boolean isNewHighScore = false; // Track if a new high score is achieved

    // Challenge variables
    private int obstacleX, obstacleY, obstacleSize = 100;
    private boolean hasObstacle = false;

    public GameView(Context context, int gameTime, String difficulty) {
        super(context);
        this.gameTime = gameTime;
        this.difficulty = difficulty;

        holder = getHolder();
        holder.addCallback(this);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(50);
        random = new Random();

        loadHighScore(context); // Load High Score for the selected difficulty

        Log.d("GameView", "GameView constructor called!");
    }

    private void spawnBall() {
        if (getWidth() == 0 || getHeight() == 0) {
            Log.e("GameView", "Screen size not ready! Retrying in 100ms...");
            postDelayed(this::spawnBall, 100);
            return;
        }

        ballX = random.nextInt(getWidth() - 100) + 50;
        ballY = random.nextInt(getHeight() - 200) + 100;

        // For Medium Difficulty, spawn second ball
        if ("Medium".equals(difficulty)) {
            ball2X = random.nextInt(getWidth() - 100) + 50;
            ball2Y = random.nextInt(getHeight() - 200) + 100;
        }

        // Randomly add obstacles for Medium Difficulty
        if ("Medium".equals(difficulty) && !hasObstacle) {
            obstacleX = random.nextInt(getWidth() - obstacleSize);
            obstacleY = random.nextInt(getHeight() - obstacleSize);
            hasObstacle = true;
        }

        Log.d("GameView", "New balls spawned at: X=" + ballX + " Y=" + ballY);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GameView", "Surface created! Starting game thread.");
        startTime = System.currentTimeMillis();
        gameThread = new Thread(this);
        isRunning = true;
        gameThread.start();
        spawnBall(); // Ensure first ball appears
    }

    @Override
    public void run() {
        while (isRunning) {
            if (!holder.getSurface().isValid()) {
                continue;
            }

            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            if (elapsedTime >= gameTime) {
                gameOver = true;
                isRunning = false;
                saveHighScore(getContext()); // Save High Score when game ends
            }

            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                drawGame(canvas, elapsedTime);
                holder.unlockCanvasAndPost(canvas);
            }

            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void drawGame(Canvas canvas, long elapsedTime) {
        canvas.drawColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        if (gameOver) {
            paint.setColor(Color.WHITE);
            canvas.drawText("Game Over!", getWidth() / 3, getHeight() / 2, paint);
            canvas.drawText("Final Score: " + score, getWidth() / 3, getHeight() / 2 + 60, paint);

            // Show message based on whether the player achieved a new High Score
            if (isNewHighScore) {
                paint.setColor(Color.GREEN);
                canvas.drawText("🎉 Congratulations! New High Score! 🎉", getWidth() / 6, getHeight() / 2 + 120, paint);
            } else {
                paint.setColor(Color.RED);
                canvas.drawText("😢 You Lost! Try Again!", getWidth() / 4, getHeight() / 2 + 120, paint);
            }
            return;
        }

        // Draw first ball
        paint.setColor(Color.RED);
        canvas.drawCircle(ballX, ballY, ballRadius, paint);

        // Draw second ball for Medium difficulty
        if ("Medium".equals(difficulty)) {
            paint.setColor(Color.BLUE);
            canvas.drawCircle(ball2X, ball2Y, ball2Radius, paint);
        }

        // Draw obstacle for Medium difficulty
        if ("Medium".equals(difficulty) && hasObstacle) {
            paint.setColor(Color.YELLOW);
            canvas.drawRect(obstacleX, obstacleY, obstacleX + obstacleSize, obstacleY + obstacleSize, paint);
        }

        // Draw Score & Timer at the bottom
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        int bottomY = getHeight() - 50; // Position near bottom
        canvas.drawText("Score: " + score, 50, bottomY, paint);
        canvas.drawText("Time: " + (gameTime - elapsedTime) + "s", getWidth() - 250, bottomY, paint);
        canvas.drawText("High Score: " + highScore, getWidth() / 3, bottomY, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !gameOver) {
            float touchX = event.getX();
            float touchY = event.getY();

            // Check if player tapped the first ball
            double distance = Math.pow(touchX - ballX, 2) + Math.pow(touchY - ballY, 2);
            if (distance <= Math.pow(ballRadius, 2)) {
                score++;
                Log.d("GameView", "Ball tapped! Score: " + score);
                spawnBall();
            }

            // Check if player tapped the second ball for Medium difficulty
            if ("Medium".equals(difficulty)) {
                double distance2 = Math.pow(touchX - ball2X, 2) + Math.pow(touchY - ball2Y, 2);
                if (distance2 <= Math.pow(ball2Radius, 2)) {
                    score++;
                    Log.d("GameView", "Second ball tapped! Score: " + score);
                    spawnBall();
                }
            }

            // Check if player tapped the obstacle (for Medium difficulty)
            if ("Medium".equals(difficulty)) {
                if (touchX >= obstacleX && touchX <= obstacleX + obstacleSize &&
                        touchY >= obstacleY && touchY <= obstacleY + obstacleSize) {
                    score--; // Penalty for hitting the obstacle
                    Log.d("GameView", "Obstacle hit! Score: " + score);
                }
            }
        }
        return true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Save High Score for Each Difficulty
    private void saveHighScore(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("HighScores", Context.MODE_PRIVATE);
        int savedHighScore = prefs.getInt(difficulty, 0);

        if (score > savedHighScore) {
            isNewHighScore = true; // New high score
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(difficulty, score);
            editor.apply();
            highScore = score; // Update high score
            Log.d("GameView", "🎉 New High Score Saved! Difficulty: " + difficulty + ", Score: " + score);
        } else {
            isNewHighScore = false; // Did not beat high score
        }
    }

    // Load High Score for Each Difficulty
    private void loadHighScore(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("HighScores", Context.MODE_PRIVATE);
        highScore = prefs.getInt(difficulty, 0);
        Log.d("GameView", "Loaded High Score for " + difficulty + ": " + highScore);
    }
}

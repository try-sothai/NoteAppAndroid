package com.social7.androidnoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.social7.androidnoteapp.preferences.preferences;

public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable openMainActivity = () -> {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dark mode preference before view creation
        preferences prefs = new preferences(this);
        preferences.applyDarkMode(prefs.isDarkMode());

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        handler.postDelayed(openMainActivity, 2000);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openMainActivity);
        super.onDestroy();
    }
}

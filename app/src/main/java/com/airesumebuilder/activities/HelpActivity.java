package com.airesumebuilder.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.google.android.material.appbar.MaterialToolbar;

/** Help & FAQ screen. */
public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}

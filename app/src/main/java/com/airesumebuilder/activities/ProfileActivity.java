package com.airesumebuilder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.models.Profile;
import com.airesumebuilder.repositories.ProfileRepository;
import com.airesumebuilder.utils.PreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Displays the active user profile and offers edit navigation.
 */
public class ProfileActivity extends AppCompatActivity {

    private ProfileRepository profileRepo;
    private PreferenceManager prefs;
    private Profile           activeProfile;

    private TextView tvName, tvHeadline;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        profileRepo = new ProfileRepository(this);
        prefs       = new PreferenceManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvName     = findViewById(R.id.tvName);
        tvHeadline = findViewById(R.id.tvHeadline);

        MaterialButton btnEdit = findViewById(R.id.btnEditProfile);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v ->
                    startActivity(new Intent(this, EditProfileActivity.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        executor.execute(() -> {
            long id = prefs.getActiveProfileId();
            if (id > 0) activeProfile = profileRepo.getById(id);
            handler.post(this::updateUi);
        });
    }

    private void updateUi() {
        if (tvName == null) return;

        if (activeProfile != null) {
            tvName.setText(activeProfile.getFullName());

            if (tvHeadline != null) {
                String headline = activeProfile.getHeadline();
                tvHeadline.setText(headline != null && !headline.isEmpty()
                        ? headline : activeProfile.getLocationString());
            }
        } else {
            tvName.setText(getString(R.string.my_profile));
            if (tvHeadline != null) {
                tvHeadline.setText("Tap Edit to create your profile");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

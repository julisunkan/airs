package com.airesumebuilder.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.airesumebuilder.R;
import com.airesumebuilder.models.Profile;
import com.airesumebuilder.repositories.ProfileRepository;
import com.airesumebuilder.utils.PreferenceManager;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Edit profile activity – allows creating or updating a user profile.
 */
public class EditProfileActivity extends AppCompatActivity {

    private ProfileRepository profileRepo;
    private PreferenceManager prefs;
    private Profile           profile;

    private TextInputEditText etFirstName, etLastName, etHeadline,
            etEmail, etPhone, etCity, etCountry, etLinkedin, etGithub, etBio;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         handler  = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileRepo = new ProfileRepository(this);
        prefs       = new PreferenceManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        bindFields();

        long profileId = prefs.getActiveProfileId();
        if (profileId > 0) {
            executor.execute(() -> {
                profile = profileRepo.getById(profileId);
                handler.post(this::populateFields);
            });
        } else {
            profile = new Profile();
        }

        MaterialButton btnSave = findViewById(R.id.btnSave);
        if (btnSave != null) btnSave.setOnClickListener(v -> saveProfile());
    }

    private void bindFields() {
        etFirstName  = findViewById(R.id.etFirstName);
        etLastName   = findViewById(R.id.etLastName);
        etHeadline   = findViewById(R.id.etHeadline);
        etEmail      = findViewById(R.id.etEmail);
        etPhone      = findViewById(R.id.etPhone);
        etCity       = findViewById(R.id.etCity);
        etCountry    = findViewById(R.id.etCountry);
        etLinkedin   = findViewById(R.id.etLinkedin);
        etGithub     = findViewById(R.id.etGithub);
        etBio        = findViewById(R.id.etBio);
    }

    private void populateFields() {
        if (profile == null) return;
        setText(etFirstName, profile.getFirstName());
        setText(etLastName,  profile.getLastName());
        setText(etHeadline,  profile.getHeadline());
        setText(etEmail,     profile.getEmail());
        setText(etPhone,     profile.getPhone());
        setText(etCity,      profile.getCity());
        setText(etCountry,   profile.getCountry());
        setText(etLinkedin,  profile.getLinkedin());
        setText(etGithub,    profile.getGithub());
        setText(etBio,       profile.getBio());
    }

    private void saveProfile() {
        if (profile == null) profile = new Profile();
        profile.setFirstName( getText(etFirstName));
        profile.setLastName(  getText(etLastName));
        profile.setHeadline(  getText(etHeadline));
        profile.setEmail(     getText(etEmail));
        profile.setPhone(     getText(etPhone));
        profile.setCity(      getText(etCity));
        profile.setCountry(   getText(etCountry));
        profile.setLinkedin(  getText(etLinkedin));
        profile.setGithub(    getText(etGithub));
        profile.setBio(       getText(etBio));

        executor.execute(() -> {
            long id;
            if (profile.getId() > 0) {
                profileRepo.update(profile);
                id = profile.getId();
            } else {
                id = profileRepo.insert(profile);
                profile.setId(id);
            }
            prefs.setActiveProfileId(id);
            handler.post(() -> {
                UiUtils.showSnackbar(
                        findViewById(android.R.id.content), "Profile saved");
                finish();
            });
        });
    }

    private void setText(TextInputEditText et, String v) {
        if (et != null && v != null) et.setText(v);
    }

    private String getText(TextInputEditText et) {
        return et != null ? UiUtils.getText(et) : "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

package com.airesumebuilder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.models.Resume;
import com.airesumebuilder.repositories.ResumeRepository;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Template picker activity. Shows a 2-column grid of available resume templates.
 * Tapping "Use Template" creates a new resume with the selected template and opens
 * the Resume Builder.
 */
public class TemplatesActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ── Template data ─────────────────────────────────────────────────────────

    /** Simple POJO for a template entry. */
    private static class TemplateItem {
        final String name;
        final String description;
        final String emoji;

        TemplateItem(String name, String description, String emoji) {
            this.name        = name;
            this.description = description;
            this.emoji       = emoji;
        }
    }

    private static List<TemplateItem> buildTemplates() {
        List<TemplateItem> list = new ArrayList<>();
        list.add(new TemplateItem("Modern",       "Clean, contemporary layout",          "✨"));
        list.add(new TemplateItem("Professional", "Classic professional look",            "💼"));
        list.add(new TemplateItem("Executive",    "Executive-level presentation",         "🏆"));
        list.add(new TemplateItem("Minimal",      "Simple and distraction-free",          "🔲"));
        list.add(new TemplateItem("Corporate",    "Structured corporate style",           "🏢"));
        list.add(new TemplateItem("Creative",     "Stand out with a creative touch",      "🎨"));
        list.add(new TemplateItem("Academic",     "Perfect for academia & research",      "🎓"));
        list.add(new TemplateItem("Engineering",  "Technical skills-forward",             "⚙️"));
        list.add(new TemplateItem("Medical",      "Healthcare professional format",       "🏥"));
        list.add(new TemplateItem("Finance",      "Numbers-focused finance layout",       "📊"));
        list.add(new TemplateItem("Sales",        "Achievement-driven sales format",      "📈"));
        list.add(new TemplateItem("Legal",        "Clean, formal legal format",           "⚖️"));
        list.add(new TemplateItem("Government",   "Official government-style CV",         "🏛️"));
        list.add(new TemplateItem("Student",      "Entry-level student friendly",         "📚"));
        list.add(new TemplateItem("Internship",   "Highlight potential and eagerness",    "🌟"));
        list.add(new TemplateItem("Timeline",     "Chronological visual timeline",        "⏳"));
        list.add(new TemplateItem("One Page",     "Fits everything on one page",          "📋"));
        list.add(new TemplateItem("Two Page",     "Detailed two-page format",             "📑"));
        list.add(new TemplateItem("ATS Simple",   "Optimised for ATS scanning",           "🤖"));
        list.add(new TemplateItem("Dark Theme",   "Bold dark professional look",          "🌙"));
        list.add(new TemplateItem("Elegant",      "Refined, elegant presentation",        "💎"));
        return list;
    }

    // ── Activity lifecycle ────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_templates);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvTemplates);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setAdapter(new TemplateAdapter(buildTemplates(), this::onTemplateSelected));
    }

    private void onTemplateSelected(TemplateItem item) {
        // Create a new resume with the chosen template and open the builder
        executor.execute(() -> {
            ResumeRepository repo = new ResumeRepository(this);
            Resume resume = new Resume();
            resume.setTitle("My " + item.name + " Resume");
            resume.setTemplate(item.name.toLowerCase().replace(" ", "_"));
            long id = repo.insert(resume);

            runOnUiThread(() -> {
                if (id > 0) {
                    Intent i = new Intent(this, ResumeBuilderActivity.class);
                    i.putExtra(ResumeBuilderActivity.EXTRA_RESUME_ID, id);
                    startActivity(i);
                    finish();
                } else {
                    UiUtils.showSnackbar(
                            findViewById(android.R.id.content), "Failed to create resume");
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    // ── Inner adapter ─────────────────────────────────────────────────────────

    interface OnTemplateClickListener {
        void onSelect(TemplateItem item);
    }

    private static class TemplateAdapter
            extends RecyclerView.Adapter<TemplateAdapter.VH> {

        private final List<TemplateItem>    items;
        private final OnTemplateClickListener listener;

        TemplateAdapter(List<TemplateItem> items, OnTemplateClickListener listener) {
            this.items    = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_template_card, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            TemplateItem item = items.get(position);
            h.tvEmoji.setText(item.emoji);
            h.tvName.setText(item.name);
            h.tvDesc.setText(item.description);
            h.btnUse.setOnClickListener(v -> { if (listener != null) listener.onSelect(item); });
            h.itemView.setOnClickListener(v -> { if (listener != null) listener.onSelect(item); });
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView     tvEmoji, tvName, tvDesc;
            MaterialButton btnUse;

            VH(View v) {
                super(v);
                tvEmoji = v.findViewById(R.id.tvTemplateEmoji);
                tvName  = v.findViewById(R.id.tvTemplateName);
                tvDesc  = v.findViewById(R.id.tvTemplateDesc);
                btnUse  = v.findViewById(R.id.btnUseTemplate);
            }
        }
    }
}

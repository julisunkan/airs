package com.airesumebuilder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.models.Resume;
import com.airesumebuilder.utils.DateUtils;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying a list of resumes.
 */
public class ResumeAdapter extends RecyclerView.Adapter<ResumeAdapter.ViewHolder> {

    public interface OnResumeActionListener {
        void onResumeClick(Resume resume);
        void onFavoriteToggle(Resume resume, boolean isFavorite);
        void onMoreClick(Resume resume, View anchor);
    }

    private List<Resume>              items = new ArrayList<>();
    private OnResumeActionListener    listener;

    public ResumeAdapter(OnResumeActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Resume> resumes) {
        this.items = resumes != null ? resumes : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addItem(Resume resume) {
        items.add(0, resume);
        notifyItemInserted(0);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Resume getItem(int position) {
        return items.get(position);
    }

    public int getPosition(long resumeId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == resumeId) return i;
        }
        return -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resume_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Resume r = items.get(position);

        h.tvTitle.setText(r.getTitle() != null ? r.getTitle() : "Untitled");
        h.tvDate.setText(DateUtils.formatRelative(r.getUpdatedAt()));
        h.tvTemplate.setText(r.getTemplate() != null ? capitalise(r.getTemplate()) : "");

        // Score chip
        int score = r.getOverallScore();
        if (score > 0) {
            h.chipScore.setText(score + "%");
            h.chipScore.setVisibility(View.VISIBLE);
        } else {
            h.chipScore.setVisibility(View.GONE);
        }

        // Favourite star
        h.btnFavorite.setImageResource(r.isFavorite()
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onResumeClick(r);
        });

        h.btnFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteToggle(r, !r.isFavorite());
        });

        h.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onMoreClick(r, v);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvTitle, tvDate, tvTemplate;
        Chip        chipScore;
        ImageButton btnFavorite, btnMore;

        ViewHolder(View v) {
            super(v);
            tvTitle     = v.findViewById(R.id.tvResumeTitle);
            tvDate      = v.findViewById(R.id.tvResumeDate);
            tvTemplate  = v.findViewById(R.id.tvResumeTemplate);
            chipScore   = v.findViewById(R.id.chipScore);
            btnFavorite = v.findViewById(R.id.btnFavorite);
            btnMore     = v.findViewById(R.id.btnMore);
        }
    }

    private String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

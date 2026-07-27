package com.airesumebuilder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.models.DashboardItem;

import java.util.List;

/**
 * Adapter for the home-screen dashboard grid.
 */
public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    // ── Action constants ─────────────────────────────────────────────────────
    public static final int ACTION_CREATE_RESUME   = 0;
    public static final int ACTION_MY_RESUMES      = 1;
    public static final int ACTION_TEMPLATES       = 2;
    public static final int ACTION_AI_REVIEW       = 3;
    public static final int ACTION_COVER_LETTER    = 4;
    public static final int ACTION_INTERVIEW_PREP  = 5;
    public static final int ACTION_CAREER_COACH    = 6;
    public static final int ACTION_PORTFOLIO       = 7;
    public static final int ACTION_JOB_TRACKER     = 8;
    public static final int ACTION_ANALYTICS       = 9;
    public static final int ACTION_FAVORITES       = 10;
    public static final int ACTION_SETTINGS        = 11;
    public static final int ACTION_HELP            = 12;
    public static final int ACTION_ABOUT           = 13;

    public interface OnItemClickListener {
        void onItemClick(int action);
    }

    private final List<DashboardItem>   items;
    private final OnItemClickListener   listener;

    public DashboardAdapter(List<DashboardItem> items, OnItemClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardItem item = items.get(position);
        holder.tvIcon.setText(item.getEmoji());
        holder.tvTitle.setText(item.getTitle());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item.getAction());
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle;
        ViewHolder(View v) {
            super(v);
            tvIcon  = v.findViewById(R.id.tvIcon);
            tvTitle = v.findViewById(R.id.tvTitle);
        }
    }
}

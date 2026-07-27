package com.airesumebuilder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list of resume section entries inside ResumeBuilderActivity.
 * Each entry shows its type label, a primary title, an optional subtitle,
 * and a delete button.
 */
public class SectionsAdapter extends RecyclerView.Adapter<SectionsAdapter.ViewHolder> {

    // ── Data model ────────────────────────────────────────────────────────────

    /** One displayable row in the section list. */
    public static class SectionItem {
        public final String type;      // e.g. "Education"
        public final String title;     // primary text, e.g. "B.Sc. Computer Science"
        public final String subtitle;  // secondary text, e.g. "MIT"
        public final long   id;        // row id in the DB table
        public final String table;     // DB table name (for deletion)

        public SectionItem(String type, String title, String subtitle,
                           long id, String table) {
            this.type     = type;
            this.title    = title != null ? title : "";
            this.subtitle = subtitle != null ? subtitle : "";
            this.id       = id;
            this.table    = table;
        }
    }

    // ── Listener ──────────────────────────────────────────────────────────────

    public interface OnDeleteListener {
        void onDelete(SectionItem item);
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private final List<SectionItem> items          = new ArrayList<>();
    private final OnDeleteListener  deleteListener;

    public SectionsAdapter(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setItems(List<SectionItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    // ── RecyclerView.Adapter ──────────────────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_section_entry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SectionItem item = items.get(position);

        holder.tvType.setText(item.type);
        holder.tvTitle.setText(item.title.isEmpty() ? item.type + " Entry" : item.title);

        if (!item.subtitle.isEmpty()) {
            holder.tvSubtitle.setText(item.subtitle);
            holder.tvSubtitle.setVisibility(View.VISIBLE);
        } else {
            holder.tvSubtitle.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView      tvType;
        final TextView      tvTitle;
        final TextView      tvSubtitle;
        final MaterialButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType     = itemView.findViewById(R.id.tvSectionType);
            tvTitle    = itemView.findViewById(R.id.tvSectionTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSectionSubtitle);
            btnDelete  = itemView.findViewById(R.id.btnDeleteSection);
        }
    }
}

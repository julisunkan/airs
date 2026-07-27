package com.airesumebuilder.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.models.JobApplication;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the job tracker list.
 */
public class JobAdapter extends RecyclerView.Adapter<JobAdapter.ViewHolder> {

    public interface OnJobActionListener {
        void onJobClick(JobApplication job);
        void onEditClick(JobApplication job);
        void onDeleteClick(JobApplication job, int position);
    }

    private List<JobApplication>  items = new ArrayList<>();
    private OnJobActionListener   listener;

    public JobAdapter(OnJobActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<JobApplication> jobs) {
        this.items = jobs != null ? jobs : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        JobApplication job = items.get(position);

        h.tvPosition.setText(job.getPosition());
        h.tvCompany.setText(job.getCompany());
        h.chipStatus.setText(job.getStatus());

        // Colour-code status
        int colorRes = statusColor(job.getStatus());
        h.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(h.itemView.getContext(), colorRes)));

        h.tvDate.setText(job.getApplicationDate() != null
                ? "Applied: " + job.getApplicationDate() : "");

        if (job.getNotes() != null && !job.getNotes().isEmpty()) {
            h.tvNotes.setText(job.getNotes());
            h.tvNotes.setVisibility(View.VISIBLE);
        } else {
            h.tvNotes.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onJobClick(job); });
        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEditClick(job); });
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(job, h.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    private int statusColor(String status) {
        if (status == null) return R.color.status_applied;
        switch (status) {
            case JobApplication.STATUS_INTERVIEW: return R.color.status_interview;
            case JobApplication.STATUS_OFFER:     return R.color.status_offer;
            case JobApplication.STATUS_REJECTED:  return R.color.status_rejected;
            case JobApplication.STATUS_WITHDRAWN: return R.color.status_withdrawn;
            default:                              return R.color.status_applied;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvPosition, tvCompany, tvDate, tvNotes;
        Chip        chipStatus;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View v) {
            super(v);
            tvPosition  = v.findViewById(R.id.tvPosition);
            tvCompany   = v.findViewById(R.id.tvCompany);
            tvDate      = v.findViewById(R.id.tvDate);
            tvNotes     = v.findViewById(R.id.tvNotes);
            chipStatus  = v.findViewById(R.id.chipStatus);
            btnEdit     = v.findViewById(R.id.btnEdit);
            btnDelete   = v.findViewById(R.id.btnDelete);
        }
    }
}

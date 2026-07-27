package com.airesumebuilder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.models.ChatMessage;
import com.airesumebuilder.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the AI chat interface. Renders two view types: user and AI messages.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final int VIEW_USER = ChatMessage.TYPE_USER;
    private static final int VIEW_AI   = ChatMessage.TYPE_AI;

    private final List<ChatMessage> messages = new ArrayList<>();

    public void addMessage(ChatMessage msg) {
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    /** Updates the last message (used to replace the loading placeholder). */
    public void updateLastMessage(ChatMessage msg) {
        if (!messages.isEmpty()) {
            messages.set(messages.size() - 1, msg);
            notifyItemChanged(messages.size() - 1);
        }
    }

    public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == VIEW_USER
                ? R.layout.item_chat_user
                : R.layout.item_chat_ai;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ChatMessage msg = messages.get(position);

        if (msg.isLoading()) {
            h.tvMessage.setText("…");
        } else {
            h.tvMessage.setText(msg.getContent());
        }

        if (h.tvTime != null) {
            h.tvTime.setText(DateUtils.formatTime(msg.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        ViewHolder(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime    = v.findViewById(R.id.tvTime);
        }
    }
}

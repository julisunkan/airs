package com.airesumebuilder.activities;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airesumebuilder.R;
import com.airesumebuilder.adapters.ChatAdapter;
import com.airesumebuilder.models.ChatMessage;
import com.airesumebuilder.network.GroqClient;
import com.airesumebuilder.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AI Career Coach – a chat interface powered by the Groq API.
 */
public class CareerCoachActivity extends AppCompatActivity {

    private static final String SYSTEM_PROMPT =
        "You are an expert career coach with 20+ years of experience. " +
        "Help users with resume building, career planning, salary negotiation, " +
        "skill development, job searching, interview preparation, and professional branding. " +
        "Be concise, practical, and encouraging. Format responses clearly.";

    private ChatAdapter          adapter;
    private RecyclerView         rvMessages;
    private TextInputEditText    etMessage;
    private View                 llTyping;
    private GroqClient           groqClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_career_coach);

        groqClient = GroqClient.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvMessages = findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter();
        rvMessages.setAdapter(adapter);

        etMessage = findViewById(R.id.etMessage);
        llTyping  = findViewById(R.id.llTyping);

        FloatingActionButton btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Opening greeting
        adapter.addMessage(new ChatMessage(
                "👋 Hi! I'm your AI Career Coach. Ask me anything about your career, " +
                "resume, interviews, salary, or professional development.",
                ChatMessage.TYPE_AI));
    }

    private void sendMessage() {
        String text = UiUtils.getText(etMessage);
        if (text.isEmpty()) return;

        etMessage.setText("");
        UiUtils.hideKeyboard(this);

        // Show user message
        adapter.addMessage(new ChatMessage(text, ChatMessage.TYPE_USER));
        scrollToBottom();

        // Show typing indicator
        llTyping.setVisibility(View.VISIBLE);
        ChatMessage loading = ChatMessage.loadingMessage();
        adapter.addMessage(loading);
        scrollToBottom();

        // Call AI
        groqClient.complete(SYSTEM_PROMPT, text, new GroqClient.AiCallback() {
            @Override
            public void onSuccess(String content) {
                runOnUiThread(() -> {
                    llTyping.setVisibility(View.GONE);
                    adapter.updateLastMessage(new ChatMessage(content, ChatMessage.TYPE_AI));
                    scrollToBottom();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    llTyping.setVisibility(View.GONE);
                    adapter.updateLastMessage(new ChatMessage(
                            "⚠️ " + errorMessage, ChatMessage.TYPE_AI));
                    scrollToBottom();
                });
            }
        });
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }
}

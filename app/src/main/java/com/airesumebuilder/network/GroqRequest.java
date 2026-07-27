package com.airesumebuilder.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request body sent to the Groq Chat Completions endpoint.
 */
public class GroqRequest {

    @SerializedName("model")
    private String model;

    @SerializedName("messages")
    private List<Message> messages;

    @SerializedName("temperature")
    private float temperature;

    @SerializedName("max_tokens")
    private int maxTokens;

    @SerializedName("stream")
    private boolean stream;

    public GroqRequest(String model, List<Message> messages, float temperature,
                       int maxTokens, boolean stream) {
        this.model       = model;
        this.messages    = messages;
        this.temperature = temperature;
        this.maxTokens   = maxTokens;
        this.stream      = stream;
    }

    // ── Nested Message class ─────────────────────────────────────────────────

    public static class Message {

        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        public Message(String role, String content) {
            this.role    = role;
            this.content = content;
        }

        public static Message system(String content) { return new Message("system", content); }
        public static Message user(String content)   { return new Message("user",   content); }
        public static Message assistant(String c)    { return new Message("assistant", c); }

        public String getRole()    { return role; }
        public String getContent() { return content; }
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String        getModel()       { return model; }
    public List<Message> getMessages()    { return messages; }
    public float         getTemperature() { return temperature; }
    public int           getMaxTokens()   { return maxTokens; }
    public boolean       isStream()       { return stream; }
}

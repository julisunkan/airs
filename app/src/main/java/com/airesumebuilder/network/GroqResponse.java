package com.airesumebuilder.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response body received from the Groq Chat Completions endpoint.
 */
public class GroqResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("model")
    private String model;

    @SerializedName("choices")
    private List<Choice> choices;

    @SerializedName("usage")
    private Usage usage;

    @SerializedName("error")
    private ApiError error;

    /** Returns the text of the first choice, or null if none. */
    public String getFirstContent() {
        if (choices == null || choices.isEmpty()) return null;
        Choice choice = choices.get(0);
        if (choice.getMessage() == null) return null;
        return choice.getMessage().getContent();
    }

    public boolean hasError() { return error != null; }
    public String  getErrorMessage() { return error != null ? error.getMessage() : null; }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String       getId()      { return id; }
    public String       getModel()   { return model; }
    public List<Choice> getChoices() { return choices; }
    public Usage        getUsage()   { return usage; }

    // ── Nested classes ───────────────────────────────────────────────────────

    public static class Choice {
        @SerializedName("message") private Message message;
        @SerializedName("finish_reason") private String finishReason;
        public Message getMessage()     { return message; }
        public String  getFinishReason(){ return finishReason; }
    }

    public static class Message {
        @SerializedName("role")    private String role;
        @SerializedName("content") private String content;
        public String getRole()    { return role; }
        public String getContent() { return content; }
    }

    public static class Usage {
        @SerializedName("prompt_tokens")     private int promptTokens;
        @SerializedName("completion_tokens") private int completionTokens;
        @SerializedName("total_tokens")      private int totalTokens;
        public int getPromptTokens()     { return promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public int getTotalTokens()      { return totalTokens; }
    }

    public static class ApiError {
        @SerializedName("message") private String message;
        @SerializedName("type")    private String type;
        @SerializedName("code")    private String code;
        public String getMessage() { return message; }
        public String getType()    { return type; }
        public String getCode()    { return code; }
    }
}

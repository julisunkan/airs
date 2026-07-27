package com.airesumebuilder.models;

/**
 * Represents a single chat message in the AI chat interface.
 */
public class ChatMessage {

    public static final int TYPE_USER = 0;
    public static final int TYPE_AI   = 1;

    private String  content;
    private int     type;
    private long    timestamp;
    private boolean isLoading; // true while the AI response is streaming

    public ChatMessage(String content, int type) {
        this.content   = content;
        this.type      = type;
        this.timestamp = System.currentTimeMillis();
        this.isLoading = false;
    }

    /** Creates a placeholder message shown while the AI is generating a response. */
    public static ChatMessage loadingMessage() {
        ChatMessage msg = new ChatMessage("", TYPE_AI);
        msg.isLoading = true;
        return msg;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String  getContent()                    { return content; }
    public void    setContent(String content)      { this.content = content; }
    public int     getType()                       { return type; }
    public void    setType(int type)               { this.type = type; }
    public long    getTimestamp()                  { return timestamp; }
    public void    setTimestamp(long timestamp)    { this.timestamp = timestamp; }
    public boolean isLoading()                     { return isLoading; }
    public void    setLoading(boolean loading)     { isLoading = loading; }
    public boolean isUser()                        { return type == TYPE_USER; }
}

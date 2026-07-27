package com.airesumebuilder.models;

/**
 * Represents a card on the home dashboard grid.
 */
public class DashboardItem {

    private String  emoji;
    private String  title;
    private int     action; // Matches DashboardAdapter.ACTION_* constants

    public DashboardItem(String emoji, String title, int action) {
        this.emoji  = emoji;
        this.title  = title;
        this.action = action;
    }

    public String getEmoji()  { return emoji; }
    public String getTitle()  { return title; }
    public int    getAction() { return action; }
}

package com.airesumebuilder.models;

/**
 * Represents one work-experience entry within a resume.
 */
public class Experience {

    private long    id;
    private long    resumeId;
    private String  company;
    private String  position;
    private String  location;
    private String  startDate;
    private String  endDate;
    private boolean isCurrent;
    private String  description;
    private String  achievements;
    private int     sortOrder;

    public Experience() {}

    // ── Getters & Setters ────────────────────────────────────────────────────
    public long    getId()                            { return id; }
    public void    setId(long id)                     { this.id = id; }
    public long    getResumeId()                      { return resumeId; }
    public void    setResumeId(long resumeId)         { this.resumeId = resumeId; }
    public String  getCompany()                       { return company; }
    public void    setCompany(String company)         { this.company = company; }
    public String  getPosition()                      { return position; }
    public void    setPosition(String position)       { this.position = position; }
    public String  getLocation()                      { return location; }
    public void    setLocation(String location)       { this.location = location; }
    public String  getStartDate()                     { return startDate; }
    public void    setStartDate(String v)             { this.startDate = v; }
    public String  getEndDate()                       { return endDate; }
    public void    setEndDate(String v)               { this.endDate = v; }
    public boolean isCurrent()                        { return isCurrent; }
    public void    setCurrent(boolean current)        { isCurrent = current; }
    public String  getDescription()                   { return description; }
    public void    setDescription(String v)           { this.description = v; }
    public String  getAchievements()                  { return achievements; }
    public void    setAchievements(String v)          { this.achievements = v; }
    public int     getSortOrder()                     { return sortOrder; }
    public void    setSortOrder(int sortOrder)        { this.sortOrder = sortOrder; }

    public String getDateRange() {
        String start = startDate != null ? startDate : "";
        String end   = isCurrent ? "Present" : (endDate != null ? endDate : "");
        if (start.isEmpty() && end.isEmpty()) return "";
        if (start.isEmpty()) return end;
        if (end.isEmpty())   return start;
        return start + " – " + end;
    }
}

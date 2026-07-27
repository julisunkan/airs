package com.airesumebuilder.models;

/** Represents a single skill entry within a resume. */
public class Skill {

    private long   id;
    private long   resumeId;
    private String name;
    private String level;      // e.g. Beginner / Intermediate / Advanced / Expert
    private String category;   // e.g. Programming / Soft Skills
    private int    sortOrder;

    public Skill() {}

    public Skill(long resumeId, String name, String level, String category) {
        this.resumeId = resumeId;
        this.name     = name;
        this.level    = level;
        this.category = category;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public long   getId()                       { return id; }
    public void   setId(long id)                { this.id = id; }
    public long   getResumeId()                 { return resumeId; }
    public void   setResumeId(long resumeId)    { this.resumeId = resumeId; }
    public String getName()                     { return name; }
    public void   setName(String name)          { this.name = name; }
    public String getLevel()                    { return level; }
    public void   setLevel(String level)        { this.level = level; }
    public String getCategory()                 { return category; }
    public void   setCategory(String category)  { this.category = category; }
    public int    getSortOrder()                { return sortOrder; }
    public void   setSortOrder(int sortOrder)   { this.sortOrder = sortOrder; }
}

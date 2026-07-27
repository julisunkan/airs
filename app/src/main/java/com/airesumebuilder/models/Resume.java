package com.airesumebuilder.models;

/**
 * Represents a resume record in the resumes table.
 */
public class Resume {

    private long    id;
    private long    profileId;
    private String  title;
    private String  template;
    private String  accentColor;
    private String  font;
    private boolean isFavorite;
    private int     atsScore;
    private int     overallScore;
    private String  tags;
    private String  sectionOrder;
    private long    createdAt;
    private long    updatedAt;

    public Resume() {
        template    = "modern";
        accentColor = "#1565C0";
        font        = "Default";
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public long    getId()                         { return id; }
    public void    setId(long id)                  { this.id = id; }

    public long    getProfileId()                  { return profileId; }
    public void    setProfileId(long profileId)    { this.profileId = profileId; }

    public String  getTitle()                      { return title; }
    public void    setTitle(String title)          { this.title = title; }

    public String  getTemplate()                   { return template; }
    public void    setTemplate(String template)    { this.template = template; }

    public String  getAccentColor()                { return accentColor; }
    public void    setAccentColor(String c)        { this.accentColor = c; }

    public String  getFont()                       { return font; }
    public void    setFont(String font)            { this.font = font; }

    public boolean isFavorite()                    { return isFavorite; }
    public void    setFavorite(boolean favorite)   { isFavorite = favorite; }

    public int     getAtsScore()                   { return atsScore; }
    public void    setAtsScore(int atsScore)       { this.atsScore = atsScore; }

    public int     getOverallScore()               { return overallScore; }
    public void    setOverallScore(int s)          { this.overallScore = s; }

    public String  getTags()                       { return tags; }
    public void    setTags(String tags)            { this.tags = tags; }

    public String  getSectionOrder()               { return sectionOrder; }
    public void    setSectionOrder(String s)       { this.sectionOrder = s; }

    public long    getCreatedAt()                  { return createdAt; }
    public void    setCreatedAt(long createdAt)    { this.createdAt = createdAt; }

    public long    getUpdatedAt()                  { return updatedAt; }
    public void    setUpdatedAt(long updatedAt)    { this.updatedAt = updatedAt; }
}

package com.airesumebuilder.models;

/**
 * Represents one education entry within a resume.
 */
public class Education {

    private long   id;
    private long   resumeId;
    private String institution;
    private String degree;
    private String field;
    private String startDate;
    private String endDate;
    private String gpa;
    private String description;
    private int    sortOrder;

    public Education() {}

    // ── Getters & Setters ────────────────────────────────────────────────────
    public long   getId()                          { return id; }
    public void   setId(long id)                   { this.id = id; }
    public long   getResumeId()                    { return resumeId; }
    public void   setResumeId(long resumeId)       { this.resumeId = resumeId; }
    public String getInstitution()                 { return institution; }
    public void   setInstitution(String v)         { this.institution = v; }
    public String getDegree()                      { return degree; }
    public void   setDegree(String degree)         { this.degree = degree; }
    public String getField()                       { return field; }
    public void   setField(String field)           { this.field = field; }
    public String getStartDate()                   { return startDate; }
    public void   setStartDate(String v)           { this.startDate = v; }
    public String getEndDate()                     { return endDate; }
    public void   setEndDate(String v)             { this.endDate = v; }
    public String getGpa()                         { return gpa; }
    public void   setGpa(String gpa)               { this.gpa = gpa; }
    public String getDescription()                 { return description; }
    public void   setDescription(String v)         { this.description = v; }
    public int    getSortOrder()                   { return sortOrder; }
    public void   setSortOrder(int sortOrder)      { this.sortOrder = sortOrder; }

    /** Returns a display string like "B.Sc. Computer Science – MIT" */
    public String getDisplayTitle() {
        StringBuilder sb = new StringBuilder();
        if (degree != null && !degree.isEmpty())      sb.append(degree);
        if (field  != null && !field.isEmpty())  {
            if (sb.length() > 0) sb.append(" ");
            sb.append(field);
        }
        if (institution != null && !institution.isEmpty()) {
            if (sb.length() > 0) sb.append(" – ");
            sb.append(institution);
        }
        return sb.length() > 0 ? sb.toString() : "Education Entry";
    }
}

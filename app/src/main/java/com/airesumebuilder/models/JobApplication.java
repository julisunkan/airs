package com.airesumebuilder.models;

/**
 * Represents a job application entry in the job_tracker table.
 */
public class JobApplication {

    public static final String STATUS_APPLIED    = "Applied";
    public static final String STATUS_INTERVIEW  = "Interview";
    public static final String STATUS_OFFER      = "Offer";
    public static final String STATUS_REJECTED   = "Rejected";
    public static final String STATUS_WITHDRAWN  = "Withdrawn";

    private long   id;
    private String company;
    private String position;
    private String status;
    private String applicationDate;
    private String interviewDate;
    private String offerAmount;
    private String notes;
    private String url;
    private long   createdAt;
    private long   updatedAt;

    public JobApplication() {
        status = STATUS_APPLIED;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public long   getId()                              { return id; }
    public void   setId(long id)                       { this.id = id; }
    public String getCompany()                         { return company; }
    public void   setCompany(String company)           { this.company = company; }
    public String getPosition()                        { return position; }
    public void   setPosition(String position)         { this.position = position; }
    public String getStatus()                          { return status; }
    public void   setStatus(String status)             { this.status = status; }
    public String getApplicationDate()                 { return applicationDate; }
    public void   setApplicationDate(String v)         { this.applicationDate = v; }
    public String getInterviewDate()                   { return interviewDate; }
    public void   setInterviewDate(String v)           { this.interviewDate = v; }
    public String getOfferAmount()                     { return offerAmount; }
    public void   setOfferAmount(String v)             { this.offerAmount = v; }
    public String getNotes()                           { return notes; }
    public void   setNotes(String notes)               { this.notes = notes; }
    public String getUrl()                             { return url; }
    public void   setUrl(String url)                   { this.url = url; }
    public long   getCreatedAt()                       { return createdAt; }
    public void   setCreatedAt(long createdAt)         { this.createdAt = createdAt; }
    public long   getUpdatedAt()                       { return updatedAt; }
    public void   setUpdatedAt(long updatedAt)         { this.updatedAt = updatedAt; }
}

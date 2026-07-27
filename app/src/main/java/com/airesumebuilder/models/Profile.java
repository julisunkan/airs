package com.airesumebuilder.models;

/**
 * Represents a user profile stored in the profiles table.
 */
public class Profile {

    private long   id;
    private String firstName;
    private String lastName;
    private String headline;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String linkedin;
    private String github;
    private String portfolio;
    private String website;
    private String bio;
    private String dateOfBirth;
    private String photoPath;
    private long   createdAt;
    private long   updatedAt;

    public Profile() {}

    // ── Getters & Setters ────────────────────────────────────────────────────

    public long   getId()          { return id; }
    public void   setId(long id)   { this.id = id; }

    public String getFirstName()                  { return firstName; }
    public void   setFirstName(String firstName)  { this.firstName = firstName; }

    public String getLastName()                   { return lastName; }
    public void   setLastName(String lastName)    { this.lastName = lastName; }

    public String getFullName() {
        String f = firstName != null ? firstName.trim() : "";
        String l = lastName  != null ? lastName.trim()  : "";
        if (f.isEmpty() && l.isEmpty()) return "Unnamed Profile";
        return (f + " " + l).trim();
    }

    public String getHeadline()                   { return headline; }
    public void   setHeadline(String headline)    { this.headline = headline; }

    public String getEmail()                      { return email; }
    public void   setEmail(String email)          { this.email = email; }

    public String getPhone()                      { return phone; }
    public void   setPhone(String phone)          { this.phone = phone; }

    public String getAddress()                    { return address; }
    public void   setAddress(String address)      { this.address = address; }

    public String getCity()                       { return city; }
    public void   setCity(String city)            { this.city = city; }

    public String getState()                      { return state; }
    public void   setState(String state)          { this.state = state; }

    public String getCountry()                    { return country; }
    public void   setCountry(String country)      { this.country = country; }

    public String getLinkedin()                   { return linkedin; }
    public void   setLinkedin(String linkedin)    { this.linkedin = linkedin; }

    public String getGithub()                     { return github; }
    public void   setGithub(String github)        { this.github = github; }

    public String getPortfolio()                  { return portfolio; }
    public void   setPortfolio(String portfolio)  { this.portfolio = portfolio; }

    public String getWebsite()                    { return website; }
    public void   setWebsite(String website)      { this.website = website; }

    public String getBio()                        { return bio; }
    public void   setBio(String bio)              { this.bio = bio; }

    public String getDateOfBirth()                { return dateOfBirth; }
    public void   setDateOfBirth(String d)        { this.dateOfBirth = d; }

    public String getPhotoPath()                  { return photoPath; }
    public void   setPhotoPath(String photoPath)  { this.photoPath = photoPath; }

    public long   getCreatedAt()                  { return createdAt; }
    public void   setCreatedAt(long createdAt)    { this.createdAt = createdAt; }

    public long   getUpdatedAt()                  { return updatedAt; }
    public void   setUpdatedAt(long updatedAt)    { this.updatedAt = updatedAt; }

    /** Returns a one-line location string, e.g. "London, UK". */
    public String getLocationString() {
        StringBuilder sb = new StringBuilder();
        if (city    != null && !city.isEmpty())    sb.append(city);
        if (country != null && !country.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }
        return sb.toString();
    }
}

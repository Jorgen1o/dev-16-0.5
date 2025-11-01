package cs151.application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class Student {
    private final SimpleStringProperty fullName = new SimpleStringProperty();
    private final SimpleStringProperty academicStatus = new SimpleStringProperty();
    private final SimpleStringProperty employed = new SimpleStringProperty();
    private final SimpleStringProperty jobDetails = new SimpleStringProperty();
    private final SimpleStringProperty programmingLanguages = new SimpleStringProperty();
    private final SimpleStringProperty databases = new SimpleStringProperty();
    private final SimpleStringProperty preferredRole = new SimpleStringProperty();
    private final SimpleStringProperty facultyComment = new SimpleStringProperty();
    private final SimpleStringProperty whiteListed = new SimpleStringProperty();
    private final SimpleStringProperty blackListed = new SimpleStringProperty();

    public Student(){}

    public Student(String name, String acad, String emp, String job, String langs, String dbs, String role, String facComment, String whiteListed, String blackListed) {
        fullName.set(name); academicStatus.set(acad); employed.set(emp);
        jobDetails.set(job); programmingLanguages.set(langs); databases.set(dbs); preferredRole.set(role);
        facultyComment.set(facComment);
        this.whiteListed.set(whiteListed);
        this.blackListed.set(blackListed);
    }

    public String getFullName() { return fullName.get(); }
    public String getAcademicStatus() { return academicStatus.get(); }
    public String getEmployed() { return employed.get(); }
    public String getJobDetails() { return jobDetails.get(); }
    public String getProgrammingLanguages() { return programmingLanguages.get(); }
    public String getDatabases() { return databases.get(); }
    public String getPreferredRole() { return preferredRole.get(); }
    public String getFacultyComment() { return facultyComment.get(); }
    public String getWhiteListed() { return whiteListed.get(); }
    public String getBlackListed() { return blackListed.get(); }
    public StringProperty whiteListedProperty() { return whiteListed; }
    public StringProperty blackListedProperty() { return blackListed; }

    private static String nz(String s) { return s == null ? "" : s; }

    public void setFullName(String v) { this.fullName.set(nz(v)); }
    public void setAcademicStatus(String v) { this.academicStatus.set(nz(v)); }
    public void setEmployed(String v) { this.employed.set(nz(v)); }
    public void setJobDetails(String v) { this.jobDetails.set(nz(v)); }
    public void setProgrammingLanguages(String v) { this.programmingLanguages.set(nz(v)); }
    public void setDatabases(String v) { this.databases.set(nz(v)); }
    public void setPreferredRole(String v) { this.preferredRole.set(nz(v)); }
    public void setFacultyComment(String comment) {
        this.facultyComment.set(comment);
    }
    public void setWhiteListed(String v) { this.whiteListed.set(nz(v)); }
    public void setBlackListed(String v) { this.blackListed.set(nz(v)); }
    // In Student.java
    public void addComment(String newComment) {
        String existing = getFacultyComment();
        if (existing == null || existing.isBlank()) {
            setFacultyComment(newComment.replace("\n", " "));
        } else {
            // Avoid raw newlines; join with a separator
            setFacultyComment(existing + " | " + newComment.replace("\n", " "));
        }
    }

}
package cs151.application;

import javafx.beans.property.SimpleStringProperty;

public class Student {
    private final SimpleStringProperty fullName = new SimpleStringProperty();
    private final SimpleStringProperty academicStatus = new SimpleStringProperty();
    private final SimpleStringProperty employed = new SimpleStringProperty();
    private final SimpleStringProperty jobDetails = new SimpleStringProperty();
    private final SimpleStringProperty programmingLanguages = new SimpleStringProperty();
    private final SimpleStringProperty databases = new SimpleStringProperty();
    private final SimpleStringProperty preferredRole = new SimpleStringProperty();

    public Student(String name, String acad, String emp, String job, String langs, String dbs, String role) {
        fullName.set(name); academicStatus.set(acad); employed.set(emp);
        jobDetails.set(job); programmingLanguages.set(langs); databases.set(dbs); preferredRole.set(role);
    }

    public String getFullName() { return fullName.get(); }
    public String getAcademicStatus() { return academicStatus.get(); }
    public String getEmployed() { return employed.get(); }
    public String getJobDetails() { return jobDetails.get(); }
    public String getProgrammingLanguages() { return programmingLanguages.get(); }
    public String getDatabases() { return databases.get(); }
    public String getPreferredRole() { return preferredRole.get(); }
}
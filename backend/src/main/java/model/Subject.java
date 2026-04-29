package com.timetable.model;

import java.util.List;
import java.util.ArrayList;

public class Subject {
    private String code;
    private String name;
    private boolean isLab;
    private int hoursPerWeek;
    private List<String> teachers;

    public Subject() { this.teachers = new ArrayList<>(); }

    public Subject(String code, String name, boolean isLab,
                   int hoursPerWeek, List<String> teachers) {
        this.code         = code;
        this.name         = name;
        this.isLab        = isLab;
        this.hoursPerWeek = hoursPerWeek;
        this.teachers     = teachers != null ? teachers : new ArrayList<>();
    }

    // Getters & setters
    public String getCode()                        { return code; }
    public void   setCode(String code)             { this.code = code; }
    public String getName()                        { return name; }
    public void   setName(String name)             { this.name = name; }
    public boolean isLab()                         { return isLab; }
    public void   setLab(boolean lab)              { this.isLab = lab; }
    public int    getHoursPerWeek()                { return hoursPerWeek; }
    public void   setHoursPerWeek(int h)           { this.hoursPerWeek = h; }
    public List<String> getTeachers()              { return teachers; }
    public void   setTeachers(List<String> t)      { this.teachers = t; }
}

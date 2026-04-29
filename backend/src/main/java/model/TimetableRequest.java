package com.timetable.model;

import java.util.List;

/**
 * The input object sent from the React frontend to the backend.
 * Contains everything needed to generate the timetable.
 */
public class TimetableRequest {
    private int             numSections;   // e.g. 3 → sections A, B, C
    private List<Subject>   subjects;      // subjects with teacher lists
    private List<Room>      rooms;         // available rooms
    private List<String>    days;          // e.g. ["Monday","Tuesday",...,"Saturday"]

    public TimetableRequest() {}

    public int           getNumSections()              { return numSections; }
    public void          setNumSections(int n)          { this.numSections = n; }
    public List<Subject> getSubjects()                 { return subjects; }
    public void          setSubjects(List<Subject> s)  { this.subjects = s; }
    public List<Room>    getRooms()                    { return rooms; }
    public void          setRooms(List<Room> r)        { this.rooms = r; }
    public List<String>  getDays()                     { return days; }
    public void          setDays(List<String> d)       { this.days = d; }
}

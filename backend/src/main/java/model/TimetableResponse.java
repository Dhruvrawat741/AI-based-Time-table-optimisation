package com.timetable.model;

import java.util.List;

/**
 * What the backend sends back to the frontend after generation.
 */
public class TimetableResponse {
    private List<SectionTimetable> sections;
    private String                 message;
    private boolean                success;

    public TimetableResponse() {}

    public TimetableResponse(List<SectionTimetable> sections, String message, boolean success) {
        this.sections = sections;
        this.message  = message;
        this.success  = success;
    }

    public List<SectionTimetable> getSections() { return sections; }
    public void setSections(List<SectionTimetable> s) { this.sections = s; }
    public String  getMessage()                { return message; }
    public void    setMessage(String m)        { this.message = m; }
    public boolean isSuccess()                 { return success; }
    public void    setSuccess(boolean s)       { this.success = s; }
}

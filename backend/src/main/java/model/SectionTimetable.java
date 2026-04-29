package com.timetable.model;

import java.util.List;
import java.util.Map;

/**
 * The complete timetable for ONE section (e.g. Section A).
 *
 * Structure:
 *   rows    = days (Mon, Tue, Wed, Thu, Fri, Sat)
 *   columns = time slots
 *   cell    = TimetableCell (subject + teacher + room)
 */
public class SectionTimetable {
    private String sectionName;   // "Section A", "Section B" ...
    private List<String> days;
    private List<TimeSlot> timeSlots;
    // Key: "Monday", Value: list of cells matching timeSlots order
    private Map<String, List<TimetableCell>> schedule;

    public SectionTimetable() {}

    public SectionTimetable(String sectionName, List<String> days,
                             List<TimeSlot> timeSlots,
                             Map<String, List<TimetableCell>> schedule) {
        this.sectionName = sectionName;
        this.days        = days;
        this.timeSlots   = timeSlots;
        this.schedule    = schedule;
    }

    public String                          getSectionName() { return sectionName; }
    public void                            setSectionName(String s) { this.sectionName = s; }
    public List<String>                    getDays()        { return days; }
    public void                            setDays(List<String> d) { this.days = d; }
    public List<TimeSlot>                  getTimeSlots()   { return timeSlots; }
    public void                            setTimeSlots(List<TimeSlot> t) { this.timeSlots = t; }
    public Map<String, List<TimetableCell>> getSchedule()  { return schedule; }
    public void                            setSchedule(Map<String, List<TimetableCell>> s) { this.schedule = s; }
}

package com.timetable.model;


/** Represents: what happens for a given section on a given day at a given time slot.
 */
public class TimetableCell {
    private String subjectCode;   // e.g. "TCS-401"
    private String subjectName;   // e.g. "Theory of Computation"
    private String teacherName;   // e.g. "Dr. Sharma"
    private String roomNumber;    // e.g. "CR-2"
    private boolean isLab;
    private String type;          // "class", "lab", "break", "lunch", "free"

    public TimetableCell() { this.type = "free"; }

    public TimetableCell(String subjectCode, String subjectName,
                         String teacherName, String roomNumber,
                         boolean isLab, String type) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
        this.roomNumber  = roomNumber;
        this.isLab       = isLab;
        this.type        = type;
    }

    public static TimetableCell breakCell(String label) {
        TimetableCell c = new TimetableCell();
        c.subjectName = label;
        c.type        = "break";
        return c;
    }

    public static TimetableCell lunchCell() {
        TimetableCell c = new TimetableCell();
        c.subjectName = "LUNCH";
        c.type        = "lunch";
        return c;
    }

    // Getters & setters
    public String  getSubjectCode()               { return subjectCode; }
    public void    setSubjectCode(String s)        { this.subjectCode = s; }
    public String  getSubjectName()               { return subjectName; }
    public void    setSubjectName(String s)        { this.subjectName = s; }
    public String  getTeacherName()               { return teacherName; }
    public void    setTeacherName(String t)        { this.teacherName = t; }
    public String  getRoomNumber()                { return roomNumber; }
    public void    setRoomNumber(String r)         { this.roomNumber = r; }
    public boolean isLab()                        { return isLab; }
    public void    setLab(boolean l)              { this.isLab = l; }
    public String  getType()                      { return type; }
    public void    setType(String t)              { this.type = t; }
}

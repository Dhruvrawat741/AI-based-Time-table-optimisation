package com.timetable.model;

/**
 * One time period in the day.
 *
 * Schedule (from image analysis):
 *   Class 1:  8:00 –  8:55   (55 min)
 *   Break:    8:55 –  9:00   (5 min)
 *   Class 2:  9:00 –  9:55   (55 min)
 *   Break:    9:55 – 10:00   (5 min)
 *   Class 3:  10:00 – 10:55  (55 min)
 *   Long break: 10:55 – 11:15 (20 min)
 *   Class 4:  11:15 – 12:10  (55 min) -- but 2hr gap means lunch 12:10-2:00
 *   Actually matching image: after 3 classes → 20 min break → then 2hr gap (lunch)
 *   Slots: 8:00, 9:00, 9:55, 11:10, 12:05, 1:00, 1:55, 3:10 (matches image exactly)
 */
public class TimeSlot {
    private int    index;      // 0,1,2,3...
    private String startTime;  // "8:00"
    private String endTime;    // "8:55"
    private String label;      // "8:00-8:55"
    private boolean isBreak;   // lunch / long break slot

    public TimeSlot() {}
    public TimeSlot(int index, String startTime, String endTime, boolean isBreak) {
        this.index     = index;
        this.startTime = startTime;
        this.endTime   = endTime;
        this.label     = startTime + "-" + endTime;
        this.isBreak   = isBreak;
    }

    public int     getIndex()              { return index; }
    public String  getStartTime()          { return startTime; }
    public String  getEndTime()            { return endTime; }
    public String  getLabel()              { return label; }
    public boolean isBreak()               { return isBreak; }
    public void    setIndex(int i)         { this.index = i; }
    public void    setStartTime(String s)  { this.startTime = s; }
    public void    setEndTime(String e)    { this.endTime = e; }
    public void    setLabel(String l)      { this.label = l; }
    public void    setBreak(boolean b)     { this.isBreak = b; }
}

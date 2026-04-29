package com.timetable.algorithm;

import com.timetable.model.*;

import java.util.*;

/**
 *  TIMETABLE GENERATOR
 *  TIME STRUCTURE (matching image exactly):
 *    Slot 0:  8:00 –  8:55   (Class 1)
 *    Slot 1:  9:00 –  9:55   (Class 2)         [5-min break between]
 *    Slot 2:  9:55 – 10:50   (Class 3)         [5-min break between]
 *    ─── 20-min break ──────────────────────────
 *    Slot 3: 11:10 – 12:05   (Class 4)
 *    ─── 2 hr gap (LUNCH) ───────────────────────
 *    Slot 4:  1:00 –  1:55   (Class 5)         [55 min, matching image]
 *    Slot 5:  1:55 –  2:50   (Class 6)
 *    Slot 6:  3:10 –  4:05   (Class 7 / optional)
 *  TEACHER ASSIGNMENT:
 *    If sections <= teachers → each section gets a unique teacher
 *    If sections >  teachers → teachers are reused (round-robin)
 *    A teacher cannot teach two sections at the SAME day+slot
 *  ROOM ASSIGNMENT:
 *    Rooms are assigned per section per slot (no double-booking)
 *    Lab subjects get lab rooms if available
 *  CONFLICT AVOIDANCE (Graph Coloring logic):
 *    A subject is placed only when:
 *      1. Teacher is free at that slot on that day
 *      2. Room is free at that slot on that day
 *      3. Subject hasn't exceeded its weekly hours
 */
public class TimetableGenerator {


    private static final List<String> DEFAULT_DAYS =
        Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");


    public static List<TimeSlot> buildTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();

        slots.add(new TimeSlot(0,  "8:00",  "8:55",  false));
        slots.add(new TimeSlot(1,  "9:00",  "9:55",  false));
        slots.add(new TimeSlot(2,  "9:55",  "10:50", false));
        slots.add(new TimeSlot(3,  "11:10", "12:05", false));
        slots.add(new TimeSlot(4,  "12:05", "1:00",  true));
        slots.add(new TimeSlot(5,  "1:00",  "1:55",  false));
        slots.add(new TimeSlot(6,  "1:55",  "2:50",  false));
        slots.add(new TimeSlot(7,  "3:10",  "4:05",  false));
        return slots;
    }

    /**
     * MAIN ENTRY POINT
     * Generates a complete timetable for all sections.
     *
     * @param request  input from frontend (sections, subjects, rooms, days)
     * @return list of SectionTimetable, one per section
     */
    public List<SectionTimetable> generate(TimetableRequest request) {
        int             numSections = request.getNumSections();
        List<Subject>   subjects    = request.getSubjects();
        List<Room>      rooms       = request.getRooms();
        List<String>    days        = request.getDays() != null && !request.getDays().isEmpty()
                                        ? request.getDays() : DEFAULT_DAYS;
        List<TimeSlot>  timeSlots   = buildTimeSlots();

        // Separate teaching slots from lunch
        List<TimeSlot> teachingSlots = new ArrayList<>();
        for (TimeSlot ts : timeSlots) {
            if (!ts.isBreak()) teachingSlots.add(ts);
        }

        // ── Section names: A, B, C, D ... ─────────────────────
        List<String> sectionNames = new ArrayList<>();
        for (int i = 0; i < numSections; i++) {
            sectionNames.add("Section " + (char)('A' + i));
        }

        // ── Assign teachers to sections per subject ────────────
        // teachers[subjectIdx][sectionIdx] = teacher name

        Map<String, String[]> teacherAssignment = assignTeachers(subjects, numSections);

        // ── Conflict tracking ──────────────────────────────────
        // teacherBusy: "teacherName|day|slotIdx" → true

        Set<String> teacherBusy = new HashSet<>();

        // roomBusy:    "roomNumber|day|slotIdx"  → true

        Set<String> roomBusy    = new HashSet<>();

        // ── Build timetable for each section ──────────────────

        List<SectionTimetable> result = new ArrayList<>();

        for (int secIdx = 0; secIdx < numSections; secIdx++) {
            String sectionName = sectionNames.get(secIdx);

            // Track how many sessions each subject has been placed this week
            Map<String, Integer> sessionsPlaced = new HashMap<>();
            for (Subject s : subjects) sessionsPlaced.put(s.getCode(), 0);

            // Build schedule: day → list of cells (one per timeSlot)
            Map<String, List<TimetableCell>> schedule = new LinkedHashMap<>();

            for (String day : days) {
                List<TimetableCell> dayCells = new ArrayList<>();

                // Track which subjects are already placed TODAY (one subject per day normally)
                Set<String> placedToday = new HashSet<>();

                for (TimeSlot slot : timeSlots) {
                    if (slot.isBreak()) {
                        dayCells.add(TimetableCell.lunchCell());
                        continue;
                    }

                    // Try to assign a subject to this slot
                    TimetableCell cell = tryAssignSlot(
                        subjects, secIdx, teacherAssignment,
                        sessionsPlaced, placedToday,
                        day, slot, rooms,
                        teacherBusy, roomBusy
                    );

                    dayCells.add(cell);
                }

                schedule.put(day, dayCells);
            }

            result.add(new SectionTimetable(sectionName, days, timeSlots, schedule));
        }

        return result;
    }

    /**
     * Assign teachers to sections for each subject.
     *
     * Rules:
     *   - If numSections <= numTeachers: each section gets a unique teacher
     *   - If numSections >  numTeachers: teachers are shared (round-robin)
     *   - Each teacher gets at most ceil(numSections / numTeachers) sections
     *
     * Returns: Map<subjectCode, String[numSections]>
     *   where String[i] = teacher name for section i
     */
    private Map<String, String[]> assignTeachers(List<Subject> subjects, int numSections) {
        Map<String, String[]> result = new HashMap<>();

        for (Subject subject : subjects) {
            List<String> teachers = subject.getTeachers();
            String[] assignment   = new String[numSections];

            if (teachers == null || teachers.isEmpty()) {
                Arrays.fill(assignment, "TBA");
            } else if (teachers.size() >= numSections) {
                // Enough teachers: each section gets a different teacher
                for (int i = 0; i < numSections; i++) {
                    assignment[i] = teachers.get(i);
                }
            } else {
                // Fewer teachers than sections: distribute round-robin
                // e.g. 2 teachers, 4 sections → T1,T2,T1,T2
                for (int i = 0; i < numSections; i++) {
                    assignment[i] = teachers.get(i % teachers.size());
                }
            }

            result.put(subject.getCode(), assignment);
        }

        return result;
    }

    /**
     * Try to place a subject into (day, slot) for a given section.
     *
     * Constraints checked:
     *   1. Subject has remaining sessions this week
     *   2. Teacher is not already teaching another section at this day+slot
     *   3. Room is available at this day+slot
     *   4. Same subject not placed twice on the same day
     */
    private TimetableCell tryAssignSlot(
            List<Subject>          subjects,
            int                    secIdx,
            Map<String, String[]>  teacherAssignment,
            Map<String, Integer>   sessionsPlaced,
            Set<String>            placedToday,
            String                 day,
            TimeSlot               slot,
            List<Room>             rooms,
            Set<String>            teacherBusy,
            Set<String>            roomBusy) {

        for (Subject subject : subjects) {
            String code    = subject.getCode();
            int    placed  = sessionsPlaced.getOrDefault(code, 0);
            int    maxWeek = subject.getHoursPerWeek();

            // Skip if this subject is already placed enough times
            if (placed >= maxWeek) continue;

            // Skip if already placed today
            if (placedToday.contains(code)) continue;

            // Get this section's teacher for this subject
            String[] teachers = teacherAssignment.get(code);
            String   teacher  = (teachers != null && secIdx < teachers.length)
                                 ? teachers[secIdx] : "TBA";

            // Check teacher availability
            String teacherKey = teacher + "|" + day + "|" + slot.getIndex();
            if (teacherBusy.contains(teacherKey)) continue;

            // Find an available room
            Room room = findAvailableRoom(rooms, subject.isLab(), day, slot.getIndex(), roomBusy);
            if (room == null) continue;

            // ── All constraints satisfied — assign! ──────────
            teacherBusy.add(teacherKey);
            roomBusy.add(room.getRoomNumber() + "|" + day + "|" + slot.getIndex());
            sessionsPlaced.put(code, placed + 1);
            placedToday.add(code);

            return new TimetableCell(
                code,
                subject.getName(),
                teacher,
                room.getRoomNumber(),
                subject.isLab(),
                subject.isLab() ? "lab" : "class"
            );
        }

        // No subject could be assigned to this slot
        TimetableCell free = new TimetableCell();
        free.setType("free");
        free.setSubjectName("—");
        return free;
    }


    private Room findAvailableRoom(List<Room> rooms, boolean needsLab,
                                   String day, int slotIdx, Set<String> roomBusy) {
        for (Room room : rooms) {
            // Prefer matching type but fall back if needed
            if (room.isLab() != needsLab) continue;
            String key = room.getRoomNumber() + "|" + day + "|" + slotIdx;
            if (!roomBusy.contains(key)) return room;
        }
        // Fallback: try any room regardless of type
        for (Room room : rooms) {
            String key = room.getRoomNumber() + "|" + day + "|" + slotIdx;
            if (!roomBusy.contains(key)) return room;
        }
        return null;
    }
}

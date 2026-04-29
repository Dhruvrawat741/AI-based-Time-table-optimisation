package com.timetable.service;

import com.timetable.algorithm.TimetableGenerator;
import com.timetable.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring service layer — thin wrapper around TimetableGenerator.
 * No database. All data lives in-memory during one request.
 */
@Service
public class TimetableService {

    private final TimetableGenerator generator = new TimetableGenerator();

    public TimetableResponse generate(TimetableRequest request) {
        try {
            if (request.getNumSections() <= 0)
                return new TimetableResponse(null, "Number of sections must be at least 1.", false);
            if (request.getSubjects() == null || request.getSubjects().isEmpty())
                return new TimetableResponse(null, "Please add at least one subject.", false);
            if (request.getRooms() == null || request.getRooms().isEmpty())
                return new TimetableResponse(null, "Please add at least one room.", false);

            List<SectionTimetable> sections = generator.generate(request);
            return new TimetableResponse(sections, "Timetable generated successfully!", true);

        } catch (Exception e) {
            return new TimetableResponse(null, "Error: " + e.getMessage(), false);
        }
    }

    /** Returns the fixed time slots so the frontend can show them */
    public List<TimeSlot> getTimeSlots() {
        return TimetableGenerator.buildTimeSlots();
    }
}

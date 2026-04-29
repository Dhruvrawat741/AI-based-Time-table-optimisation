package com.timetable.controller;

import com.timetable.model.*;
import com.timetable.service.TimetableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller.
 * POST /api/generate      → send TimetableRequest, get TimetableResponse
 * GET  /api/timeslots     → get the fixed list of time slots
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TimetableController {

    private final TimetableService service;

    public TimetableController(TimetableService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<TimetableResponse> generate(@RequestBody TimetableRequest request) {
        TimetableResponse response = service.generate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/timeslots")
    public ResponseEntity<List<TimeSlot>> getTimeSlots() {
        return ResponseEntity.ok(service.getTimeSlots());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Timetable System Running");
    }
}

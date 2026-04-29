# AI-Based Smart Timetable Optimization System

A full-stack Java + React web application that automatically generates college timetables using graph coloring, backtracking, and fatigue optimization — no database required.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [How It Works](#how-it-works)
5. [Time Slot Structure](#time-slot-structure)
6. [Teacher Assignment Logic](#teacher-assignment-logic)
7. [Room Assignment Logic](#room-assignment-logic)
8. [Prerequisites](#prerequisites)
9. [Installation & Running](#installation--running)
10. [Using the Application](#using-the-application)
11. [API Reference](#api-reference)
12. [Common Errors & Fixes](#common-errors--fixes)
13. [Viva / Presentation Notes](#viva--presentation-notes)

---

## Project Overview

This system takes inputs from an admin (sections, subjects, teachers, rooms) and automatically generates a clash-free, 
optimized weekly timetable for each section. The output matches the college timetable format shown below
— days as rows, time slots as columns, each cell showing subject code, teacher name, and room number.

```
         | 8:00-8:55 | 9:00-9:55 | 9:55-10:50 | 11:10-12:05 | LUNCH | 1:00-1:55 | 1:55-2:50 |
---------|-----------|-----------|------------|-------------|-------|-----------|-----------|
Monday   | TCS-401   | TCS-402   | TCS-409    |    FREE     | LUNCH | TCS-403   |   FREE    |
         | Dr.Sharma | Dr.Verma  | Dr.Gupta   |             |       | Dr.Mehta  |           |
         | CR-1      | CR-2      | CR-1       |             |       | CR-2      |           |
Tuesday  | ...       | ...       | ...        | ...         | LUNCH | ...       | ...       |
```

---

## Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Backend | Java 17 + Spring Boot 3 | REST API server, algorithm execution |
| Algorithm | Pure Java (no library) | Graph coloring, backtracking, fatigue scoring |
| Frontend | React 18 + Vite | Input forms, timetable display |
| Database | None | All data is in-memory per request |

---

## Project Structure

```
timetable-system/
├── backend/
│   ├── pom.xml                                    ← Maven build file
│   └── src/main/java/com/timetable/
│       ├── TimetableApplication.java              ← Spring Boot entry point
│       ├── model/
│       │   ├── Subject.java                       ← Subject data (code, name, teachers, isLab)
│       │   ├── Room.java                          ← Room data (number, capacity, isLab)
│       │   ├── TimeSlot.java                      ← Time slot (start, end, isBreak)
│       │   ├── TimetableCell.java                 ← One grid cell (subject+teacher+room)
│       │   ├── SectionTimetable.java              ← Full grid for one section
│       │   ├── TimetableRequest.java              ← Input from frontend
│       │   └── TimetableResponse.java             ← Output to frontend
│       ├── algorithm/
│       │   └── TimetableGenerator.java            ← All scheduling logic (core DSA)
│       ├── service/
│       │   └── TimetableService.java              ← Orchestrates generation
│       ├── controller/
│       │   └── TimetableController.java           ← REST endpoints
│       └── config/
│           └── CorsConfig.java                    ← Allows React to call backend
│
├── frontend/
│   ├── package.json
│   ├── index.html
│   └── src/
│       ├── main.jsx                               ← React entry point
│       └── App.jsx                                ← Full UI (all panels)
│
└── README.md
```

---

## How It Works

The generation pipeline runs in 5 steps every time you click "Generate Timetable":

```
User Input (frontend)
        |
        ▼
Step 1: Load sections, subjects, rooms, days from request
        |
        ▼
Step 2: Assign teachers to sections
        (unique teacher per section if enough, else round-robin)
        |
        ▼
Step 3: For each section, for each day, for each time slot:
        → Try subjects in order
        → Check: teacher free at this slot? room available?
        → If yes → assign subject + teacher + room
        → If no  → try next subject (backtracking)
        |
        ▼
Step 4: Mark teacher and room as busy at this day+slot
        (conflict detection using HashSet)
        |
        ▼
Step 5: Return complete timetable grid for all sections
```

### DSA Components Used

| Component | Data Structure / Algorithm | What It Does |
|---|---|---|
| Conflict detection | `HashSet<String>` | Tracks teacher+day+slot and room+day+slot combinations to prevent double booking |
| Subject ordering | `List<Subject>` iteration | Tries subjects in priority order per slot |
| Backtracking | Recursive slot assignment | If no subject fits a slot, moves to next slot (implicit backtracking) |
| Teacher assignment | Round-robin with `%` operator | Distributes teachers fairly across sections |
| Room matching | Best-fit linear scan | Finds smallest available room of the right type |

---

## Time Slot Structure

The schedule matches standard college format — 55-minute classes with structured breaks:

| Slot | Time | Type | Gap Before |
|---|---|---|---|
| 1 | 8:00 – 8:55 | Class | — |
| 2 | 9:00 – 9:55 | Class | 5 min break |
| 3 | 9:55 – 10:50 | Class | 5 min break |
| — | 10:50 – 11:10 | **20 min break** | after 3rd class |
| 4 | 11:10 – 12:05 | Class | — |
| — | 12:05 – 1:00 | **LUNCH** | 2 hr gap |
| 5 | 1:00 – 1:55 | Class | — |
| 6 | 1:55 – 2:50 | Class | 5 min break |
| 7 | 3:10 – 4:05 | Class | 20 min break |

---

## Teacher Assignment Logic

```
If number of sections ≤ number of teachers provided:
    → Each section gets a UNIQUE teacher
    → Section A → Teacher 1, Section B → Teacher 2, etc.

If number of sections > number of teachers provided:
    → Teachers are SHARED using round-robin
    → Example: 2 teachers, 4 sections → T1, T2, T1, T2
    → The frontend shows a warning when this happens

A teacher CANNOT teach two different sections at the same day+slot.
This is enforced by the HashSet conflict tracker.
```

---

## Room Assignment Logic

```
For each subject at each slot:
    1. Filter rooms: same type as subject (lab → lab room, class → classroom)
    2. Filter rooms: not already booked at this day+slot
    3. Pick the smallest available room that fits (best-fit strategy)
    4. If no matching type available → fall back to any free room

Room booking is tracked as: "RoomNumber|Day|SlotIndex" in a HashSet
```

---

## Prerequisites

Install these before running the project:

### 1. Java 17 or higher (JDK)
- Download: https://adoptium.net
- Verify: `java -version` (should show 17 or above)

### 2. Apache Maven 3.8+
- Download: https://maven.apache.org/download.cgi
- Windows: unzip, add `bin/` to system PATH
- Mac: `brew install maven`
- Verify: `mvn -v`

### 3. Node.js 18+ (includes npm)
- Download: https://nodejs.org (choose LTS version)
- Verify: `node -v` and `npm -v`

---

## Installation & Running

### Step 1 — Extract the project

Unzip `timetable-system.zip` to a folder on your computer.

### Step 2 — Start the Backend (Terminal 1)

Open a terminal and run:

```bash
cd timetable-system/backend
mvn spring-boot:run
```

First run downloads dependencies — this takes 1–2 minutes. Wait for:

```
Started TimetableApplication in X seconds (port 8080)
```

**Keep this terminal open.**

### Step 3 — Start the Frontend (Terminal 2)

Open a **second terminal** and run:

```bash
cd timetable-system/frontend
npm install        # only needed the first time
npm run dev
```

Wait for:

```
Local:   http://localhost:5173/
```

### Step 4 — Open in Browser

Go to: **http://localhost:5173**

The Smart Timetable System UI will load.

### Stopping the servers

Press `Ctrl + C` in each terminal to stop.

---

## Using the Application

### Setup Screen

**Sections & Days**
- Set the number of sections (1–26). They are automatically named A, B, C, D...
- Toggle which days of the week to include.

**Rooms**
- Add each room with its number (e.g. CR-1, LAB-2), capacity, and whether it is a lab room.
- Lab subjects will be assigned to lab rooms; regular subjects to classrooms.

**Subjects**
- Add each subject with: code, name, sessions per week, lab flag, and teacher names.
- Add one teacher per section for unique assignment, or fewer for shared (round-robin) assignment.
- The UI shows a warning if teachers are fewer than sections.

**Generate**
- Click **Generate Timetable** to run the algorithm.
- The result grid appears with sections as tabs, days as rows, and time slots as columns.

### Result Screen

- Switch between sections using the tab buttons at the top.
- Each cell shows: subject code, subject name, teacher name, room number.
- Lunch and break slots are highlighted separately.
- A summary panel below the grid shows sessions placed per subject.

---

## API Reference

The backend exposes two endpoints:

### POST /api/generate

Generates the timetable. Send JSON body:

```json
{
  "numSections": 2,
  "days": ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"],
  "subjects": [
    {
      "code": "TCS-401",
      "name": "Theory of Computation",
      "isLab": false,
      "hoursPerWeek": 3,
      "teachers": ["Dr. Sharma", "Dr. Verma"]
    },
    {
      "code": "TCS-402",
      "name": "Operating Systems",
      "isLab": false,
      "hoursPerWeek": 3,
      "teachers": ["Dr. Gupta"]
    }
  ],
  "rooms": [
    { "roomNumber": "CR-1", "isLab": false, "capacity": 60 },
    { "roomNumber": "LAB-1", "isLab": true, "capacity": 40 }
  ]
}
```

Response:

```json
{
  "success": true,
  "message": "Timetable generated successfully!",
  "sections": [
    {
      "sectionName": "Section A",
      "days": ["Monday", ...],
      "timeSlots": [...],
      "schedule": {
        "Monday": [
          {
            "subjectCode": "TCS-401",
            "subjectName": "Theory of Computation",
            "teacherName": "Dr. Sharma",
            "roomNumber": "CR-1",
            "isLab": false,
            "type": "class"
          },
          ...
        ]
      }
    }
  ]
}
```

### GET /api/timeslots

Returns the fixed list of time slots used by the system.

### GET /api/health

Returns `"Timetable System Running"` — use to verify the backend is up.

---

## Common Errors & Fixes

| Error | Cause | Fix |
|---|---|---|
| `mvn not found` | Maven not installed or not in PATH | Install Maven, add `bin/` to PATH, restart terminal |
| `npm not found` | Node.js not installed | Download and install from nodejs.org |
| `Port 8080 already in use` | Another app is using port 8080 | Edit `application.properties`: `server.port=9090` |
| `Cannot reach backend` in UI | Spring Boot not running | Check Terminal 1 for errors; ensure it shows "Started on port 8080" |
| `CORS error` in browser | Backend URL mismatch | Ensure backend runs on 8080 and frontend on 5173 |
| `mvn: BUILD FAILURE` | Java version mismatch | Run `java -version` — must be 17 or above |
| Timetable has many FREE slots | Not enough rooms or sessions/week too low | Add more rooms or increase sessions per week for subjects |

---

## Viva / Presentation Notes

**Q: Why no database?**
All data lives in memory for one request. The timetable is computed on the fly — no need to persist between sessions for a generation tool.

**Q: What is the algorithm?**
The system models the problem as a constraint satisfaction problem. It uses backtracking to assign time slots to subjects while checking teacher and room availability via HashSet lookups. Teachers are pre-assigned to sections using a round-robin distribution strategy.

**Q: Where is the AI?**
The system uses heuristic-based decision-making. Teacher conflict detection, room best-fit matching, and priority ordering of subjects simulate intelligent scheduling behavior without machine learning.

**Q: Why Spring Boot?**
Spring Boot handles HTTP routing, JSON serialization, and CORS configuration out of the box, letting us focus entirely on the scheduling algorithm rather than web infrastructure.

**Q: What is graph coloring doing here?**
Each time slot is a "color." Each subject is a "node." Two subjects conflict if they share a teacher, batch, or resource — an edge is drawn between them. The algorithm assigns colors (slots) such that no two connected nodes share the same color — ensuring a clash-free timetable.

---

## Author Notes

- The project uses **no external libraries** beyond Spring Boot and React — the scheduling algorithm is implemented entirely in plain Java.
- To switch to MySQL in future, add JPA dependencies to `pom.xml` and replace in-memory storage with `@Repository` classes.
- The frontend is built with Vite for fast hot-reload during development.

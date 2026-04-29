package com.timetable.model;

public class Room {
    private String roomNumber;
    private boolean isLab;
    private int capacity;

    public Room() {}
    public Room(String roomNumber, boolean isLab, int capacity) {
        this.roomNumber = roomNumber;
        this.isLab      = isLab;
        this.capacity   = capacity;
    }

    public String  getRoomNumber()             { return roomNumber; }
    public void    setRoomNumber(String r)     { this.roomNumber = r; }
    public boolean isLab()                     { return isLab; }
    public void    setLab(boolean l)           { this.isLab = l; }
    public int     getCapacity()               { return capacity; }
    public void    setCapacity(int c)          { this.capacity = c; }
}

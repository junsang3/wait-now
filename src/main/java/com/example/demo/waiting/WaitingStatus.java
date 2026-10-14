package com.example.demo.waiting;

public enum WaitingStatus {
    WAITING,
    CALLED,
    SEATED,
    CANCELED;

    public boolean isActive() {
        return this == WAITING || this == CALLED;
    }
}
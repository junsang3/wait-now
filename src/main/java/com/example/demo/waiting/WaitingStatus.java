package com.example.demo.waiting;

public enum WaitingStatus {
    WAITING("대기"),
    CALLED("호출"),
    SEATED("착석"),
    CANCELED("취소");

    private final String label;

    WaitingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean canCall() {
        return this == WAITING;
    }

    public boolean canSeat() {
        return this == CALLED;
    }

    public boolean canCancel() {
        return this == WAITING || this == CALLED;
    }
}
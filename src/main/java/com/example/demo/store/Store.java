package com.example.demo.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private int lastTicketNumber;

    private LocalDateTime openedAt;

    protected Store() {
    }

    public Store(String name) {
        this.name = name;
    }

    public boolean isOpen() {
        return openedAt != null;
    }

    public void open(LocalDateTime now) {
        if (isOpen()) {
            throw new IllegalStateException("이미 영업 중입니다");
        }
        this.lastTicketNumber = 0;
        this.openedAt = now;
    }

    public void close() {
        if (!isOpen()) {
            throw new IllegalStateException("영업 중이 아닙니다");
        }
        this.openedAt = null;
    }

    public int issue() {
        if (!isOpen()) {
            throw new IllegalStateException("영업 중이 아닙니다");
        }
        return ++lastTicketNumber;
    }
}
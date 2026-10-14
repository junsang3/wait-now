package com.example.demo.waiting;

import com.example.demo.store.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, unique = true, length = 36)
    private String ticketToken;

    @Column(nullable = false)
    private int ticketNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 255)
    private WaitingStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime calledAt;

    private LocalDateTime finishedAt;

    protected Waiting() {
    }
}
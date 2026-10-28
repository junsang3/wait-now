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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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

    @Column(nullable = false)
    private int ticketNumber;

    @Column(nullable = false, unique = true, length = 36)
    private String ticketToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 255)
    private WaitingStatus status;

    @Column(nullable = false)
    private int partySize;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime calledAt;

    private LocalDateTime finishedAt;

    protected Waiting() {
    }

    public Waiting(Store store, int ticketNumber, int partySize, String phoneNumber, LocalDateTime now) {
        this.store = store;
        this.ticketNumber = ticketNumber;
        this.ticketToken = UUID.randomUUID().toString();
        this.status = WaitingStatus.WAITING;
        this.partySize = partySize;
        this.phoneNumber = phoneNumber;
        this.createdAt = now;
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public String getTicketToken() {
        return ticketToken;
    }

    public int getPartySize() {
        return partySize;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public WaitingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void call(LocalDateTime now) {
        if (status != WaitingStatus.WAITING) {
            throw new IllegalStateException("대기 중인 손님만 호출할 수 있습니다");
        }
        this.status = WaitingStatus.CALLED;
        this.calledAt = now;
    }

    public void seat(LocalDateTime now) {
        if (status != WaitingStatus.CALLED) {
            throw new IllegalStateException("호출된 손님만 착석 처리할 수 있습니다");
        }
        this.status = WaitingStatus.SEATED;
        this.finishedAt = now;
    }

    public void cancel(LocalDateTime now) {
        if (!status.isActive()) {
            throw new IllegalStateException("이미 종료된 대기입니다");
        }
        this.status = WaitingStatus.CANCELED;
        this.finishedAt = now;
    }
}
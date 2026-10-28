package com.example.demo.waiting;

import com.example.demo.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    Optional<Waiting> findByTicketToken(String ticketToken);
    long countByStore_IdAndStatusIn(Long storeId, List<WaitingStatus> statuses);
    long countByStore_IdAndStatusAndIdLessThan(Long storeId, WaitingStatus status, Long waitingId);
}

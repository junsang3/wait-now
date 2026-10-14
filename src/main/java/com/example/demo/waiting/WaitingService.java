package com.example.demo.waiting;

import com.example.demo.store.Store;
import com.example.demo.store.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final StoreRepository storeRepository;

    public WaitingService(WaitingRepository waitingRepository, StoreRepository storeRepository) {
        this.waitingRepository = waitingRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional
    public void issue(Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow();
        int ticketNumber = store.issue();
        Waiting waiting = waitingRepository.save(new Waiting(store, ticketNumber, LocalDateTime.now()));
    }

}

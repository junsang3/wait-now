package com.example.demo.waiting;

import com.example.demo.store.Store;
import com.example.demo.store.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final StoreRepository storeRepository;

    public WaitingService(WaitingRepository waitingRepository, StoreRepository storeRepository) {
        this.waitingRepository = waitingRepository;
        this.storeRepository = storeRepository;
    }

    public Waiting findByTicketToken(String ticketToken) {
        return waitingRepository.findByTicketToken(ticketToken).orElseThrow();
    }

    public long getWaitingCount(Long storeId) {
        return waitingRepository.countByStore_IdAndStatusIn(storeId, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED));
    }

    public long getPosition(Long storeId, Long waitingId) {
        long aheadCount = waitingRepository.countByStore_IdAndStatusAndIdLessThan(storeId, WaitingStatus.WAITING, waitingId);
        return aheadCount + 1;
    }

    @Transactional
    public String issue(Long storeId, int partySize, String phoneNumber) {
        Store store = storeRepository.findById(storeId).orElseThrow();
        int ticketNumber = store.issue();
        Waiting waiting = waitingRepository.save(new Waiting(store, ticketNumber, partySize, phoneNumber,  LocalDateTime.now()));
        return waiting.getTicketToken();
    }

}

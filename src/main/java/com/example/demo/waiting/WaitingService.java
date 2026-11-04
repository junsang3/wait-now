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

    public List<Waiting> getWaitings(Long storeId, List<WaitingStatus> statuses) {
        return waitingRepository.findByStore_IdAndStatusIn(storeId, statuses);
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

    @Transactional
    public Waiting call(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow();
        waiting.call(LocalDateTime.now());
        return waiting;
    }

    @Transactional
    public Waiting seat(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow();
        waiting.seat(LocalDateTime.now());
        return waiting;
    }

    @Transactional
    public Waiting cancel(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow();
        waiting.cancel(LocalDateTime.now());
        return waiting;
    }

    @Transactional
    public Waiting cancel(String ticketToken) {
        Waiting waiting = waitingRepository.findByTicketToken(ticketToken).orElseThrow();
        waiting.cancel(LocalDateTime.now());
        return waiting;
    }

    @Transactional
    public void openStore(Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow();
        store.open(LocalDateTime.now());
    }

    @Transactional
    public void closeStore(Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow();
        store.close();
        List<Waiting> waitings = waitingRepository.findByStore_IdAndStatusIn(
                storeId, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED)
        );
        for (Waiting waiting : waitings) {
            waiting.cancel(LocalDateTime.now());
        }
    }

}

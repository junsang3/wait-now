package com.example.demo.store;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class StoreService {
    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public Store findById(Long storeId) {
        return storeRepository.findById(storeId).orElseThrow();
    }

    public void create(String name) {
        storeRepository.save(new Store(name));
    }

    @Transactional
    public Store open(Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow();
        store.open(LocalDateTime.now());
        return store;
    }

    @Transactional
    public Store close(Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow();
        store.close();
        return store;
    }
}

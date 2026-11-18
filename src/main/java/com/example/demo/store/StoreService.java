package com.example.demo.store;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoreService {
    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public Store findById(Long storeId) {
        return storeRepository.findById(storeId).orElseThrow();
    }

    public List<Store> findAll() {
        return storeRepository.findAll();
    }
}

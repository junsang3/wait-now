package com.example.demo.store;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * 매장 행에 배타 락(SELECT ... FOR UPDATE)을 걸고 조회한다.
     * 번호표 발급, 접수 시작·종료처럼 lastTicketNumber·openedAt 을 바꾸는 작업은
     * 이 메서드로 조회해 같은 매장에 대한 변경을 순서대로 처리한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Store s where s.id = :id")
    Optional<Store> findByIdForUpdate(@Param("id") Long id);
}

package com.example.demo.waiting;

import com.example.demo.store.Store;
import com.example.demo.store.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 여러 손님이 동시에 웨이팅을 등록할 때 번호표가 중복 발급되지 않는지 검증한다.
 * 트랜잭션이 각 스레드에서 따로 커밋되어야 하므로 클래스에 @Transactional 을 붙이지 않는다.
 */
@SpringBootTest
@Testcontainers
class TicketIssueConcurrencyTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:9");

    @Autowired
    WaitingService waitingService;

    @Autowired
    WaitingRepository waitingRepository;

    @Autowired
    StoreRepository storeRepository;

    @Test
    void 동시에_웨이팅을_등록해도_번호표가_중복되지_않는다() throws Exception {
        // given: 영업 중인 매장
        Long storeId = storeRepository.save(new Store("동시성 테스트 매장")).getId();
        waitingService.openStore(storeId);

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        // when: 50명이 동시에 등록
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await(); // 모든 스레드가 준비될 때까지 대기
                return waitingService.issue(storeId, 2, "010-0000-0000");
            }));
        }
        ready.await();
        start.countDown(); // 동시에 출발

        List<String> tokens = new ArrayList<>();
        try {
            for (Future<String> future : futures) {
                tokens.add(future.get(30, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }

        // then
        List<Integer> ticketNumbers = tokens.stream()
                .map(token -> waitingRepository.findByTicketToken(token).orElseThrow().getTicketNumber())
                .toList();

        assertThat(ticketNumbers).doesNotHaveDuplicates();
        assertThat(ticketNumbers).containsExactlyInAnyOrderElementsOf(
                IntStream.rangeClosed(1, threadCount).boxed().toList());
    }
}

package com.example.demo.waiting;

import com.example.demo.store.Store;
import com.example.demo.store.StoreRepository;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final StoreRepository storeRepository;
    private final PushService pushService;

    public WaitingService(
            WaitingRepository waitingRepository,
            StoreRepository storeRepository,
            @Value("${vapid.public-key}") String publicKey,
            @Value("${vapid.private-key}") String privateKey,
            @Value("${vapid.subject}") String subject
            ) throws GeneralSecurityException {
        this.waitingRepository = waitingRepository;
        this.storeRepository = storeRepository;
        Security.addProvider(new BouncyCastleProvider());
        this.pushService = new PushService(publicKey, privateKey, subject);
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
//        Store store = storeRepository.findById(storeId).orElseThrow();
        Store store = storeRepository.findByIdForUpdate(storeId).orElseThrow();
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
        Store store = storeRepository.findByIdForUpdate(storeId).orElseThrow();
        store.open(LocalDateTime.now());
    }

    @Transactional
    public void closeStore(Long storeId) {
        Store store = storeRepository.findByIdForUpdate(storeId).orElseThrow();
        store.close();
        List<Waiting> waitings = waitingRepository.findByStore_IdAndStatusIn(
                storeId, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED)
        );
        for (Waiting waiting : waitings) {
            waiting.cancel(LocalDateTime.now());
        }
    }

    @Transactional
    public void subscribePush(String ticketToken, String endpoint, String p256dh, String auth) {
        Waiting waiting = waitingRepository.findByTicketToken(ticketToken).orElseThrow();
        if (waiting.getStatus() != WaitingStatus.WAITING) {
            throw new IllegalStateException("대기중인 웨이팅이 아닙니다");
        }
        waiting.setPush(endpoint, p256dh, auth);
    }

    public void sendPush(Waiting waiting) {
        if (waiting.getPushEndpoint() == null) return;
        try {
            String payload = """
            {"title":"입장 안내","body":"%d번 손님, 입장해주세요","url":"/waitings/%s"}
            """.formatted(waiting.getTicketNumber(), waiting.getTicketToken());
            Subscription sub = new Subscription(waiting.getPushEndpoint(),
                    new Subscription.Keys(waiting.getPushP256dh(), waiting.getPushAuth()));
            HttpResponse res = pushService.send(new Notification(sub, payload), Encoding.AES128GCM);
            System.out.println("push status=" + res.getStatusLine().getStatusCode()
                    + " body=" + EntityUtils.toString(res.getEntity()));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

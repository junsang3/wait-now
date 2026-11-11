package com.example.demo.waiting;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WaitingSseEmitters {
    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long storeId) {
        SseEmitter emitter = new SseEmitter();
        Set<SseEmitter> storeEmitters = emitters.computeIfAbsent(storeId, id -> ConcurrentHashMap.newKeySet());
        storeEmitters.add(emitter);

        Runnable remove = () -> storeEmitters.remove(emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());

        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            remove.run();
        }

        return emitter;
    }

    public void send(Long storeId) {
        Set<SseEmitter> storeEmitters = emitters.get(storeId);
        if (storeEmitters == null) return;

        for (SseEmitter emitter: storeEmitters) {
            try {
                emitter.send(SseEmitter.event().name("waiting-changed").data("refresh"));
            } catch (IOException e) {
                storeEmitters.remove(emitter);
            }
        }
    }
}

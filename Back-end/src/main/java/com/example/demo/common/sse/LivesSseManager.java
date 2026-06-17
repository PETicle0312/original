package com.example.demo.common.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Component
public class LivesSseManager {
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(e -> remove(userId, emitter));

        try { emitter.send(SseEmitter.event().name("connected").data("ok")); } catch (IOException ignored) {}
        return emitter;
    }

    public void publishLives(String userId, Object payload) {
        var list = emitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
        var dead = new ArrayList<SseEmitter>();
        for (SseEmitter s : list) {
            try { s.send(SseEmitter.event().name("lives").data(payload)); }
            catch (Exception e) { dead.add(s); }
        }
        list.removeAll(dead);
    }

    public void publishPoints(String userId, Object payload) {
        var list = emitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
        var dead = new ArrayList<SseEmitter>();
        for (SseEmitter s : list) {
            try {
                s.send(SseEmitter.event().name("points").data(payload));
                s.send(payload);
                s.send(SseEmitter.event().name("points").data(payload));
            } catch (Exception e) {
                dead.add(s);
            }
        }
        list.removeAll(dead);
    }

    private void remove(String userId, SseEmitter emitter) {
        var list = emitters.get(userId);
        if (list != null) list.remove(emitter);
    }
}

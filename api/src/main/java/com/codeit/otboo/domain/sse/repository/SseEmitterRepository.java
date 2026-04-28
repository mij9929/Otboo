package com.codeit.otboo.domain.sse.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class SseEmitterRepository {
    private final ConcurrentMap<UUID, Set<SseEmitter>> emittersByReceiverId = new ConcurrentHashMap<>();
    private final ConcurrentMap<SseEmitter, UUID> receiverIdByEmitter = new ConcurrentHashMap<>();

    public SseEmitter save(UUID receiverId, SseEmitter emitter) {
        emittersByReceiverId
                .computeIfAbsent(receiverId, key -> ConcurrentHashMap.newKeySet())
                .add(emitter);

        receiverIdByEmitter.put(emitter, receiverId);
        return emitter;
    }

    public List<SseEmitter> findAllByReceiverIdsIn(Collection<UUID> receiverIds) {
        return receiverIds.stream()
                .map(emittersByReceiverId::get)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .toList();
    }

    public List<SseEmitter> findAll () {
        return emittersByReceiverId.values().stream()
                .flatMap(Collection::stream)
                .toList();
    }

    public void delete(SseEmitter emitter) {
        UUID receiverId = receiverIdByEmitter.remove(emitter);

        if (receiverId == null) {
            return;
        }

        removeFromReceiverMap(receiverId, emitter);
    }

    private void removeFromReceiverMap(UUID receiverId, SseEmitter emitter) {
        emittersByReceiverId.computeIfPresent(receiverId, (key, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }
}

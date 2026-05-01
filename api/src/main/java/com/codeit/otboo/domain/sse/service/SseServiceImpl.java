package com.codeit.otboo.domain.sse.service;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.sse.object.SseMessage;
import com.codeit.otboo.domain.sse.repository.SseEmitterRepository;
import com.codeit.otboo.domain.sse.repository.SseMessageRepository;

import java.io.IOException;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    @Value("${sse.timeout}")
    private long timeout;

    private final SseEmitterRepository sseEmitterRepository;
    private final SseMessageRepository sseMessageRepository;

    @Override
    public SseEmitter connect(UUID receiverId, UUID lastEventId) {
        SseEmitter sseEmitter = new SseEmitter(timeout);

        sseEmitter.onCompletion(() -> {
            log.debug("sse on onCompletion. receiverId = {}", receiverId);
            sseEmitterRepository.delete(sseEmitter);
        });
        sseEmitter.onTimeout(() -> {
            log.debug("sse on onTimeout. receiverId = {}", receiverId);
            sseEmitterRepository.delete(sseEmitter);
            sseEmitter.complete();
        });
        sseEmitter.onError((ex) -> {
            log.debug("sse on onError. receiverId = {}", receiverId, ex);
            sseEmitterRepository.delete(sseEmitter);
            sseEmitter.complete();
        });

        sseEmitterRepository.save(receiverId, sseEmitter);

        Optional.ofNullable(lastEventId)
                .ifPresentOrElse(
                        id -> {
                            SseMessageRepository.SseReplayResult replayResult =
                                    sseMessageRepository.findReplayMessages(id, receiverId);

                            if(!replayResult.lastEventFound()) {
                                sendSyncRequiredEvent(sseEmitter);
                                return;
                            }

                            replayResult.messages().forEach(sseMessage ->
                                    sendToEmitter(sseEmitter, sseMessage.toEvent())
                            );

                            ping(sseEmitter);
                        },
                        () -> ping(sseEmitter)
                );

        return sseEmitter;
    }

    private void sendSyncRequiredEvent(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("sync-required")
                    .data("SSE event cache missed. Please refetch notifications.")
                    .build());
        } catch (IOException e) {
            log.debug("SSE sync-required event send failed.", e);
            sseEmitterRepository.delete(emitter);
            emitter.complete();
        }
    }

    @Override
    public void send(Collection<UUID> receiverIds, String eventName, NotificationDto data) {
        SseMessage message = sseMessageRepository.save(
                SseMessage.create(receiverIds, eventName, data)
        );

        Set<DataWithMediaType> event = message.toEvent();

        sseEmitterRepository.findAllByReceiverIdsIn(receiverIds)
                .forEach(sseEmitter -> sendToEmitter(sseEmitter, event));
    }

    @Override
    public void broadcast(String eventName, NotificationDto data) {
        SseMessage message = sseMessageRepository.save(SseMessage.createBroadcast(eventName, data));

        Set<DataWithMediaType> event = message.toEvent();

        sseEmitterRepository.findAll()
                .forEach(sseEmitter -> sendToEmitter(sseEmitter, event));

    }

    private void sendToEmitter(SseEmitter emitter, Set<DataWithMediaType> event) {
        try {
            emitter.send(event);
        } catch (IOException e) {
            log.warn("SSE send failed. emitter={}", emitter, e);
            sseEmitterRepository.delete(emitter);
            emitter.completeWithError(e);
        }
    }

    @Override
    @Scheduled(fixedDelay = 1000 * 30)
    public void cleanUp() {
        sseEmitterRepository.findAll()
                .stream()
                .filter(sseEmitter -> !ping(sseEmitter))
                .forEach(sseEmitter -> {
                    sseEmitterRepository.delete(sseEmitter);
                    sseEmitter.complete();
                });
    }

    private boolean ping(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("ping")
                    .data("alive")
                    .build());
            return true;
        } catch (IOException e) {
            log.debug("SSE Ping Failed, Connection closed emitter = {}", emitter);
            return false;
        }
    }
}

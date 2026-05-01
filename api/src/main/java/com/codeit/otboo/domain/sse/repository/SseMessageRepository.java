package com.codeit.otboo.domain.sse.repository;

import com.codeit.otboo.domain.sse.object.SseMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SseMessageRepository {

    private static final String SSE_EVENTS_KEY_PREFIX = "sse:events:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${sse.event-cache-capacity:100}")
    private long eventCacheCapacity;

    @Value("${sse.event-cache-ttl-seconds:600}")
    private long eventCacheTtlSeconds;

    public record SseReplayResult(
            boolean lastEventFound,
            List<SseMessage> messages
    ) {
    }

    public SseMessage save(SseMessage message) {
        if (message.isBroadcast()) {
            log.debug("Broadcast SSE message is not cached per receiver. eventId={}", message.getEventId());
            return message;
        }

        String payload = serialize(message);

        message.getReceiverIds().forEach(receiverId -> {
            String key = key(receiverId);

            redisTemplate.opsForList().rightPush(key, payload);
            redisTemplate.opsForList().trim(key, -eventCacheCapacity, -1);
            redisTemplate.expire(key, Duration.ofSeconds(eventCacheTtlSeconds));
        });

        return message;
    }

    public SseReplayResult findReplayMessages(UUID eventId, UUID receiverId) {
        String key = key(receiverId);

        List<String> payloads = redisTemplate.opsForList().range(key, 0, -1);

        if (payloads == null || payloads.isEmpty()) {
            return new SseReplayResult(false, List.of());
        }

        List<SseMessage> messages = payloads.stream()
                .map(this::deserialize)
                .filter(Objects::nonNull)
                .toList();

        int index = findIndex(messages, eventId);

        if (index == -1) {
            return new SseReplayResult(false, List.of());
        }

        return new SseReplayResult(
                true,
                messages.subList(index + 1, messages.size())
        );
    }


    private int findIndex(List<SseMessage> messages, UUID eventId) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getEventId().equals(eventId)) {
                return i;
            }
        }

        return -1;
    }

    private String key(UUID receiverId) {
        return SSE_EVENTS_KEY_PREFIX + receiverId;
    }

    private String serialize(SseMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("SSE message serialize failed. eventId={}", message.getEventId(), e);
            throw new IllegalStateException(e);
        }
    }

    private SseMessage deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, SseMessage.class);
        } catch (JsonProcessingException e) {
            log.warn("SSE message deserialize failed. payload={}", payload, e);
            return null;
        }
    }
}
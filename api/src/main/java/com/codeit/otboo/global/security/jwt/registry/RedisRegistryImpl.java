package com.codeit.otboo.global.security.jwt.registry;

import com.codeit.otboo.global.security.jwt.exception.JwtInvalidRefreshTokenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisRegistryImpl implements RedisRegistry {

    private static final String LOGIN_STATE_KEY_PREFIX = "auth:login:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final DefaultRedisScript<Long> rotateScript = createRotateScript();

    private DefaultRedisScript<Long> createRotateScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText("""
        local value = redis.call('get', KEYS[1])
        if not value then
            return 0
        end
        
        local data = cjson.decode(value)
        
        if data.refreshToken ~= ARGV[1] then
            return 0
        end
        
        data.refreshToken = ARGV[2]
        
        redis.call('SET', KEYS[1], cjson.encode(data), 'EX', ARGV[3])
        
        return 1
        """);
        return script;
    }

    @Override
    public void save(UUID userId, String sessionId, String refreshToken, long ttlSeconds) {
        UserInfo userInfo = new UserInfo(sessionId, refreshToken);
        redisTemplate.opsForValue().set(
                key(userId),
                serialize(userInfo),
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public boolean isValidSession(UUID userId, String sessionId) {
        UserInfo userInfo = get(userId);
        return userInfo != null
                && sessionId != null
                && sessionId.equals(userInfo.sessionId());
    }

    @Override
    public boolean isValidRefreshToken(UUID userId, String refreshToken) {
        UserInfo userInfo = get(userId);
        return userInfo != null
                && refreshToken != null
                && refreshToken.equals(userInfo.refreshToken());
    }

    @Override
    public void rotateRefreshToken(UUID userId, String oldRefreshToken, String newRefreshToken, long ttlSeconds) {

        Long result = redisTemplate.execute(
                rotateScript,
                List.of(key(userId)),
                oldRefreshToken,
                newRefreshToken,
                String.valueOf(ttlSeconds)
        );

        if (result != 1L) {
            throw new JwtInvalidRefreshTokenException(oldRefreshToken);
        }
    }

    @Override
    public void delete(UUID userId) {
        redisTemplate.delete(key(userId));
    }

    @Override
    public UserInfo get(UUID userId) {
        String value = redisTemplate.opsForValue().get(key(userId));
        if (value == null) {
            return null;
        }
        return deserialize(value);
    }

    private String key(UUID userId) {
        return LOGIN_STATE_KEY_PREFIX + userId;
    }

    private String serialize(UserInfo loginState) {
        try {
            return objectMapper.writeValueAsString(loginState);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("로그인 상태 직렬화에 실패했습니다.", e);
        }
    }

    private UserInfo deserialize(String value) {
        try {
            return objectMapper.readValue(value, UserInfo.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("로그인 상태 역직렬화에 실패했습니다.", e);
        }
    }
}
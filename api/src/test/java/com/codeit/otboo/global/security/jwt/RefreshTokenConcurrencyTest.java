package com.codeit.otboo.global.security.jwt;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.codeit.otboo.domain.feed.elasticsearch.repository.FeedDocumentRepository;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.user.service.AuthService;
import com.codeit.otboo.domain.user.service.AuthServiceImpl;
import com.codeit.otboo.global.security.jwt.dto.JwtInformation;
import com.codeit.otboo.global.security.jwt.exception.JwtInvalidRefreshTokenException;
import com.codeit.otboo.global.security.jwt.registry.RedisRegistry;
import com.codeit.otboo.global.security.jwt.registry.RedisRegistryImpl;
import com.codeit.otboo.global.security.jwt.registry.UserInfo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RefreshTokenConcurrencyTest {

    @Autowired
    AuthService authService;
    @Autowired
    RedisRegistry redisRegistry;
    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtProperties jwtProperties;

    @MockitoBean
    ElasticsearchOperations elasticsearchOperations;
    @MockitoBean
    ElasticsearchClient elasticsearchClient;
    @MockitoBean
    FeedDocumentRepository feedDocumentRepository;

    private UUID userId;

    @Autowired
    EntityManager em;

    @Autowired
    StringRedisTemplate redisTemplate;

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushDb();
    }

    @BeforeEach
    void clear() {
        em.clear();
    }

    @RepeatedTest(10)
    @DisplayName("동시에 refresh 요청하면 하나만 성공해야 한다.")
    void concurrent_refresh_token_then_one_success() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@test.com";


        User user = User.builder()
                .email(email)
                .password("password")
                .build();
        Profile profile = Profile.builder()
                .user(user)
                .name("테스트유저")
                .build();

        user.setProfile(profile);

        userRepository.saveAndFlush(user);

        userId = user.getId();
        em.clear();

        String sessionId = UUID.randomUUID().toString();
        String refreshToken = jwtProvider.generateRefreshToken(userId, email, sessionId);

        redisRegistry.save(
                userId,
                sessionId,
                refreshToken,
                jwtProperties.refreshTokenExpiration()
        );

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        List<JwtInformation> successResults = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        Runnable task = () -> {
            ready.countDown();

            try {
                start.await();
                successResults.add(authService.refreshToken(refreshToken));
            } catch (Throwable e) {
                failures.add(e);
            } finally {
                done.countDown();
            }
        };

        try {
            executorService.submit(task);
            executorService.submit(task);

            ready.await();
            start.countDown();
            done.await();

            System.out.println("success size = " + successResults.size());
            System.out.println("failure size = " + failures.size());

            System.out.println("old refresh = " + refreshToken);

            successResults.forEach(result -> {
                System.out.println("new refresh = " + result.refreshToken());
                System.out.println("old == new ? " + refreshToken.equals(result.refreshToken()));
            });

            failures.forEach(e -> {
                System.out.println(e.getClass().getName());
                System.out.println(e.getMessage());
            });


            assertThat(successResults).hasSize(1);
            assertThat(failures).hasSize(1);
            assertThat(failures.get(0))
                    .isInstanceOf(JwtInvalidRefreshTokenException.class);

            UserInfo finalState = redisRegistry.get(userId);
            assertThat(finalState).isNotNull();
            assertThat(finalState.refreshToken())
                    .isEqualTo(successResults.get(0).refreshToken());
        } finally {
            redisRegistry.delete(userId);
            executorService.shutdownNow();
        }
    }

    @AfterEach
    void tearDown() {
        if (userId != null) {
            redisRegistry.delete(userId);
        }
    }
}
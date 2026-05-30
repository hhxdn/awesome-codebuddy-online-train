package com.onlinetrain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OAuth状态临时存储器（内存）
 * 用于微信OAuth授权流程中的state参数管理
 */
@Component
public class OAuthStateStore {

    @Data
    @AllArgsConstructor
    public static class OAuthState {
        private Long userId;
        private String redirectUri;
        private Long createdAt;
    }

    private final Map<String, OAuthState> store = new ConcurrentHashMap<>();

    /**
     * 存储OAuth状态，返回state token
     */
    public String put(Long userId, String redirectUri) {
        // 清理超过5分钟的过期条目
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> now - e.getValue().getCreatedAt() > 5 * 60 * 1000);

        String state = UUID.randomUUID().toString().replace("-", "");
        store.put(state, new OAuthState(userId, redirectUri, now));
        return state;
    }

    /**
     * 消费OAuth状态（取出并删除）
     */
    public OAuthState consume(String state) {
        return store.remove(state);
    }
}

package com.galgame.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WebSocket 核心处理器。
 * Phase 1 只实现握手 + 心跳 + 消息路由骨架。
 * 后续 Phase 逐步填充具体业务消息处理。
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GameWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public GameWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket connected: {}", sessionId);

        // 发送握手确认
        sendMessage(session, "CONNECTED", Map.of("sessionId", sessionId, "message", "连接成功"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received: {}", payload);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            String type = (String) msg.get("type");

            switch (type) {
                case "PING":
                    // 心跳响应
                    sendMessage(session, "PONG", Map.of("timestamp", System.currentTimeMillis()));
                    break;

                case "LAUNCH_GAME":
                    // Phase 2+ 实现
                    sendMessage(session, "ERROR",
                            Map.of("code", "NOT_IMPLEMENTED", "message", "启动游戏功能将在 Phase 2 实现"));
                    break;

                default:
                    sendMessage(session, "ERROR",
                            Map.of("code", "UNKNOWN_TYPE", "message", "未知消息类型: " + type));
            }
        } catch (Exception e) {
            log.error("Failed to parse message: {}", payload, e);
            sendMessage(session, "ERROR", Map.of("code", "PARSE_ERROR", "message", "消息格式错误"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket disconnected: {}, status: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: {}", session.getId(), exception);
        sessions.remove(session.getId());
    }

    /**
     * 向指定 session 发送 JSON 消息
     */
    public void sendMessage(WebSocketSession session, String type, Object data) {
        try {
            Map<String, Object> msg = Map.of("type", type, "data", data);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        } catch (IOException e) {
            log.error("Failed to send message to {}", session.getId(), e);
        }
    }

    /**
     * 广播消息给所有连接的客户端（用于推送通知等）
     */
    public void broadcast(String type, Object data) {
        sessions.values().forEach(s -> sendMessage(s, type, data));
    }
}

package com.santosh.jobtracker.realtime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WebSocketPublisher implements RealtimePublisher {

    private final SimpMessagingTemplate template;

    public WebSocketPublisher(SimpMessagingTemplate template) {
        this.template = template;
    }

    @Override
    public void publishUserEvent(String userId, String type, Object payload) {
        template.convertAndSend("/topic/users/" + userId, Map.of(
                "type", type,
                "payload", payload
        ));
    }
}

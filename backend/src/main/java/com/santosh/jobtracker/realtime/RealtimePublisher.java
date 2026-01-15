package com.santosh.jobtracker.realtime;

public interface RealtimePublisher {
    void publishUserEvent(String userId, String type, Object payload);
}

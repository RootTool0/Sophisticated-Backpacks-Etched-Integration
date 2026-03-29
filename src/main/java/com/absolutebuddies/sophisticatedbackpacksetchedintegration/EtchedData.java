package com.absolutebuddies.sophisticatedbackpacksetchedintegration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EtchedData
{
    public static final Map<UUID, EtchedStreamInfo> ACTIVE_STREAMS_CACHE = new ConcurrentHashMap<>();
    public static final Map<String, Integer> AUDIO_DURATION_CACHE = new ConcurrentHashMap<>();
}

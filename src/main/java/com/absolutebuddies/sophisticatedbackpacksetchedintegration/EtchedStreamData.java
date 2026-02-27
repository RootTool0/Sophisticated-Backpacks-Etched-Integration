package com.absolutebuddies.sophisticatedbackpacksetchedintegration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EtchedStreamData
{
    public static final Map<UUID, EtchedStreamInfo> ACTIVE_STREAMS = new ConcurrentHashMap<>();
}

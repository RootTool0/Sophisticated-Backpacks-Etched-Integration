package com.absolutebuddies.sophisticatedbackpacksetchedintegration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SophisticatedBackpacksEtchedIntegrationDataBase
{
    public static final Map<UUID, EtchedStreamInfo> ACTIVE_STREAMS_CACHE = new ConcurrentHashMap<>();
    public static int DISC_DURATION = 0;
}

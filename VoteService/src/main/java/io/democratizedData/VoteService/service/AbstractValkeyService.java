package io.democratizedData.VoteService.service;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.SnappyCodecV2;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public abstract class AbstractValkeyService {
    private static volatile RedissonClient client;
    protected final String entityName;

    protected AbstractValkeyService(@Value("entity_name") String entityName) {
        this.entityName = entityName;
    }

    public <AI> void syncAll(String mapKey, String entityName, Map<String, AI> allObjects) {
        new LockingUpdater(mapKey, entityName) {
            @Override
            public void updateProcess() {
                RMap<String, AI> map = getClient().getMap(mapKey);
                map.clear();
                map.putAll(allObjects);
            }
        }.apply();
    }

    public <AI> void save(String mapKey, AI object, String code) {
        new LockingUpdater(mapKey, entityName) {
            @Override
            public void updateProcess() {
                RMap<String, AI> map = getClient().getMap(mapKey);
                map.put(code, object);
            }
        }.apply();
    }

    protected <AI> Map<String, AI> get(String mapKey) {
        RMap<String, AI> map = getClient().getMap(mapKey);
        return map.readAllMap();
    }

    protected <AI> RMap<String, AI> getRMap(String mapKey) {
        return getClient().getMap(mapKey);
    }

    protected <AI> List<AI> getValues(String mapKey) {
        RMap<String, AI> map = getClient().getMap(mapKey);
        return new ArrayList<>(map.readAllValues());
    }

    protected <AI> AI get(String mapKey, String code) {
        RMap<String, AI> map = getClient().getMap(mapKey);
        return map.get(code);
    }

    protected <AI> Map<String, AI> getByKeys(String mapKey, Set<String> codes) {
        RMap<String, AI> map = getClient().getMap(mapKey);
        return map.getAll(codes);
    }

    public static RedissonClient getClient() {
        if (client == null) {
            synchronized (AbstractValkeyService.class) {
                if (client == null) {
                    client = getRedissonClient();
                }
            }
        }
        return client;
    }

    public static RedissonClient getRedissonClient() {
        Config config = new Config();
        config.useMasterSlaveServers()
                .setReadMode(ReadMode.MASTER_SLAVE)
                .setMasterAddress("valkey://127.0.0.1:6379")
                .setSlaveAddresses(Collections.singleton("valkey://127.0.0.1:6380"));

        config.setCodec(new SnappyCodecV2(new JsonJacksonCodec()));

        return Redisson.create(config);
    }

    public static String getBucketName(String identifier, String entityName) {
        return "{" + identifier + ":" + entityName + "}";
    }

    public abstract static class LockingUpdater {
        private final String entityName;
        private final String mapKey;

        protected LockingUpdater(String mapKey, String entityName) {
            this.entityName = entityName;
            this.mapKey = mapKey;
        }

        public abstract void updateProcess();

        public void apply() {
            apply("{lock:" + getBucketName(mapKey, entityName) + "}");
        }

        public void apply(String lockName) {
            int retryCount = 10;
            do {
                RLock lock = getClient().getLock(lockName);
                try {
                    if (lock.tryLock(10, TimeUnit.MINUTES)) {
                        retryCount = 0;
                        try {
                            updateProcess();
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        retryCount--;
                        try {
                            Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                        } catch (InterruptedException ignored) {
                        }
                    }
                } catch (InterruptedException ignored) {
                    retryCount--;
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                    } catch (InterruptedException ignored2) {
                    }
                }
            } while (retryCount > 0);
        }
    }
}

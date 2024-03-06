package org.apache.eventmesh.dashboard.console.function.client.create;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.eventmesh.dashboard.console.function.client.ClientCreateOperation;

public class RedisClientCreateOperation implements ClientCreateOperation<StatefulRedisConnection<String, String> > {
    @Override
    public StatefulRedisConnection<String, String> createClient() {
        return null;
    }

    @Override
    public void close( StatefulRedisConnection<String, String>  client) {
        client.close();
    }
}

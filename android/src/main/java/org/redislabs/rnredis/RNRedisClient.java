package org.redislabs.rnredis;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.redisson.Redisson;

import org.redisson.api.RBucket;
import org.redisson.api.RBuckets;
import org.redisson.api.RKeys;
import org.redisson.api.RPatternTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.PatternMessageListener;

import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.net.URL;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


class RNRedisClient {

    private RedissonClient       _client    = null;
    private Map<String, Integer> _listeners = new HashMap<>();

    RNRedisClient(final String jsonConfig) throws Exception {
        Config config = Config.fromJSON(jsonConfig);
        config.setCodec(new StringCodec());
        _client = Redisson.create(config);
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }

    private RedissonClient client() throws Exception {
        if (_client == null) {
            throw new ReactException("ENINIT", "Redis client not initialized");
        }
        return _client;
    }

    WritableArray address() throws Exception {
        WritableArray addressList = Arguments.createArray();

        Config config = client().getConfig();
        if (config.isClusterConfig()) {
            List<URL> urls = config.useClusterServers().getNodeAddresses();
            for (Object url : urls) {
                addressList.pushString(url.toString());
            }
        } else {
            URL uri = config.useSingleServer().getAddress();
            addressList.pushString(uri.toString());
        }

        return addressList;
    }

    Integer dbIndex() throws Exception {
        Integer dbId = (-1);

        Config config = client().getConfig();
        if (!config.isClusterConfig()) {
            dbId = config.useSingleServer().getDatabase();
        }

        return dbId;
    }

    Boolean destroy() throws Exception {
        if (_client != null) {
            Iterator it = _listeners.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                RPatternTopic<String> topic = client().getPatternTopic((String)pair.getKey());
                if (topic != null) {
                    topic.removeListener((Integer)pair.getValue());
                }
                it.remove();
            }
            return true;
        }

        _client = null;
        return false;
    }

    long getObjectCount() throws Exception {
        RKeys keys = client().getKeys();
        if (keys != null) {
            return keys.count();
        }
        return (0);
    }

    WritableMap getObjects(ReactTask task) throws Exception {
        return this.getObjects(task, (-1), (-1));
    }

    WritableMap getObjects(ReactTask task, int firstIndex, int lastIndex) throws Exception {
        WritableMap dbDump = Arguments.createMap();

        RBuckets buckets = client().getBuckets();
        List list = buckets.find("*");

        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object lho, Object rho) {
                RBucket left  = (RBucket)lho;
                RBucket right = (RBucket)rho;
                return left.getName().compareTo(right.getName());
            }
        });

        if (firstIndex >= list.size()) firstIndex = list.size() - 1;
        if (lastIndex  >  list.size()) lastIndex  = list.size();

        if (firstIndex < 0) firstIndex = 0;
        if (lastIndex  < 0) lastIndex  = list.size();

        List sublist = list.subList(firstIndex, lastIndex);

        for (Object item : sublist) {
            if (task.isCancelled()) return dbDump;

            RBucket bucket = (RBucket)item;
            dbDump.putString(bucket.getName(), (String)bucket.get());
        }

        return dbDump;
    }

    <T> Object getBucket(final String key) throws Exception {
        RBucket<T> bucket = client().getBucket(key);
        return bucket.get();
    }

    <T> Boolean setBucket(final String key, final T obj) throws Exception {
        RBucket<T> bucket = client().getBucket(key);
        return bucket.trySet(obj);
    }

    <T> Boolean topicSubscribe(final String topicName, final PatternMessageListener<T> listener) throws Exception {
        Integer listenerId = _listeners.get(topicName);

        RPatternTopic<T> topic = client().getPatternTopic(topicName);
        int topicId = topic.addListener(listener);

        _listeners.put(topicName, topicId);
        return true;
    }

    <T> Boolean topicUnsubscribe(final String topicName) throws Exception {
        Integer listenerId = _listeners.get(topicName);
        if (listenerId != null) {
            RPatternTopic<T> topic = client().getPatternTopic(topicName);
            topic.removeListener(listenerId);
            return true;
        }

        return false;
    }

}

package org.redislabs.rnredis;

import java.util.HashMap;
import java.lang.String;
import java.lang.Boolean;
import java.util.Iterator;
import java.util.Map;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import org.redisson.Redisson;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.api.RPatternTopic;

import org.redisson.api.listener.PatternMessageListener;


class ReactNativeRedis extends ReactContextBaseJavaModule {

    private RedissonClient       redisson       = null;
    private Map<String, Integer> topicListeners = new HashMap<>();

    ReactNativeRedis(final ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private Boolean _init(final String jsonConfig) throws Exception {
        if (redisson == null) {
            Config config = Config.fromJSON(jsonConfig);
            config.setCodec(new StringCodec());
            redisson = Redisson.create(config);
        }
        return true;
    }

    private Boolean _destroy() throws Exception {
        if (redisson != null) {
            Iterator it = topicListeners.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                RPatternTopic<String> topic = redisson.getPatternTopic((String)pair.getKey());
                if (topic != null) {
                    topic.removeListener((Integer)pair.getValue());
                }
                it.remove();
            }
            topicListeners.clear();
        }

        redisson = null;
        return true;
    }

    private <T> Object _getBucket(final String key) throws Exception {
        if (redisson == null) {
            throw new Exception("Redis client not initialized");
        }

        RBucket<T> bucket = redisson.getBucket(key);
        return bucket.get();
    }

    private <T> Boolean _setBucket(final String key, final T obj) throws Exception {
        if (redisson == null) {
            throw new Exception("Redis client not initialized");
        }

        RBucket<T> bucket = redisson.getBucket(key);
        return bucket.trySet(obj);
    }

    @SuppressWarnings("unused")
    private <T> Boolean _topicSubscribe(final String topicName, final PatternMessageListener<T> listener) throws Exception {
        if (redisson == null) {
            throw new Exception("Redis client not initialized");
        }

        Integer listenerId = topicListeners.get(topicName);

        RPatternTopic<T> topic = redisson.getPatternTopic(topicName);
        int topicId = topic.addListener(listener);

        topicListeners.put(topicName, topicId);
        return true;
    }

    private <T> Boolean _topicUnsubscribe(final String topicName) throws Exception {
        if (ReactNativeRedis.this.redisson == null) {
            throw new Exception("Redis client not initialized");
        }

        Integer listenerId = topicListeners.get(topicName);
        if (listenerId != null) {
            RPatternTopic<T> topic = redisson.getPatternTopic(topicName);
            topic.removeListener(listenerId);
            return true;
        }

        return false;
    }

    @Override
    public String getName() {
        return "ReactNativeRedisAndroid";
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void init(final String config, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                ReactNativeRedis.this._init(config);
                return this._successResult(true);
            }
        }).start();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void destroy(Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                Boolean success = ReactNativeRedis.this._destroy();
                return this._successResult(success);
            }
        }).start();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void readObject(final String key, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                return ReactNativeRedis.this._getBucket(key);
            }
        }).start();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void saveObject(final String key, final String jsonObj, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                Boolean success = ReactNativeRedis.this._setBucket(key, jsonObj);
                return this._successResult(success);
            }
        }).start();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void subscribe(final String topicName, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                Boolean success = ReactNativeRedis.this._topicSubscribe(topicName, TopicListener.create());
                return this._successResult(success);
            }
        }).start();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void unsubscribe(final String topicName, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                Boolean success = ReactNativeRedis.this.<String>_topicUnsubscribe(topicName);
                return this._successResult(success);
            }
        }).start();
    }

}

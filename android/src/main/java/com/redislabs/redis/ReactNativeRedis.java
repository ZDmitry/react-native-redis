package com.redislabs.redis;

import java.util.HashMap;
import java.lang.String;
import java.lang.Boolean;
import java.util.Map;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;

import org.json.JSONObject;
import org.redisson.Redisson;
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

    private Boolean _init(final ReadableMap config) throws Exception {
        JSONObject jsonConfig = ReactNativeUtils.convertMapToJson(config);
        String strConfig = jsonConfig.toString();

        redisson = Redisson.create(Config.fromJSON(strConfig));
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

    @SuppressWarnings("unused")
    private <T> Boolean _topicUnsubscribe(final String topicName) throws Exception {
        if (ReactNativeRedis.this.redisson == null) {
            throw new Exception("Redis client not initialized");
        }

        Integer listenerId = topicListeners.get(topicName);
        if (listenerId != null) {
            RPatternTopic<String> topic = redisson.getPatternTopic(topicName);
            topic.removeListener(listenerId);
        }

        return true;
    }

    @Override
    public String getName() {
        return "ReactNativeRedisAndroid";
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void init(final ReadableMap config, Callback callback) {
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

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

import org.redisson.api.RedissonClient;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;


class ReactNativeRedis extends ReactContextBaseJavaModule {

    private RedissonClient       redisson       = null;
    private Map<String, Integer> topicListeners = new HashMap<>();

    ReactNativeRedis(final ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ReactNativeRedisAndroid";
    }

    @ReactMethod
    public void init(final ReadableMap config, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                JSONObject jsonConfig = ReactNativeUtils.convertMapToJson(config);
                String strConfig = jsonConfig.toString();

                ReactNativeRedis.this.redisson = Redisson.create(Config.fromJSON(strConfig));
                return this._successResult(true);
            }
        }).start();
    }

    @ReactMethod
    public void readObject(final String key, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                if (ReactNativeRedis.this.redisson == null) {
                    throw new Exception("Redis client not initialized");
                }

                RBucket<ReadableMap> bucket = ReactNativeRedis.this.redisson.getBucket(key);
                return bucket.get();
            }
        }).start();
    }

    @ReactMethod
    public void saveObject(final String key, final ReadableMap obj, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                if (ReactNativeRedis.this.redisson == null) {
                    throw new Exception("Redis client not initialized");
                }

                RBucket<ReadableMap> bucket = ReactNativeRedis.this.redisson.getBucket(key);
                Boolean success = bucket.trySet(obj);

                return this._successResult(success);
            }
        }).start();
    }

    @ReactMethod
    public void subscribe(final String topicName, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                if (ReactNativeRedis.this.redisson == null) {
                    throw new Exception("Redis client not initialized");
                }

                Integer listenerId = ReactNativeRedis.this.topicListeners.get(topicName);

                RTopic<ReadableMap> topic = ReactNativeRedis.this.redisson.getTopic(topicName);
                int topicId = topic.addListener(TopicListener.create());

                ReactNativeRedis.this.topicListeners.put(topicName, topicId);
                return this._successResult(true);
            }
        }).start();
    }

    @ReactMethod
    public void unsubscribe(final String topicName, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                if (ReactNativeRedis.this.redisson == null) {
                    throw new Exception("Redis client not initialized");
                }

                Integer listenerId = topicListeners.get(topicName);
                if (listenerId != null) {
                    RTopic<ReadableMap> topic = redisson.getTopic(topicName);
                    topic.removeListener(listenerId);
                }

                return this._successResult(true);
            }
        }).start();
    }

}

package com.redislabs.redis;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
// import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Date;
import java.lang.String;
import java.lang.Boolean;
import java.util.Map;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;

import org.json.JSONObject;
import org.redisson.Redisson;
import org.redisson.config.Config;

import org.redisson.api.RedissonClient;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;

import org.redisson.api.listener.MessageListener;


class ReactNativeRedis extends ReactContextBaseJavaModule {

    private RedissonClient redisson             = null;
    private Map<String, Integer> topicListeners = new HashMap<>();

    ReactNativeRedis(final ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private void _raiseNotInitialized(Callback callback) {
        _raiseException(callback, "ENOENT", "Redis client not initialized");
    }

    private void _raiseException(Callback callback, String code, String message) {
        _raiseException(callback, code, message, null);
    }

    private void _raiseException(Callback callback, String code, String message, WritableMap details) {
        if (details == null) {
            details = Arguments.createMap();
        }

        WritableMap errorMap = Arguments.createMap();

        errorMap.putString("code",    code);
        errorMap.putString("message", message);
        errorMap.putMap("details",    details);

        callback.invoke(errorMap);
    }

    @Override
    public String getName() {
        return "ReactNativeRedisAndroid";
    }

    @ReactMethod
    public void init(ReadableMap config, Callback callback) {
        try {
            JSONObject jsonConfig = ReactNativeUtils.convertMapToJson(config);
            String strConfig = jsonConfig.toString();

            redisson = Redisson.create(Config.fromJSON(strConfig));
            callback.invoke();
        } catch (Exception e) {
            _raiseException(callback, "EINVAL", e.getMessage());
        }
    }

    @ReactMethod
    public void readObject(String key, Callback callback) {
        if (redisson != null) {
            RBucket<ReadableMap> bucket = redisson.getBucket(key);
            WritableMap obj = new WritableNativeMap();
            obj.merge(bucket.get());
            callback.invoke(null, obj);
            return;
        }

        _raiseNotInitialized(callback);
    }

    @ReactMethod
    public void saveObject(String key, ReadableMap obj, Callback callback) {
        if (redisson != null) {
            RBucket<ReadableMap> bucket = redisson.getBucket(key);
            Boolean success = bucket.trySet(obj);
            callback.invoke(success);
            return;
        }

        _raiseNotInitialized(callback);
    }

    @ReactMethod
    public void subscribe(String topicName, Callback callback) {
        if (redisson != null) {
            Integer listenerId = topicListeners.get(topicName);

            RTopic<ReadableMap> topic = redisson.getTopic(topicName);
            int topicId = topic.addListener(TopicListener.create());

            topicListeners.put(topicName, topicId);
            callback.invoke(true);
            return;
        }

        _raiseNotInitialized(callback);
    }

    @ReactMethod
    public void unsubscribe(String topicName, Callback callback) {
        if (redisson != null) {
            Integer listenerId = topicListeners.get(topicName);
            if (listenerId != null) {
                RTopic<ReadableMap> topic = redisson.getTopic(topicName);
                topic.removeListener(listenerId);
                callback.invoke(true);
                return;
            }
        }

        _raiseNotInitialized(callback);
    }

}

package org.redislabs.rnredis;

import java.lang.String;
import java.lang.Boolean;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.Iterator;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;


class ReactNativeRedis extends ReactContextBaseJavaModule {

    private Map<String, Object> _storage;

    ReactNativeRedis(final ReactApplicationContext reactContext) {
        super(reactContext);
        _storage = new HashMap<>();
    }

    @Override
    protected void finalize() throws Throwable {
        _destroy();
        super.finalize();
    }

    void _destroy() {
        Iterator it = _storage.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            RNRedisClient client = (RNRedisClient)pair.getValue();

            try {
                client.destroy();
            } catch(Throwable e) {
                // ...
            } finally {
                it.remove();
            }
        }
    }

    private String _newObject(Object obj) {
        UUID uuid = UUID.randomUUID();
        _storage.put(uuid.toString(), obj);
        return uuid.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> T _getObject(final String uuid) throws Exception {
        if (!_storage.containsKey(uuid)) {
            throw new ReactException("ENULLPTR", "No such object with key \"" + uuid + "\"");
        }
        return (T) _storage.get(uuid);
    }

    private Boolean _delObject(final String uuid) {
        if (_storage.containsKey(uuid)) {
            _storage.remove(uuid);
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
    public void connect(final String config, final Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = new RNRedisClient(config);
                String uuid = ReactNativeRedis.this._newObject(client);

                WritableMap metaMap = Arguments.createMap();

                metaMap.putString("uuid", uuid);
                metaMap.putArray("address", client.address());
                metaMap.putInt("dbIndex", client.dbIndex());

                return metaMap;
            }
        }).start();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void destroy(final String uuid, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                client.destroy();

                Boolean success = ReactNativeRedis.this._delObject(uuid);
                return this._successResult(success);
            }
        }).start();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void readAllObjects(final String uuid, final String key, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                return client.getAllObjects();
            }
        }).startAsync();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void readObject(final String uuid, final String key, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                return client.getBucket(key);
            }
        }).startAsync();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void saveObject(final String uuid, final String key, final String jsonObj, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                Boolean success = client.setBucket(key, jsonObj);
                return this._successResult(success);
            }
        }).startAsync();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void subscribe(final String uuid, final String topicName, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                Boolean success = client.topicSubscribe(topicName, TopicListener.create(uuid));
                return this._successResult(success);
            }
        }).start();
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void unsubscribe(final String uuid, final String topicName, Callback callback) {
        (new ReactTask(callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                Boolean success = client.<String>topicUnsubscribe(topicName);
                return this._successResult(success);
            }
        }).start();
    }

}

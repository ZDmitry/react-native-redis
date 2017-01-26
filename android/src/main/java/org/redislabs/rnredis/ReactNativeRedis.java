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

    static final String TAG = "ReactNativeRedis";

    private Map<String, Object>    _storage  = new HashMap<>();
    private Map<String, ReactTask> _cmdQueue = new HashMap<>();

    ReactNativeRedis(final ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    protected void finalize() throws Throwable {
        _destroy();
        _cmdQueue.clear();
        super.finalize();
    }

    private void removeCommand(final String command) {
        _cmdQueue.remove(command);
    }

    private void cancelCommand(final String command) {
        cancelCommand(command, true);
    }

    private void cancelCommand(final String command, final Boolean terminate) {
        ReactTask task = _cmdQueue.get(command);
        if (task != null) {
            removeCommand(command);
            task.cancel(terminate);
        }
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
        final String cmdName = "connect";
        cancelCommand(cmdName, false);

        ReactTask task = (new ReactTask(getReactApplicationContext(), callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = new RNRedisClient(config);
                String uuid = ReactNativeRedis.this._newObject(client);

                WritableMap metaMap = Arguments.createMap();

                if (isCancelled()) {
                    ReactNativeRedis.this._delObject(uuid);
                    return null;
                }

                metaMap.putString("uuid", uuid);
                metaMap.putArray("address", client.address());
                metaMap.putInt("dbIndex", client.dbIndex());

                return metaMap;
            }

            @Override
            void finish() {
                ReactNativeRedis.this.removeCommand(cmdName);
            }
        }).startAsync();

        _cmdQueue.put(cmdName, task);
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void destroy(final String uuid, Callback callback) {
        final String cmdName = uuid + ":destroy";
        cancelCommand(cmdName);

        ReactTask task = (new ReactTask(getReactApplicationContext(), callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                client.destroy();

                Boolean success = ReactNativeRedis.this._delObject(uuid);
                return this._successResult(success);
            }
            @Override
            void finish() {
                ReactNativeRedis.this.removeCommand(cmdName);
            }
        }).startAsync();

        _cmdQueue.put(cmdName, task);
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void readObjects(final String uuid, final Integer firstIdx, final Integer lastIdx, Callback callback) {
        final String cmdName = uuid + ":readObjects";
        cancelCommand(cmdName);

        ReactTask task = (new ReactTask(getReactApplicationContext(), callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                return client.getObjects(this, firstIdx, lastIdx);
            }
            @Override
            void finish() {
                ReactNativeRedis.this.removeCommand(cmdName);
            }
        }).startAsync();

        _cmdQueue.put(cmdName, task);
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void readAllObjects(final String uuid, Callback callback) {
        final String cmdName = uuid + ":readAllObjects";
        cancelCommand(cmdName);

        final ReactTask task = (new ReactTask(getReactApplicationContext(), callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                return client.getObjects(this);
            }
            @Override
            void finish() {
                ReactNativeRedis.this.removeCommand(cmdName);
            }
        }).startAsync();

        _cmdQueue.put(cmdName, task);
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void readObject(final String uuid, final String key, Callback callback) {
        final String cmdName = uuid + ":readObject:" + key;
        cancelCommand(cmdName);

        ReactTask task = (new ReactTask(getReactApplicationContext(), callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                return client.getBucket(key);
            }
            @Override
            void finish() {
                ReactNativeRedis.this.removeCommand(cmdName);
            }
        }).startAsync();

        _cmdQueue.put(cmdName, task);
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void saveObject(final String uuid, final String key, final String jsonObj, Callback callback) {
        final String cmdName = uuid + ":saveObject:" + key;
        cancelCommand(cmdName);

        ReactTask task = (new ReactTask(getReactApplicationContext(), callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                Boolean success = client.setBucket(key, jsonObj);
                return this._successResult(success);
            }
            @Override
            void finish() {
                ReactNativeRedis.this.removeCommand(cmdName);
            }
        }).startAsync();

        _cmdQueue.put(cmdName, task);
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void subscribe(final String uuid, final String topicName, Callback callback) {
        final String cmdName = uuid + ":subscribe";
        cancelCommand(cmdName);

        ReactTask task = (new ReactTask(getReactApplicationContext(), callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                Boolean success = client.topicSubscribe(topicName, TopicListener.create(uuid));
                return this._successResult(success);
            }
            @Override
            void finish() {
                ReactNativeRedis.this.removeCommand(cmdName);
            }
        }).startAsync();

        _cmdQueue.put(cmdName, task);
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public void unsubscribe(final String uuid, final String topicName, Callback callback) {
        final String cmdName = uuid + ":unsubscribe";
        cancelCommand(cmdName);

        ReactTask task = (new ReactTask(getReactApplicationContext(), callback) {
            @Override
            Object run() throws Exception {
                RNRedisClient client = ReactNativeRedis.this._getObject(uuid);
                Boolean success = client.<String>topicUnsubscribe(topicName);
                return this._successResult(success);
            }
            @Override
            void finish() {
                ReactNativeRedis.this.removeCommand(cmdName);
            }
        }).startAsync();

        _cmdQueue.put(cmdName, task);
    }

}

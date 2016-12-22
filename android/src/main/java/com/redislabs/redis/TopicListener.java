package com.redislabs.redis;

import java.lang.String;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import org.redisson.api.listener.MessageListener;


class TopicListener {

    static MessageListener<ReadableMap> create() {
        return (new MessageListener<ReadableMap> () {
            @Override
            public void onMessage(String channel, ReadableMap msg) {
                EventEmitter emitter = EventEmitter.getInstance();

                WritableMap obj = new WritableNativeMap();
                obj.merge(msg);

                emitter.emit(channel, obj);
            }
        });
    }

}

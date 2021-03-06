package org.redislabs.rnredis;

import android.util.Log;

import java.lang.String;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import org.redisson.api.listener.PatternMessageListener;


class TopicListener {

    static PatternMessageListener create(final String uuid) {
        return (new PatternMessageListener<String> () {

            private final String _uuid = uuid;

            @Override
            public void onMessage(String pattern, String channel, String msg) {
                EventEmitter emitter = EventEmitter.getInstance();

                WritableMap eventMap = new WritableNativeMap();
                eventMap.putString("event", channel);
                eventMap.putString("pattern", pattern);
                eventMap.putString("message", msg);

                WritableMap returnMap = new WritableNativeMap();
                returnMap.putString("uuid", _uuid);
                returnMap.putMap("result", eventMap);

                emitter.emit("redis.event", returnMap);
            }
        });
    }

}

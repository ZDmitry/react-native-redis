package org.redislabs.rnredis;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


class ReactNativeUtils {

    static JSONObject convertMapToJson(ReadableMap readableMap) throws JSONException {
        JSONObject object = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    object.put(key, JSONObject.NULL);
                    break;
                case Boolean:
                    object.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    object.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    object.put(key, readableMap.getString(key));
                    break;
                case Map:
                    object.put(key, convertMapToJson(readableMap.getMap(key)));
                    break;
                case Array:
                    object.put(key, convertArrayToJson(readableMap.getArray(key)));
                    break;
            }
        }
        return object;
    }

    static JSONArray convertArrayToJson(ReadableArray readableArray) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.put(readableArray.getDouble(i));
                    break;
                case String:
                    array.put(readableArray.getString(i));
                    break;
                case Map:
                    array.put(convertMapToJson(readableArray.getMap(i)));
                    break;
                case Array:
                    array.put(convertArrayToJson(readableArray.getArray(i)));
                    break;
            }
        }
        return array;
    }

    static WritableMap writableMapFromObject(String key, Object obj) {
        WritableMap retval = Arguments.createMap();

        if (obj == null) {
            retval.putNull(key);
            return retval;
        }

        if (obj instanceof String) {
            retval.putString(key, (String)obj);
        } else if (obj instanceof Double) {
            retval.putDouble(key, (Double)obj);
        } else if (obj instanceof Boolean) {
            retval.putBoolean(key, (Boolean)obj);
        } else if (obj instanceof Integer) {
            retval.putInt(key, (Integer)obj);
        } else if (obj instanceof WritableMap) {
            retval.putMap(key, (WritableMap)obj);
        } else if (obj instanceof WritableArray) {
            retval.putArray(key, (WritableArray)obj);
        } else {
            retval.putNull(key);
        }

        return retval;
    }
}

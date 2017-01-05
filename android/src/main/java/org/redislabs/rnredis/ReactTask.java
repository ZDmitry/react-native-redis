package org.redislabs.rnredis;

import android.os.AsyncTask;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;


abstract class ReactTask extends AsyncTask<Object, Void, Object> {
    private Callback _callback = null;

    ReactTask(Callback callback) {
        _callback = callback;
    }

    @Override
    protected Object doInBackground(Object... params) {
        this.start();
        return null;
    }

    abstract Object run() throws Exception;

    void start() {
        try {
            Object result = this.run();
            this._returnJSResult(this._callback, result);
        } catch (Exception e) {
            String      message = e.getMessage();
            WritableMap details = Arguments.createMap();

            if (e instanceof ReactException) {
                ReactException re = (ReactException)e;
                details = re.getDetails();
            }

            this._raiseJSException(this._callback, "ERNINT", message, details);
        }
    }

    void startAsync() {
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    Object _successResult(Boolean success) {
        WritableMap successMap = Arguments.createMap();
        successMap.putBoolean("success", success);
        return successMap;
    }

    void _raiseJSException(Callback callback, String code, String message) {
        _raiseJSException(callback, code, message, null);
    }

    void _raiseJSException(Callback callback, String code, String message, WritableMap details) {
        if (details == null) {
            details = Arguments.createMap();
        }

        WritableMap errorMap = Arguments.createMap();

        errorMap.putString("code",    code);
        errorMap.putString("message", message);
        errorMap.putMap("details",    details);

        WritableMap resultMap = Arguments.createMap();
        resultMap.putMap("error", errorMap);

        callback.invoke(resultMap);
    }

    void _returnJSResult(Callback callback, Object result) {
        WritableMap returnMap = ReactNativeUtils.writableMapFromObject("result", result);
        callback.invoke(returnMap);
    }
}

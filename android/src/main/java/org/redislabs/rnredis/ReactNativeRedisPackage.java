package org.redislabs.rnredis;

import android.util.Log;

import com.facebook.react.ReactPackage;

import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;

import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class ReactNativeRedisPackage implements ReactPackage {

    private List<NativeModule> _modules = new ArrayList<>();

    @Override
    protected void finalize() throws Throwable {
        _destroy();
        super.finalize();
    }

    private void _destroy() {
        Iterator it = _modules.iterator();
        while (it.hasNext()) {
            ReactNativeRedis redis = (ReactNativeRedis)it.next();
            redis._destroy();
            it.remove();
        }
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Arrays.asList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.asList();
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        _destroy();
        EventEmitter.setup(reactContext);
        _modules.add(new ReactNativeRedis(reactContext));
        return _modules;
    }

}

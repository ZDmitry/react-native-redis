import {
  DeviceEventEmitter,
  NativeAppEventEmitter,
  NativeModules,
  Platform
} from 'react-native';

const ReactNativeRedisAndroid = NativeModules.ReactNativeRedisAndroid;

let bridge = null;
let notification_listeners = [];

let call_notification_listeners = function (notification) {
    var i = notification_listeners.length - 1;

    for (i; i >= 0; i--) {
        notification_listeners[i](notification);
    }
}

switch (Platform.OS) {
    case 'android':
        bridge = ReactNativeRedisAndroid;
        DeviceEventEmitter.addListener('receivedNotification', (notification) => {
            call_notification_listeners(notification);
        });
        break;
}


export class Redis {

    static connect(config) {
        return new Promise((resolve, reject) => {
            bridge.init(config, (err, result) => {
                if (err) {
                    reject(err);
                    return;
                }
                resolve(result)
            })
        });
    }

    static readObject(key) {
        return new Promise((resolve, reject) => {
            bridge.readObject(key, (err, result) => {
                if (err) {
                    reject(err);
                    return;
                }
                resolve(result)
            })
        });
    }

    static saveObject(key, obj) {
        return new Promise((resolve, reject) => {
            bridge.saveObject(key, obj, (err, result) => {
                if (err) {
                    reject(err);
                    return;
                }
                resolve(result)
            })
        });
    }

    static subscribe(topic) {
        return new Promise((resolve, reject) => {
            bridge.subscribe(topic, (err, result) => {
                if (err) {
                    reject(err);
                    return;
                }
                resolve(result)
            })
        });
    }

    static unsubscribe(topic) {
        return new Promise((resolve, reject) => {
            bridge.unsubscribe(topic, (err, result) => {
                if (err) {
                    reject(err);
                    return;
                }
                resolve(result)
            })
        });
    }

    static onNotification(callback) {
        notification_listeners.push(callback);
    }
}

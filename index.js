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
            // ...
        });
        break;
}


export class Redis {

    static on_notification(callback) {
        notification_listeners.push(callback);
    }

}

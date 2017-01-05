import {
  DeviceEventEmitter,
  NativeAppEventEmitter,
  NativeModules,
  Platform
} from 'react-native';

const ReactNativeRedisAndroid = NativeModules.ReactNativeRedisAndroid;

let bridge = null;
let notification_listeners = {};

let call_notification_listeners = function (listener, notification) {
  let callback = notification_listeners[listener];
  if (typeof callback === 'function') {
    callback(notification);
  }
};

switch (Platform.OS) {
  case 'android':
    bridge = ReactNativeRedisAndroid;
    DeviceEventEmitter.addListener('redis.event', (notification) => {
      let message  = (notification && notification.result);
      let listener = (notification && notification.uuid);
      call_notification_listeners(listener, message);
    });
    break;
}

export default {
  bridge: bridge,
  notificationListeners: notification_listeners
}


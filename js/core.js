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
};

let keySpaceNotificationTransform = function(msg) {
  if (msg) {
    let eventName = msg.event || '';
    if (eventName.startsWith('__keyspace@')) {
      eventName   = eventName.slice('__keyspace@'.length);

      let dbName  = eventName.split(':')[0];
      let varName = eventName.split(':')[1];

      dbName = dbName[0];

      return {
        action: msg.message,
        target: varName,
        scope:  parseInt(dbName)
      }
    }
  }
};

switch (Platform.OS) {
  case 'android':
    bridge = ReactNativeRedisAndroid;
    DeviceEventEmitter.addListener('redis.event', (notification) => {
      notification = (notification && notification.result);

      let keySpaceMsg = keySpaceNotificationTransform(notification);

      if (keySpaceMsg) {
        notification = keySpaceMsg
      }

      call_notification_listeners(notification);
    });
    break;
}

export default {
  bridge: bridge,
  notificationListeners: notification_listeners
}


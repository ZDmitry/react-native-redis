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
    DeviceEventEmitter.addListener('redis.event', (notification) => {
      call_notification_listeners(notification);
    });
    break;
}


export class Redis {

  static connect(config) {
    return new Promise((resolve, reject) => {
      bridge.init(JSON.stringify(config || '{}'), answ => {
        if (answ.error) {
          reject(answ.error);
          return;
        }
        resolve(answ.result)
      })
    });
  }

  static readObject(key) {
    return new Promise((resolve, reject) => {
      bridge.readObject(key, answ => {
        if (answ.error) {
          reject(answ.error);
          return;
        }
        resolve(JSON.parse(answ.result || "null"))
      })
    });
  }

  static saveObject(key, obj) {
    return new Promise((resolve, reject) => {
      bridge.saveObject(key, JSON.stringify(obj || '{}'), answ => {
        if (answ.error) {
          reject(answ.error);
          return;
        }
        resolve(answ.result)
      })
    });
  }

  static subscribe(topic) {
    return new Promise((resolve, reject) => {
      bridge.subscribe(topic, answ => {
        if (answ.error) {
          reject(answ.error);
          return;
        }
        resolve(answ.result)
      })
    });
  }

  static unsubscribe(topic) {
    return new Promise((resolve, reject) => {
      bridge.unsubscribe(topic, answ => {
        if (answ.error) {
          reject(answ.error);
          return;
        }
        resolve(answ.result)
      })
    });
  }

  static onNotification(callback) {
    notification_listeners.push(callback);
  }
}

import Core from './core'
import { RedisError, RedisNoAnswerError } from './errors'

export class Redis {

  static connect(config) {
    return new Promise((resolve, reject) => {
      Core.bridge.init(JSON.stringify(config || '{}'), answ => {
        if (!answ) {
          reject(new RedisNoAnswerError());
          return;
        }

        if (answ.error) {
          reject(new RedisError(answ.error));
          return;
        }

        resolve(answ.result);
      });
    });
  }

  static destroy() {
    Core.notificationListeners.splice(0, Core.notificationListeners.length);
    return new Promise((resolve, reject) => {
      Core.bridge.destroy(answ => {
        if (!answ) {
          reject(new RedisNoAnswerError());
          return;
        }

        if (answ.error) {
          reject(new RedisError(answ.error));
          return;
        }

        resolve(answ.result);
      });
    });
  }

  static readObject(key) {
    return new Promise((resolve, reject) => {
      bridge.readObject(key, answ => {
        if (!answ) {
          reject(new RedisNoAnswerError());
          return;
        }

        if (answ.error) {
          reject(new RedisError(answ.error));
          return;
        }

        resolve(JSON.parse(answ.result || "null"));
      });
    });
  }

  static saveObject(key, obj) {
    return new Promise((resolve, reject) => {
      Core.bridge.saveObject(key, JSON.stringify(obj || '{}'), answ => {
        if (!answ) {
          reject(new RedisNoAnswerError());
          return;
        }

        if (answ.error) {
          reject(new RedisError(answ.error));
          return;
        }

        resolve(answ.result);
      });
    });
  }

  static subscribe(topic) {
    return new Promise((resolve, reject) => {
      Core.bridge.subscribe(topic, answ => {
        if (!answ) {
          reject(new RedisNoAnswerError());
          return;
        }

        if (answ.error) {
          reject(new RedisError(answ.error));
          return;
        }

        resolve(answ.result);
      });
    });
  }

  static unsubscribe(topic) {
    return new Promise((resolve, reject) => {
      Core.bridge.unsubscribe(topic, answ => {
        if (!answ) {
          reject(new RedisNoAnswerError());
          return;
        }

        if (answ.error) {
          reject(new RedisError(answ.error));
          return;
        }

        resolve(answ.result);
      });
    });
  }

  static onNotification(callback) {
    Core.notificationListeners.push(callback);
  }
}

import Core from './core'

import { RedisClient } from './client'
import { RedisError, RedisNoAnswerError } from './errors'

export class Redis {

  static connect(config) {
    return new Promise((resolve, reject) => {
      Core.bridge.connect(JSON.stringify(config || '{}'), answ => {
        if (!answ) {
          reject(new RedisNoAnswerError());
          return;
        }

        if (answ.error) {
          reject(new RedisError(answ.error));
          return;
        }

        resolve(new RedisClient(Core, answ.result));
      });
    });
  }


}

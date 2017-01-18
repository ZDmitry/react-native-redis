import { BaseClass } from "../base-class";

export class RedisNoAnswerError extends BaseClass(Error) {
  constructor() {
    super('Redis error: no answer');
    this.name = "RedisNoAnswerError";
  }
}

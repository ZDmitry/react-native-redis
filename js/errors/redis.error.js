import { BaseClass } from "../base-class";

export class RedisError extends BaseClass(Error) {
  constructor(details, object) {
    super('Redis internal error');

    this.name    = "RedisError";
    this.details = details;
    this.object  = object;

    if (details.code === "ERNINT") {
      this.details = details.message;
      this.object  = details.details;
    }
  }
}

# react-native-redis
Redis client for React Native

## Installation

### Automatic (for Android with RN 0.28 and before)

If you have rnpm installed, all you need to do is

```
npm install zdmitry/react-native-redis --save
rnpm link react-native-redis
```


### Manual

#### Android (with RN 0.29 and above)
in `settings.gradle`

```
include ':react-native-redis'
project(':react-native-redis').projectDir = file('../node_modules/react-native-redis/android')
```

in `android/app/build.gradle`

```
dependencies {
    compile project(':react-native-redis')
```

in `MainApplication.java`
add package to getPacakges()

```java
import com.redislabs.redis.ReactNativeRedisPackage;

// ...

@Override
protected List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
        new MainReactPackage(),
        // ... ,
        new ReactNativeRedisPackage()
   );
}
```

Additionally multi dex support could be required.


##API

`connect` (Promise) - preform connect to the Redis server

`destroy` (Promise) - destroy redis client instance (optional)

`saveObject` (Promise) - save object to redis storage

`readObject` (Promise) - read object from redis storage

`subscribe` (Promise) - subscribe for redis topic events

`unsubscribe` (Promise) - undo redis topic subscription


##Usage

Methods should be called from React Native as any other promise.
Prevent methods from being called multiple times (on Android).

###Example

```javascript
import { Redis } from 'react-native-redis';

// ...

Redis.connect({
  "singleServerConfig": {
    "address": "redis://127.0.0.1:6379",
    "database": 0
  }
}).then(() => {
  Redis.saveObject('test1', {
    "val1": "test_text",
    "val2": 22
  }).then((val) => {
    console.log('redis.saveObject = ', val);
  });

  Redis.readObject('test1').then((val) => {
    console.log('redis.readObject = ', val);
  });
  
  Redis.subscribe('__keyspace@*__:*').then(() => {
    Redis.onNotification(msg => {
      if (msg.target && msg.action === 'set') {
        Redis.readObject(msg.target).then((newVal) => {
          console.log('redis.event', msg, newVal);
        })
      }
    });
  });
});

```

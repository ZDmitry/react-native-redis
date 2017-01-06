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
import org.redislabs.rnredis.ReactNativeRedisPackage;

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

`connect(Object config)` (Promise) - preform connect to the Redis server

`client.destroy()` (Promise) - destroy redis client instance (optional)

`client.saveObject(String name, Object object)` (Promise) - save object to redis storage

`client.readObject(String name)` (Promise) - read object from redis storage

`client.subscribe(String topicName)` (Promise) - subscribe for redis topic events

`client.unsubscribe(String topicName)` (Promise) - undo redis topic subscription


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
}).then((client) => {
  client.saveObject('test1', {
    "val1": "test_text",
    "val2": 22
  }).then((val) => {
    console.log('redis.saveObject = ', val);
  });

  client.readObject('test1').then((val) => {
    console.log('redis.readObject = ', val);
  });
  
  client.listen(msg => {
    if (msg.target && msg.action === 'set') {
      client.readObject(msg.target).then((newVal) => {
        console.log('redis.event', msg, newVal);
      })
    }
  });

  client.subscribe('__keyspace@*__:*').then(() => {
    // ...
  });
});

```

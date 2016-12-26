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

```
import com.redislabs.redis.ReactNativeRedisPackage;
...

@Override
protected List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
        new MainReactPackage(),
        ...,
        new ReactNativeRedisPackage()
   );
}
```

Additionally multi dex support could be required.


##API

`getContact` (Promise) - returns basic contact data as a JS object.  Currently returns name, first phone number and first email for contact.
`getEmail` (Promise) - returns first email address (if found) for contact as string.


##Usage

Methods should be called from React Native as any other promise.
Prevent methods from being called multiple times (on Android).

###Example

```
import { Redis } from 'react-native-redis';
...
    Redis.connect({
      "singleServerConfig": {
        "address": "redis://127.0.0.1:6379",
        "database": 0
      }
    }).then(() => {
      result = await Redis.saveObject('test1', {
        "val1": "test_text",
        "val2": 22
      });
      console.log('redis.saveObject = ', result);

      result = await Redis.readObject('test1');
      console.log('redis.readObject = ', result);

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

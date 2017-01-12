#import <Foundation/Foundation.h>

@interface RNRedisClient : NSObject

+ (RNRedisClient*) connect:(NSString*)conn;

@end

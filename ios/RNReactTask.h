#import <Foundation/Foundation.h>
#import <RCTBridge.h>

typedef NSDictionary* (^RNReactTaskBlock)();

@interface RNReactTask : NSObject

+ (RNReactTask*) task:(RNReactTaskBlock)block reactCallback:(RCTResponseSenderBlock)callback;

- (void) run;
- (void) runAsync;

@end

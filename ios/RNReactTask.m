#import "RNReactTask.h"

@implementation RNReactTask {
    RNReactTaskBlock       _task;
    RCTResponseSenderBlock _callback;
}

+ (RNReactTask*) task:(RNReactTaskBlock)block reactCallback:(RCTResponseSenderBlock)callback
{
    return [[RNReactTask alloc] init:block reactCallback:callback];
}

- (id) init:(RNReactTaskBlock)block reactCallback:(RCTResponseSenderBlock)callback {
    self = [super init];
    if (self) {
        _task     = block;
        _callback = callback;
    }
    return self;
}

- (NSDictionary*) _raiseExeption:(NSString*)code message:(NSString*)message details:(NSDictionary*)details
{
    return @{
      @"code":    code,
      @"message": message,
      @"details": details
    };
}

- (void) run
{
    NSMutableDictionary* dict = [[NSMutableDictionary alloc] init];
    
    @try {
        NSDictionary* retval = _task();
        [dict setValue:retval forKey:@"result"];
    } @catch (NSException* e) {
        [dict setValue:@{
          @"code":    @"ERNINT",
          @"message": e.reason,
          @"details": @{
            @"name":  e.name
          }
        } forKey:@"error"];
    } @finally {
        _callback(@[dict]);
    }
}

- (void) runAsync
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [self run];
    });
}

@end

#import "RNRedisManager.h"

@implementation RNRedisManager {
    NSMutableDictionary* _storage;
}

RCT_EXPORT_MODULE();

- (id) init
{
    self = [super init];
    if (self) {
        _storage = [[NSMutableDictionary alloc] init];
    }
    return self;
}

RCT_EXPORT_METHOD(connect:(NSString*)config callback:(RCTResponseSenderBlock)callback)
{

}

@end

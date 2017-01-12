#import "RNRedisClient.h"
#include "hiredis/hiredis.h"

NSString* REDIS_PREFIX = @"redis://";


@implementation RNRedisClient {
    redisContext* _context;
}

+ (RNRedisClient*) connect:(NSString*)conn
{
    RNRedisClient* client = [[RNRedisClient alloc] init];
    if ([client _connect:conn]) {
        return client;
    }
    
    client = nil;
    return nil;
}

- (void) dealloc
{
    if (_context) {
        redisFree(_context);
        _context = NULL;
    }
}

- (bool) _connect:(NSString*)conn
{
    if ([conn hasPrefix:REDIS_PREFIX]) {
        conn = [conn substringFromIndex:REDIS_PREFIX.length];
        NSArray* connParams = [conn componentsSeparatedByString:@":"];
        
        NSString* connHost = [connParams objectAtIndex:0];
        NSString* connPort = [connParams objectAtIndex:0];
        
        _context = redisConnect([connHost UTF8String], [connPort intValue]);
        return (_context != NULL);
    }
    
    return false;
}

@end

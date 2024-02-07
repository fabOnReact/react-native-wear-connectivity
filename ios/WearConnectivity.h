
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNWearConnectivitySpec.h"

@interface WearConnectivity : NSObject <NativeWearConnectivitySpec>
#else
#import <React/RCTBridgeModule.h>

@interface WearConnectivity : NSObject <RCTBridgeModule>
#endif

@end

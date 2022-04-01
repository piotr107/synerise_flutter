#import "SyneriseFlutterPlugin.h"
#if __has_include(<synerise_flutter/synerise_flutter-Swift.h>)
#import <synerise_flutter/synerise_flutter-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "synerise_flutter-Swift.h"
#endif

@implementation SyneriseFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftSyneriseFlutterPlugin registerWithRegistrar:registrar];
}
@end

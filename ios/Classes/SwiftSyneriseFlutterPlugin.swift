import Flutter
import UIKit
import SyneriseSDK

public class SwiftSyneriseFlutterPlugin: NSObject, FlutterPlugin, SyneriseDelegate {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "synerise_flutter", binaryMessenger: registrar.messenger())
    let instance = SwiftSyneriseFlutterPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      if (call.method == "initSynerise") {
          guard let args = call.arguments as? [String: String] else {
              result("iOS could not recognize flutter arguments for method initSynerise()")
              return
          }
          initSynerise(apiKey: args["apiKey"]!, appId: args["appId"]!)
          result("Synerise client UUID: " + Client.getUUID())
      }
      result("Method not implemented")
  }
    
    private func initSynerise(apiKey: String, appId: String) {
        Synerise.initialize(clientApiKey: apiKey)
        Synerise.setDebugModeEnabled(true)
        Synerise.setCrashHandlingEnabled(true)
        Synerise.setDelegate(self)
    }
    
}

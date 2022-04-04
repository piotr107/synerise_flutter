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
      switch (call.method) {
        case "initSynerise":
          guard let args = call.arguments as? [String: String] else {
              result("iOS could not recognize flutter arguments for method initSynerise()")
              return
          }
          initSynerise(apžiKey: args["apiKey"]!, appId: args["appId"]!)
          result("Synerise client UUID: " + Client.getUUID())
          break

        case "authorizeByOauth":
          guard let token = call.arguments as? String else {
              result("Missing OAuth token")
              return
          }
          authorizeByOauth(token: token, result: result)
          break

        default:
          result("Method not implemented: " + call.method)
          break
      }
  }
    
    private func initSynerise(apiKey: String, appId: String) {
        Synerise.initialize(clientApiKey: apiKey)
        Synerise.setDebugModeEnabled(true)
        Synerise.setCrashHandlingEnabled(true)
        Synerise.setDelegate(self)
    }
    
    private func authorizeByOauth(token: String, result: @escaping FlutterResult) {
        Client.authenticate(token: token, clientIdentityProvider: ClientIdentityProvider.oAuth, authID: nil, context: nil, success: { (value: Bool) in
            result("OAuth result: " + String(value))
        }, failure: { (error: SNRApiError) in
            result(error.localizedDescription)
        })
    }
    
}

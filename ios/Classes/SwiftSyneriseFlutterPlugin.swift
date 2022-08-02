import Flutter
import UIKit
import SyneriseSDK
import os.log

public class SwiftSyneriseFlutterPlugin: NSObject, FlutterPlugin, SyneriseDelegate {
    
    var channel: FlutterMethodChannel
    
    init(withMethodChannel _channel: FlutterMethodChannel) {
        channel = _channel
    }
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let _channel = FlutterMethodChannel(name: "synerise_flutter", binaryMessenger: registrar.messenger())
    let _instance = SwiftSyneriseFlutterPlugin(withMethodChannel: _channel)
    registrar.addApplicationDelegate(_instance)
    registrar.addMethodCallDelegate(_instance, channel: _channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      switch (call.method) {
        case "initSynerise":
          guard let args = call.arguments as? [String: String] else {
              result("iOS could not recognize flutter arguments for method initSynerise()")
              return
          }
          if (Synerise.settings.sdk.appGroupIdentifier != nil) {
              result("Synerise client UUID: " + Client.getUUID())
              break
          }
          initSynerise(apiKey: args["apiKey"]!, appId: args["appId"]!, appGroupIdentifier: args["appGroupIdentifier"]!, keychainGroupIdentifier: args["appGroupIdentifier"]!)
          result("Synerise client UUID: " + Client.getUUID())
          break

        case "authorizeByOauth":
          guard let token = call.arguments as? String else {
              result("Missing OAuth token")
              return
          }
          authorizeByOauth(token: token, result: result)
          break
          
        case "trackScreenView":
          guard let screenName = call.arguments as? String else {
              result("Missing screen name")
              return
          }
          trackScreenView(screenName: screenName)
          break
          
        case "registerFcmToken":
          guard let fcmToken = call.arguments as? String else {
              result("Missing Fcm token")
              return
          }
          registerFcmToken(fcmToken: fcmToken, result: result)
          break
          
        case "trackEvent":
          guard let args = call.arguments as? [String: AnyObject] else {
              result("iOS could not recognize flutter arguments for method trackEvent")
              return
          }
          if let params = args["params"] as? [String : String] {
              trackEventWithParams(action: args["action"] as! String, label: args["label"] as! String, params: params)
          } else {
              trackEvent(action: args["action"] as! String, label: args["label"] as! String)
          }
          
          break

        case "trackPushViewed":
          guard let label = call.arguments as? String else {
              result("Missing label")
              return
          }
          trackPushViewedEvent(label: label)
          break

        case "trackPushClicked":
          guard let label = call.arguments as? String else {
              result("Missing label")
              return
          }
          trackPushClickedEvent(label: label)
          break

        default:
          result("Method not implemented: " + call.method)
      }
  }
    
    private func initSynerise(apiKey: String, appId: String, appGroupIdentifier: String, keychainGroupIdentifier: String) {
        Synerise.settings.sdk.appGroupIdentifier = appGroupIdentifier
        Synerise.settings.sdk.keychainGroupIdentifier = keychainGroupIdentifier
        Synerise.initialize(clientApiKey: apiKey)
        Synerise.setDebugModeEnabled(true)
        Synerise.setCrashHandlingEnabled(true)
        Synerise.setDelegate(self)
        Synerise.settings.tracker.autoTracking.enabled = false
        Synerise.settings.notifications.disableInAppAlerts = true
        initNotificationSettings()
    }

    private func initNotificationSettings() {


        let singleMediaCategory = UNNotificationCategory(identifier: SNRSingleMediaContentExtensionViewControllerCategoryIdentifier, actions: [], intentIdentifiers: [], options: [])

        let carouselPrevious = UNNotificationAction(identifier: SNRCarouselContentExtensionViewControllerPreviousItemIdentifier, title: "Previous", options: [])
        let carouselAction = UNNotificationAction(identifier: SNRCarouselContentExtensionViewControllerChooseItemIdentifier, title: "Go!", options: UNNotificationActionOptions.foreground)
        let carouselNext = UNNotificationAction(identifier: SNRCarouselContentExtensionViewControllerNextItemIdentifier, title: "Next", options: [])
        let carouselCategory = UNNotificationCategory(identifier: SNRCarouselContentExtensionViewControllerCategoryIdentifier, actions: [carouselPrevious, carouselAction, carouselNext], intentIdentifiers: [], options: [])

        UNUserNotificationCenter.current().setNotificationCategories([singleMediaCategory, carouselCategory])
    }
    
    private func authorizeByOauth(token: String, result: @escaping FlutterResult) {
        Client.authenticate(token: token, clientIdentityProvider: ClientIdentityProvider.oAuth, authID: nil, context: nil, success: { (value: Bool) in
            result("OAuth result: " + String(value))
        }, failure: { (error: SNRApiError) in
            result(error.localizedDescription)
        })
    }
    
    private func trackScreenView(screenName: String) {
        let event: VisitedScreenEvent = VisitedScreenEvent.init(label: screenName)
        Tracker.send(event);
    }
    
    private func registerFcmToken(fcmToken: String, result: @escaping FlutterResult) {
        Client.registerForPush(registrationToken: fcmToken, mobilePushAgreement: true, success: { (success) in
            result("Register for Push succeed: " + String(fcmToken))
        }) { (error) in
            result("Register for push failed: " + String(fcmToken))
        }
    }

    private func trackEventWithParams(action: String, label: String, params: [String:String]) {
        let parameters: TrackerParams = TrackerParams.make {
            builder in
            for (key, value) in params {
                builder.setString(value, forKey: key)
            }
        }
        let event: CustomEvent = CustomEvent.init(label: label, action: action, params: parameters)

        Tracker.send(event)
        print("trackEventWithParams success, label: " + label + " action: " + action)
    }
    
    private func trackEvent(action: String, label: String) {
        let event: CustomEvent = CustomEvent.init(label: label, action: action)

        Tracker.send(event)
        print("trackEvent success, label: " + label + " action: " + action)
    }

    private func trackPushViewedEvent(label: String) {
        let event: PushViewedEvent = PushViewedEvent.init(label: label)

        Tracker.send(event)
        print("pushViewedEvent success, label: " + label)
    }

    private func trackPushClickedEvent(label: String) {
        let event: PushClickedEvent = PushClickedEvent.init(label: label)

        Tracker.send(event)
        print("pushClickedEvent success, label: " + label)
    }
    
    public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
      let userInfo = response.notification.request.content.userInfo
      let isSyneriseNotification: Bool = Synerise.isSyneriseNotification(userInfo)
      if isSyneriseNotification {
          Synerise.handleNotification(userInfo, actionIdentifier: response.actionIdentifier)
          let rawNotificationContent = userInfo["content"] as! String
          let rawData = rawNotificationContent.data(using: .utf8)
          let decoder = JSONDecoder()
          let notificationContent = try! decoder.decode(SyneriseNotificationContent.self, from: rawData!)
          let action = notificationContent.notification.action
          if (action.type == "DEEP_LINKING") {
            channel.invokeMethod("onUrlOpen", arguments: action.item)
          }
          if (action.type == "OPEN_APP") {
            channel.invokeMethod("onAppOpen2", arguments: nil)
          }
      }
      completionHandler()
    }

    public func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
      let userInfo = notification.request.content.userInfo

      let isSyneriseNotification: Bool = Synerise.isSyneriseNotification(userInfo)
      
      if isSyneriseNotification {
        Synerise.handleNotification(userInfo)
        if #available(iOS 14.0, *) {
            completionHandler([.banner, .badge, .sound])
        } else {
            completionHandler([.alert, .badge, .sound])
        }
      }
    }
    
}

struct SyneriseNotificationContent: Codable {
    let notification: SyneriseNotificationData
}

struct SyneriseNotificationData: Codable {
    let action: SyneriseNotificationAction
}

struct SyneriseNotificationAction: Codable {
    let item: String?
    let type: String
}

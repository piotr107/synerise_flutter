import UIKit
import Flutter
import Firebase
import SyneriseSDK

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate, MessagingDelegate, SyneriseDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
      Synerise.initialize(clientApiKey: Keys.apiKey)
      FirebaseApp.configure()
             Messaging.messaging().delegate = self

             if #available(iOS 10, *) {
                 UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { (granted, error) in

                 }
             } else {
                 let settings = UIUserNotificationSettings(types: [.alert, .badge, .sound], categories: nil)
                 application.registerUserNotificationSettings(settings)
             }

             application.registerForRemoteNotifications()
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
    
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String) {
        Client.registerForPush(registrationToken: fcmToken, mobilePushAgreement: true, success: { (success) in
               print("registerForPush success")
           }) { (error) in
               print("registerForPush failure")
           }
       }
    
    func snr_registerForPushNotificationsIsNeeded() -> Void {
          guard let fcmToken = Messaging.messaging().fcmToken else {
              return
          }

          Client.registerForPush(registrationToken: fcmToken, mobilePushAgreement: true, success: { (success) in
              print("registerForPush success")
          }) { (error) in
              print("registerForPush failure")
          }
      }
    
    // Support for Push Notifications on iOS 9
    // Support for Silent Notifications

    override func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
      let isSyneriseNotification: Bool = Synerise.isSyneriseNotification(userInfo)
      
      if isSyneriseNotification {
        Synerise.handleNotification(userInfo)
        completionHandler(.noData)
      }
    }

    override func application(_ application: UIApplication, handleActionWithIdentifier identifier: String?, forRemoteNotification userInfo: [AnyHashable : Any], completionHandler: @escaping () -> Void) {
      let isSyneriseNotification: Bool = Synerise.isSyneriseNotification(userInfo)
      
      if isSyneriseNotification {
        Synerise.handleNotification(userInfo, actionIdentifier: identifier)
        completionHandler()
      }
    }

    // Support for Push Notifications on iOS 10 and above

    // MARK: - UNUserNotificationCenterDelegate

    @available(iOS 10.0, *)
    override func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
      let userInfo = response.notification.request.content.userInfo
      
      let isSyneriseNotification: Bool = Synerise.isSyneriseNotification(userInfo)
      
      if isSyneriseNotification {
        Synerise.handleNotification(userInfo, actionIdentifier: response.actionIdentifier)
        completionHandler()
      }
    }

    @available(iOS 10.0, *)
    override func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
      let userInfo = notification.request.content.userInfo

      let isSyneriseNotification: Bool = Synerise.isSyneriseNotification(userInfo)
      
      if isSyneriseNotification {
        Synerise.handleNotification(userInfo)
        completionHandler(UNNotificationPresentationOptions.init(rawValue: 0))
      }
    }
}

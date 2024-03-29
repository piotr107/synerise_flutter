package com.healthdom.synerise_flutter;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.healthdom.synerise_flutter.util.Keys;
import com.healthdom.synerise_flutter.util.OauthErrorHandler;
import com.healthdom.synerise_flutter.util.OauthSuccessHandler;
import com.healthdom.synerise_flutter.util.PushThread;
import com.synerise.sdk.client.Client;
import com.synerise.sdk.client.model.ClientIdentityProvider;
import com.synerise.sdk.core.Synerise;
import com.synerise.sdk.core.listeners.OnLocationUpdateListener;
import com.synerise.sdk.core.listeners.SyneriseListener;
import com.synerise.sdk.core.net.IApiCall;
import com.synerise.sdk.core.types.enums.HostApplicationType;
import com.synerise.sdk.core.types.enums.MessagingServiceType;
import com.synerise.sdk.event.Tracker;
import com.synerise.sdk.event.TrackerParams;
import com.synerise.sdk.event.model.CustomEvent;
import com.synerise.sdk.event.model.interaction.VisitedScreenEvent;
import com.synerise.sdk.injector.Injector;
import com.synerise.sdk.injector.callback.InjectorSource;
import com.synerise.sdk.injector.callback.OnInjectorListener;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/** SyneriseFlutterPlugin */
public class SyneriseFlutterPlugin extends BroadcastReceiver implements FlutterPlugin, ActivityAware, MethodCallHandler, OnLocationUpdateListener, SyneriseListener, OnInjectorListener, PluginRegistry.NewIntentListener {

  private MethodChannel channel;
  private Activity activity;
  private String lastData;
  private static final String TAG = SyneriseFlutterPlugin.class.getSimpleName();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    Log.d(TAG, "onAttachedToEngine");
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "synerise_flutter");
    channel.setMethodCallHandler(this);
    IntentFilter intentFilter = new IntentFilter();
    LocalBroadcastManager manager =
            LocalBroadcastManager.getInstance(flutterPluginBinding.getApplicationContext());
    manager.registerReceiver(this, intentFilter);
  }


  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "initSynerise":
        if (Synerise.getAppId() != null) {
          result.success("Android Synerise AppId: " + Synerise.getAppId());
          break;
        }
        final String apiKey = call.argument("apiKey");
        final String appId = call.argument("appId");
        initSynerise(apiKey, appId);
        result.success("Android Synerise ApiKey: " + apiKey + " AppId: " + Synerise.getAppId());
        break;
      case "authorizeByOauth":
        final String token = call.arguments.toString();
        authorizeByOauth(token, result);
        break;
      case "authorizeByOauthWithCheck":
        final String tokentoCheck = call.arguments.toString();
        authorizeByOauthWithCheck(tokentoCheck, result);
        break;
      case "trackScreenView":
        final String screenName = call.arguments.toString();
        trackScreenView(screenName);
        break;
      case "registerFcmToken":
        final String fcmToken = call.arguments.toString();
        registerFcmToken(fcmToken, result);
        break;
      case "trackEvent":
        final String action = call.argument("action");
        final String label = call.argument("label");
        final Map<String, String> params = call.argument("params");
        trackEvent(action, label, params);
        break;
      case "getLastData":
        result.success(lastData);
        break;
      default:
        result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    Log.d(TAG, "onDetachedFromEngine");
    channel.setMethodCallHandler(null);
    if (binding.getApplicationContext() != null) {
      LocalBroadcastManager.getInstance(binding.getApplicationContext()).unregisterReceiver(this);
    }
  }

  private void trackEvent(String action, String label, Map<String, String> params) {
    Log.d(TAG, "Track event: " + action + " , label: " + label);
    if (params != null) {
      Log.d(TAG, "Track event with params: " + params.toString());
      TrackerParams.Builder builder = new TrackerParams.Builder();
      for (Map.Entry<String, String> entry : params.entrySet()) {
        builder.add(entry.getKey(), entry.getValue());
      }
      TrackerParams trackerParams = builder.build();
      Tracker.send(new CustomEvent(action, label, trackerParams));
    } else {
      Tracker.send(new CustomEvent(action, label));
    }
  }

  @Override
  public void onLocationUpdateRequired() {
    Log.d(TAG, "onLocationUpdateRequired");
  }

  @Override
  public void onInitializationCompleted() {
    Log.d(TAG, "onInitializationCompleted");
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    Log.d(TAG, "Synerise onAttachedToActivity");
    binding.addOnNewIntentListener(this);
    this.activity = binding.getActivity();
    if (activity.getIntent() != null && activity.getIntent().getExtras() != null) {
      if ((activity.getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
              != Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
        onNewIntent(activity.getIntent());
      }
    }
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    Log.d(TAG, "onDetachedFromActivityForConfigChanges");
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    Log.d(TAG, "onReattachedToActivityForConfigChanges");
    binding.addOnNewIntentListener(this);
    this.activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    Log.d(TAG, "onDetachedFromActivity");
    this.activity = null;
  }

  @Override
  public boolean onOpenUrl(InjectorSource source, String url) {
    Log.d(TAG, "onOpenUrl");
    return false;
  }

  @Override
  public boolean onDeepLink(InjectorSource source, String deepLink) {
    Log.d(TAG, "onDeepLink");
    return false;
  }

  @Override
  public boolean onNewIntent(Intent intent) {
    Log.d(TAG, "Synerise onNewIntent");
    if (intent == null || intent.getExtras() == null) {
      return false;
    }
    String data = intent.getDataString();
    if (data != null) {
      Log.d(TAG, data);
      channel.invokeMethod("onUrlOpen", data);
      lastData = data;
    }
    activity.setIntent(intent);
    return true;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "SyneriseMessagingReceiver: message received");
    if (intent.getExtras() == null) {
      Log.d(
              TAG,
              "broadcast received but intent contained no extras to process RemoteMessage. Operation cancelled.");
      return;
    }
    RemoteMessage remoteMessage = new RemoteMessage(intent.getExtras());
    Map<String, String> messageData = remoteMessage.getData();
    Log.d(TAG, "SyneriseMessagingReceiver messageData: " + messageData);
    try {
      boolean isSynerisePush = Injector.isSynerisePush(messageData);
      if (isSynerisePush) {
        PushThread thread = new PushThread(messageData);
        thread.start();
      }
    } catch (Exception e) {
      Synerise.settings.tracker.autoTracking.enabled = false;
      Synerise.settings.tracker.setMinimumBatchSize(11);
      Synerise.settings.tracker.setMaximumBatchSize(99);
      Synerise.settings.tracker.setAutoFlushTimeout(4999);
      Synerise.settings.injector.automatic = true;
      Synerise.settings.sdk.shouldDestroySessionOnApiKeyChange = true;
      Synerise.settings.notifications.setEncryption(true);
      Synerise.Builder.with((Application) context.getApplicationContext(), Keys.API_KEY, Keys.APP_ID)
              .mesaggingServiceType(MessagingServiceType.GMS)
              .syneriseDebugMode(true)
              .crashHandlingEnabled(true)
              .locationUpdateRequired(this)
              .initializationListener(this)
              .hostApplicationType(HostApplicationType.NATIVE_ANDROID)
              .build();
      boolean isSynerisePush = Injector.isSynerisePush(messageData);
      if (isSynerisePush) {
        PushThread thread = new PushThread(messageData);
        thread.start();
      }
    }
  }

  private void initSynerise(String apiKey, String appId) {
    Synerise.settings.tracker.autoTracking.enabled = false;
    Synerise.settings.tracker.setMinimumBatchSize(11);
    Synerise.settings.tracker.setMaximumBatchSize(99);
    Synerise.settings.tracker.setAutoFlushTimeout(4999);
    Synerise.settings.injector.automatic = true;
    Synerise.settings.sdk.shouldDestroySessionOnApiKeyChange = true;
    Synerise.settings.notifications.setEncryption(true);
    Synerise.Builder.with(activity.getApplication(), apiKey, appId)
            .mesaggingServiceType(MessagingServiceType.GMS)
            .syneriseDebugMode(true)
            .crashHandlingEnabled(true)
            .locationUpdateRequired(this)
            .initializationListener(this)
            .hostApplicationType(HostApplicationType.NATIVE_ANDROID)
            .build();
  }

  private void authorizeByOauth(String token, Result result) {
    Client.authenticate(token, ClientIdentityProvider.OAUTH, null, null, null)
            .execute(new OauthSuccessHandler(result), new OauthErrorHandler(result));
  }

  private void authorizeByOauthWithCheck(String token, Result result) {
    if (!Client.isSignedIn()) {
      Client.authenticate(token, ClientIdentityProvider.OAUTH, null, null, null)
              .execute(new OauthSuccessHandler(result), new OauthErrorHandler(result));
    }
  }

  private void trackScreenView(String name) {
    VisitedScreenEvent event = new VisitedScreenEvent(name);
    Tracker.send(event);
  }

  private void registerFcmToken(String token, Result result) {
    Log.d(TAG, "Refreshed token: " + token);

    IApiCall call = Client.registerForPush(token, true);
    call.execute(() -> result.success("Register for Push succeed: " + token),
            apiError -> result.success(Log.w(TAG, "Register for push failed: " + token)));
  }
}

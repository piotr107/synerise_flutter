package com.healthdom.synerise_flutter;

import android.app.Activity;

import androidx.annotation.NonNull;

import android.util.Log;

import com.healthdom.synerise_flutter.util.OauthErrorHandler;
import com.healthdom.synerise_flutter.util.OauthSuccessHandler;
import com.synerise.sdk.client.Client;
import com.synerise.sdk.client.model.ClientIdentityProvider;
import com.synerise.sdk.core.Synerise;
import com.synerise.sdk.core.listeners.OnLocationUpdateListener;
import com.synerise.sdk.core.listeners.SyneriseListener;
import com.synerise.sdk.core.net.IApiCall;
import com.synerise.sdk.core.types.enums.HostApplicationType;
import com.synerise.sdk.core.types.enums.MessagingServiceType;
import com.synerise.sdk.event.Tracker;
import com.synerise.sdk.event.model.interaction.VisitedScreenEvent;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** SyneriseFlutterPlugin */
public class SyneriseFlutterPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler, OnLocationUpdateListener, SyneriseListener {

  private MethodChannel channel;
  private Activity activity;
  private static final String TAG = SyneriseFlutterPlugin.class.getSimpleName();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "synerise_flutter");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "initSynerise":
        final String apiKey = call.argument("apiKey");
        final String appId = call.argument("appId");
        initSynerise(apiKey, appId);
        result.success("Synerise " + Synerise.getAppId());
        break;
      case "authorizeByOauth":
        final String token = call.arguments.toString();
        authorizeByOauth(token, result);
        break;
      case "trackScreenView":
        final String screenName = call.arguments.toString();
        trackScreenView(screenName);
        break;
      case "registerFcmToken":
        final String fcmToken = call.arguments.toString();
        registerFcmToken(fcmToken, result);
        break;
      default:
        result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
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

  @Override
  public void onLocationUpdateRequired() {

  }

  @Override
  public void onInitializationCompleted() {

  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

  }

  @Override
  public void onDetachedFromActivity() {

  }
}

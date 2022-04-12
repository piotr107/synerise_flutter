package com.healthdom.synerise_flutter;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import android.text.TextUtils;
import android.util.Log;

import com.synerise.sdk.client.Client;
import com.synerise.sdk.client.model.ClientIdentityProvider;
import com.synerise.sdk.core.Synerise;
import com.synerise.sdk.core.listeners.ActionListener;
import com.synerise.sdk.core.listeners.OnLocationUpdateListener;
import com.synerise.sdk.core.listeners.OnRegisterForPushListener;
import com.synerise.sdk.core.listeners.SyneriseListener;
import com.synerise.sdk.core.net.IApiCall;
import com.synerise.sdk.core.types.enums.HostApplicationType;
import com.synerise.sdk.core.types.enums.MessagingServiceType;
import com.synerise.sdk.core.types.enums.TrackMode;
import com.synerise.sdk.event.Tracker;
import com.synerise.sdk.event.model.interaction.VisitedScreenEvent;
import com.synerise.sdk.injector.callback.InjectorSource;
import com.synerise.sdk.injector.callback.OnInjectorListener;
import com.healthdom.synerise_flutter.util.FirebaseIdChangeBroadcastReceiver;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** SyneriseFlutterPlugin */
public class SyneriseFlutterPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler, OnRegisterForPushListener, OnLocationUpdateListener, SyneriseListener {

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
            .pushRegistrationRequired(this)
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

  @Override
  public void onLocationUpdateRequired() {

  }

  @Override
  public void onRegisterForPushRequired() {
    // your logic here
    FirebaseApp.initializeApp(activity.getApplicationContext());
    FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
      if (!TextUtils.isEmpty(token)) {
        Log.d(TAG, "Retrieve token Successful : " + token);
        IApiCall call = Client.registerForPush(token, true);
        call.execute(() -> Log.d(TAG, "Register for Push succeed: " + token),
                apiError -> Log.w(TAG, "Register for push failed: " + token));

        Intent intent = FirebaseIdChangeBroadcastReceiver.createFirebaseIdChangedIntent();
        LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);
      } else{
        Log.w(TAG, "token should not be null...");
      }
    });
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

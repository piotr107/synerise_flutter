package com.healthdom.synerise_flutter;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.synerise.sdk.core.Synerise;
import com.synerise.sdk.core.listeners.OnLocationUpdateListener;
import com.synerise.sdk.core.listeners.OnRegisterForPushListener;
import com.synerise.sdk.core.listeners.SyneriseListener;
import com.synerise.sdk.core.types.enums.HostApplicationType;
import com.synerise.sdk.core.types.enums.MessagingServiceType;
import com.synerise.sdk.core.types.enums.TrackMode;
import com.synerise.sdk.injector.callback.InjectorSource;
import com.synerise.sdk.injector.callback.OnInjectorListener;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** SyneriseFlutterPlugin */
public class SyneriseFlutterPlugin extends MultiDexApplication implements FlutterPlugin, MethodCallHandler, OnRegisterForPushListener, OnLocationUpdateListener, SyneriseListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "synerise_flutter");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("initSynerise")) {
      initSynerise();
      result.success("Synerise " + Synerise.getAppId());
    }
    else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  private void initSynerise() {
    String syneriseClientApiKey = "57f246e5-6d02-4a76-a95e-74cc9077d741";
    String appId = "ZdrowAppka (partner: Persooa)";
    Synerise.settings.tracker.autoTracking.trackMode = TrackMode.FINE;
    Synerise.settings.tracker.setMinimumBatchSize(11);
    Synerise.settings.tracker.setMaximumBatchSize(99);
    Synerise.settings.tracker.setAutoFlushTimeout(4999);
    Synerise.settings.injector.automatic = true;
    Synerise.settings.sdk.shouldDestroySessionOnApiKeyChange = true;
    Synerise.settings.notifications.setEncryption(true);

    Synerise.Builder.with(this, syneriseClientApiKey, appId)
            .mesaggingServiceType(MessagingServiceType.GMS)
            .syneriseDebugMode(true)
            .crashHandlingEnabled(true)
            .pushRegistrationRequired(this)
            .locationUpdateRequired(this)
            .initializationListener(this)
            .hostApplicationType(HostApplicationType.NATIVE_ANDROID)
            .build();
  }

  @Override
  public void onLocationUpdateRequired() {

  }

  @Override
  public void onRegisterForPushRequired() {

  }

  @Override
  public void onInitializationCompleted() {

  }
}

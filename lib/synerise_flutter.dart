import 'dart:async';

import 'package:flutter/services.dart';

class SyneriseFlutter {
  static const MethodChannel _channel = MethodChannel('synerise_flutter');

  static Future<String?> initSynerise(
      {required String apiKey, required String appId}) async {
    final String? version = await _channel
        .invokeMethod('initSynerise', {'apiKey': apiKey, 'appId': appId});
    return version;
  }

  static Future<String?> authorizeByOauth(String token) async {
    final String? response =
        await _channel.invokeMethod('authorizeByOauth', token);
    return response;
  }

  static Future<String?> registerFcmToken(String token) async {
    final String? response =
        await _channel.invokeMethod('registerFcmToken', token);
    return response;
  }

  static void trackScreenView(String name) {
    _channel.invokeMethod('trackScreenView', name);
  }
}

import 'dart:async';

import 'package:flutter/services.dart';

class SyneriseFlutter {
  factory SyneriseFlutter() {
    if (_instance == null) {
      const MethodChannel methodChannel = MethodChannel('synerise_flutter');
      _instance = SyneriseFlutter.private(methodChannel);
    }
    return _instance!;
  }

  SyneriseFlutter.private(this._channel) {
    _channel.setMethodCallHandler(_methodCallHandler);
  }

  static SyneriseFlutter? _instance;
  final MethodChannel _channel;

  final _onUrlOpen = StreamController<Uri>.broadcast();

  Stream<Uri> get onUrlOpen => _onUrlOpen.stream;

  Future<dynamic> _methodCallHandler(MethodCall call) async {
    switch (call.method) {
      case 'onUrlOpen':
        final url = call.arguments as String;
        final uri = Uri.tryParse(url);
        if (uri != null) {
          _onUrlOpen.add(uri);
        }
        break;
      default:
        throw 'Method ${call.method} not implemented on channel synerise_flutter';
    }
  }

  Future<String?> initSynerise(
      {required String apiKey,
      required String appId,
      String? appGroupIdentifier,
      String? keychainGroupIdentifier}) async {
    final String? result = await _channel.invokeMethod('initSynerise', {
      'apiKey': apiKey,
      'appId': appId,
      'appGroupIdentifier': appGroupIdentifier,
      'keychainGroupIdentifier': keychainGroupIdentifier
    });
    return result;
  }

  Future<String?> authorizeByOauth(String token) async {
    final String? response =
        await _channel.invokeMethod('authorizeByOauth', token);
    return response;
  }

  Future<String?> registerFcmToken(String token) async {
    final String? response =
        await _channel.invokeMethod('registerFcmToken', token);
    return response;
  }

  void trackScreenView(String name) {
    _channel.invokeMethod('trackScreenView', name);
  }

  void trackEvent(String action, String label, Map<String, String>? params) {
    var args = {
      'action': action,
      'label': label,
      'params': params
    };
    _channel.invokeMethod('trackEvent', args);
  }
}

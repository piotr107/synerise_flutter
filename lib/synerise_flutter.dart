import 'dart:async';
import 'dart:io';

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
  final _onAppOpen = StreamController<void>.broadcast();

  Stream<Uri> get onUrlOpen => _onUrlOpen.stream;
  Stream<void> get onAppOpen => _onAppOpen.stream;

  Future<dynamic> _methodCallHandler(MethodCall call) async {
    switch (call.method) {
      case 'onUrlOpen':
        final url = call.arguments as String;
        final uri = Uri.tryParse(url);
        if (uri != null) {
          _onUrlOpen.add(uri);
        }
        break;
      case 'onAppOpen2':
        _onAppOpen.add(null);
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
        await _channel.invokeMethod('registerFcmToken', token).toString();
    return response;
  }

  void trackScreenView(String name) {
    _channel.invokeMethod('trackScreenView', name);
  }

  Future<Uri?> getLastData() async {
    if (Platform.isIOS) {
      return null;
    }
    final url = await _channel.invokeMethod('getLastData');
    if (url == null) {
      return null;
    }
    final uri = Uri.tryParse(url);
    return uri;
  }

  void trackEvent(String action, String label, Map<String, String>? params) {
    var args = {'action': action, 'label': label, 'params': params};
    _channel.invokeMethod('trackEvent', args);
  }
}

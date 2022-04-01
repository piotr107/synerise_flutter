
import 'dart:async';

import 'package:flutter/services.dart';

class SyneriseFlutter {
  static const MethodChannel _channel = MethodChannel('synerise_flutter');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String?> get initSynerise async {
    final String? version = await _channel.invokeMethod('initSynerise');
    return version;
  }
}

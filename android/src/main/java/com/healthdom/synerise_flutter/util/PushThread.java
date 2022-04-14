package com.healthdom.synerise_flutter.util;

import com.synerise.sdk.injector.Injector;

import java.util.Map;

public class PushThread extends Thread {

    Map<String,String> messageData;
    public PushThread(Map<String, String> messageData) {this.messageData = messageData;}

    public void run() {
        Injector.handlePushPayload(messageData);
    }

}

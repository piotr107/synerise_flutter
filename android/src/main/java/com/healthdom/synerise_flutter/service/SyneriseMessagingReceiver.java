package com.healthdom.synerise_flutter.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.healthdom.synerise_flutter.util.PushThread;
import com.synerise.sdk.injector.Injector;

import java.util.Map;

public class SyneriseMessagingReceiver extends BroadcastReceiver {
    private static final String TAG = "SNRMessagingReceiver";

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
        Map<String,String> messageData = remoteMessage.getData();
        boolean isSynerisePush = Injector.isSynerisePush(messageData);
        if (isSynerisePush) {
            PushThread thread = new PushThread(messageData);
            thread.start();
        }
    }
}

package com.healthdom.synerise_flutter;

import com.synerise.sdk.core.listeners.ActionListener;

import io.flutter.plugin.common.MethodChannel;

public class OauthSuccessHandler implements ActionListener {

    private MethodChannel.Result result;

    public OauthSuccessHandler(MethodChannel.Result result) {
        this.result = result;
    }

    @Override
    public void onAction() {
        result.success("Oauth success");
    }
}

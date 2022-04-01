package com.healthdom.synerise_flutter;

import com.synerise.sdk.core.listeners.DataActionListener;
import com.synerise.sdk.error.ApiError;

import io.flutter.plugin.common.MethodChannel;

public class OauthErrorHandler implements DataActionListener<ApiError> {

    private MethodChannel.Result result;

    public OauthErrorHandler(MethodChannel.Result result) {
        this.result = result;
    }

    @Override
    public void onDataAction(ApiError error) {
        result.error(String.valueOf(error.getHttpCode()), error.getErrorBody().getMessage(), error.getErrorBody().getError());
    }
}

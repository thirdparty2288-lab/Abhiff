package com.ffabhi.app;

import android.telecom.Call;

public class CallScreeningService extends android.telecom.CallScreeningService {
    @Override
    public void onScreenCall(Call.Details callDetails) {
        CallResponse.Builder response = new CallResponse.Builder();
        response.setDisallowCall(true);
        response.setRejectCall(true);
        response.setSkipNotification(true);
        respondToCall(callDetails, response.build());
    }
}

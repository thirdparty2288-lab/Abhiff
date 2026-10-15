package com.ffabhi.app;

import android.telecom.Call;
import android.telecom.CallScreeningService;

public class CallScreeningService extends CallScreeningService {
    @Override
    public void onScreenCall(Call.Details callDetails) {
        CallResponse.Builder response = new CallResponse.Builder();
        response.setDisallowCall(true);
        response.setRejectCall(true);
        response.setSkipNotification(true);
        respondToCall(callDetails, response.build());
    }
}

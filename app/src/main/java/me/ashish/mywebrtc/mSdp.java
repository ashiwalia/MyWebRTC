package me.ashish.mywebrtc;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

/**
 * Created by ashishmac on 14/09/17.
 */

public class mSdp implements SdpObserver {
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d("MYTAG", "SdpObserver onCreateSuccess");
    }

    @Override
    public void onSetSuccess() {
        Log.d("MYTAG", "SdpObserver onSetSuccess");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d("MYTAG", "SdpObserver onCreateFailure");
    }

    @Override
    public void onSetFailure(String s) {
        Log.d("MYTAG", "SdpObserver onSetFailure");
    }
}

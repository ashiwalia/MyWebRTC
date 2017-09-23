package me.ashish.mywebrtc;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;

/**
 * Created by ashishmac on 15/09/17.
 */

public class MyIceObserver implements PeerConnection.Observer {
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d("JJJ", "onSignalingChange");
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d("JJJ", "onAddStream");
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d("JJJ", "onRemoveStream");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d("JJJ", "onDataChannel");
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d("JJJ", "onRenegotiationNeeded");
    }
}

package me.ashish.mywebrtc;


import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {


    private Dialog mDialog;
    private EditText mEditTextRoomName;
    private PeerConnection peerConnection;
    private String CURRENT_USER, ROOM_NAME;

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("server_url");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCreateOrJoinRoom();
        init();
        mSocket.connect();


    }


    private void initCreateOrJoinRoom() {
        mDialog = new Dialog(this, R.style.AppTheme);
        mDialog.setContentView(R.layout.first_screen);
        mEditTextRoomName = mDialog.findViewById(R.id.roomNameEditText);
        mDialog.setCancelable(false);
        mDialog.show();


    }


    public void buttonClicked(View view) {
        peerConnection.close();
    }

    public void createOrJoinRoom(View view) {
        if(mEditTextRoomName.getText().toString().isEmpty()){
            Toast.makeText(this, "Please enter a room's name to create or join.", Toast.LENGTH_SHORT).show();
            return;
        }
        view.setEnabled(false);
        JSONObject roomPayload = new JSONObject();
        try {
            roomPayload.put("roomName", mEditTextRoomName.getText().toString().toLowerCase().trim());
            roomPayload.put("socketId", mSocket.id());
            mSocket.emit("newRoomOrJoinOne", roomPayload);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        mSocket.on("newRoomOrJoinOneCallback", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray jsonArray = (JSONArray) args[0];
                            ((TextView)(mDialog.findViewById(R.id.textWaiting))).setText(jsonArray.getString(0));
                            mDialog.findViewById(R.id.textWaiting).setVisibility(View.VISIBLE);
                            mDialog.findViewById(R.id.inputHolder).setVisibility(View.GONE);
                            CURRENT_USER =  String.valueOf(jsonArray.getString(1));
                            ROOM_NAME = mEditTextRoomName.getText().toString().toLowerCase().trim();
                            ReceivedAnOffer();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

            }
        });

        mSocket.on("startHandshakeProcess", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                sendOffer(String.valueOf(args[0]));
            }
        });
    }











    private void sendOffer(final String roomName) {
        peerConnection.createOffer(new mSdp() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                peerConnection.setLocalDescription(new mSdp(), sessionDescription);
                try {


                    JSONObject payload = new JSONObject();
                    payload.put("roomName", roomName);
                    payload.put("type", sessionDescription.type.canonicalForm());
                    payload.put("sdp", sessionDescription.description);

                    mSocket.emit("sendOffer", payload);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new MediaConstraints());


        mSocket.on("receiveAnswer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject ReceivedData;
                try {
                    ReceivedData = new JSONObject(String.valueOf(args[0]));
                    SessionDescription sdp = new SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(ReceivedData.getString("type")),
                            ReceivedData.getString("sdp")
                    );
                    peerConnection.setRemoteDescription(new mSdp(), sdp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void ReceivedAnOffer() {

        mSocket.on("receiveOffer", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                JSONObject ReceivedData;

                try {
                    ReceivedData = new JSONObject(String.valueOf(args[0]));
                    SessionDescription sdp = new SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(ReceivedData.getString("type")),
                            ReceivedData.getString("sdp")
                    );
                    peerConnection.setRemoteDescription(new mSdp(), sdp);


                    peerConnection.createAnswer(new mSdp() {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {
                            super.onCreateSuccess(sessionDescription);
                            peerConnection.setLocalDescription(new mSdp(), sessionDescription);
                            sendAnswer(sessionDescription, String.valueOf(args[0]));
                        }
                    }, new MediaConstraints());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void sendAnswer(SessionDescription sessionDescription, String dataReceivedInOffer) {
        try {
            JSONObject ReceivedData = new JSONObject(dataReceivedInOffer);

            JSONObject payload = new JSONObject();
            payload.put("roomName", ReceivedData.getString("roomName"));
            payload.put("type", sessionDescription.type.canonicalForm());
            payload.put("sdp", sessionDescription.description);

            mSocket.emit("sendAnswer", payload);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        videoCapturer = createCameraCapturer();

        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer() {
        VideoCapturer videoCapturer = VideoCapturerAndroid.create(CameraEnumerationAndroid.getNameOfFrontFacingDevice());

        if (videoCapturer != null) {
            return videoCapturer;
        }

        VideoCapturer videoCapturere = VideoCapturerAndroid.create(CameraEnumerationAndroid.getNameOfBackFacingDevice());

        if (videoCapturere != null) {
            return videoCapturere;
        }

        return null;
    }


    private void init() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        PeerConnectionFactory peerConnectionFactory = new PeerConnectionFactory();
        peerConnectionFactory.setOptions(options);


        VideoCapturer videoCapturerAndroid = createVideoCapturer();

        MediaConstraints constraints = new MediaConstraints();
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid, new MediaConstraints());
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack("videoTrack", videoSource);

        final AudioSource audioSource = peerConnectionFactory.createAudioSource(constraints);
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack("audioTrack", audioSource);

        AudioManager audioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        audioManager.setSpeakerphoneOn(true);

        final SurfaceViewRenderer videoView = (SurfaceViewRenderer) findViewById(R.id.glSurfaceView);
        videoView.setMirror(true);


        EglBase rootEglBase = EglBase.create();
        videoView.init(rootEglBase.getEglBaseContext(), null);

        MediaConstraints sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveVideo", "true"));

        List<PeerConnection.IceServer> iceServers = new ArrayList<>();

        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, sdpConstraints, new MyIceObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                sendIceCandidates(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                final VideoTrack videoTrack = mediaStream.videoTracks.getFirst();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            videoTrack.addRenderer(new VideoRenderer(videoView));
                            mDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });


        MediaStream stream = peerConnectionFactory.createLocalMediaStream("localMediaStream");
        stream.addTrack(localAudioTrack);
        stream.addTrack(localVideoTrack);
        peerConnection.addStream(stream);


    }


    private void sendIceCandidates(IceCandidate iceCandidate) {
        JSONObject iceCandidateData = new JSONObject();
        try {
            iceCandidateData.put("roomName", ROOM_NAME);
            iceCandidateData.put("toUser", getUserToSendIce());
            iceCandidateData.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
            iceCandidateData.put("sdpMid", iceCandidate.sdpMid);
            iceCandidateData.put("candidate", iceCandidate.sdp);

            mSocket.emit("sendICE", iceCandidateData);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        mSocket.on("setICE", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    setIceCandidates(String.valueOf(args[0]));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setIceCandidates(String data) throws JSONException {
        JSONObject ReceivedData = new JSONObject(data);
        IceCandidate candidate = new IceCandidate(
                ReceivedData.getString("sdpMid"),
                ReceivedData.getInt("sdpMLineIndex"),
                ReceivedData.getString("candidate")
        );
        peerConnection.addIceCandidate(candidate);

    }


    private String getUserToSendIce(){
        return CURRENT_USER.matches("userOne") ? "userSecond" : "userOne";
    }

}


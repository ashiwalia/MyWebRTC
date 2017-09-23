# WebRTC for Android

This app is a demo video chat app based on **WebRTC** for Android. This is a fully functional demo. I have also set up a basic Node.js server for signaling and information exchange using Socket.io

The demo is supposed to be an introduction to WebRTC workings in Android.


### How to use the app
- Set up a `Node.js` server using  `signaling_server.js` file. To get started you can use [AWS Free Tier](https://aws.amazon.com/free/)

- In `MainActivity.java` on line number 53 replace `server_url` with yours.
 ```
try {
       mSocket = IO.socket("server_url");
     } catch (URISyntaxException e) {
            e.printStackTrace();
     }
 ```

- Now just run the app and create a chat room or join one.

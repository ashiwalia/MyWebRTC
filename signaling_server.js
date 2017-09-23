var express = require('express');
var socket = require('socket.io');



var app = express();
var server = app.listen(8080, function(){
    console.log('listening for requests on port 8080,');
});


app.use(express.static('public'));

var activeRooms = [];


var io = socket(server);
io.on('connection', (mSocket) => {
    console.log('made socket connection', mSocket.id);

    mSocket.on("newRoomOrJoinOne", function(data){
        if(activeRooms.length == 0){
            var userOneSocketID = data.socketId;
            delete data.socketId;
            data.userOne = userOneSocketID;
            activeRooms.push(data);
            mSocket.emit("newRoomOrJoinOneCallback", ["Waiting for another user to join.", "userOne"]);
            console.log("NEW ROOM CREATED = %j", activeRooms);
        }else{
            var isRoomAvailable = false;
            for (var i = 0; i < activeRooms.length; i++) {
                if(activeRooms[i].roomName == data.roomName){
                    activeRooms[i].userSecond = data.socketId;
                    console.log("A USER JOINED THE ROOM= %j", activeRooms);
                    isRoomAvailable = true;
                    mSocket.emit("newRoomOrJoinOneCallback", ["Connecting to another user.", "userSecond"]);
                    io.to(activeRooms[i].userOne).emit("startHandshakeProcess", activeRooms[i].roomName);
                    break;
                }
    
            }
            if(!isRoomAvailable){
                var userOneSocketID = data.socketId;
                delete data.socketId;
                data.userOne = userOneSocketID;
                activeRooms.push(data);
                mSocket.emit("newRoomOrJoinOneCallback", ["Waiting for another user to join.", "userOne"]);
                console.log("NEW ROOM ADDED = %j" + activeRooms);
            }
            

        }
        
    });



    mSocket.on("sendOffer", function(data){
        console.log("RECEIVING OFFER");
        for (var i = 0; i < activeRooms.length; i++) {
            if(activeRooms[i].roomName == data.roomName){
                io.to(activeRooms[i].userSecond).emit("receiveOffer", data);
                console.log("OFFER SENT");
            }

        }
    });

    mSocket.on("sendAnswer",function(data){
        console.log("SENDING ANSWER");
        for (var i = 0; i < activeRooms.length; i++) {
            if(activeRooms[i].roomName == data.roomName){
                io.to(activeRooms[i].userOne).emit("receiveAnswer", data);
                console.log("ANSWER SENT");
            }

        }
    });

    mSocket.on("sendICE", function(data){
        console.log("SENDING ICE");
        for (var i = 0; i < activeRooms.length; i++) {
            if(activeRooms[i].roomName == data.roomName){
                var user = data.toUser;
                console.log(user);
                console.log(activeRooms[i][user]);
                io.to(activeRooms[i][user]).emit("setICE", data);
                console.log("ICE SENT");
            }

        }    
    });


   
    

});

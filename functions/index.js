//#Note - messageIds must be unique otherwise two notifications with same message Id will overlap

const functions = require('firebase-functions');

const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sos = functions.database.ref('sosDetails/{sosId}').onCreate((snapshot, context) => {

	//get the sosId
	const sosId = context.params.sosId;
	//get the latitude
	const userLat = snapshot.child('lat').val();
	//get the longitude
	const userLng = snapshot.child('lng').val();
	//get the userId
	const userId = snapshot.child('userId').val();
	//tokens
	var tokens = [];
	//receiverIds
	var receiverIds = [];

	console.log("userId: " + userId);


	//define the message id. We'll be sending this in the payload
	const messageId = (Math.floor(Math.random() * 100000)).toString();
		

	//send the notification to all users within 3km radius of the user
	return admin.database().ref("userLocation").orderByKey().once('value').then(snapshot => {
		var dist = 0.0;
		var lat = 0.0;
		var lng = 0.0;

		snapshot.forEach(snap => {

			lat = snap.child('lat').val();	
			lng = snap.child('lng').val();

			dist = distance(userLat, userLng, lat, lng);

			if(userId !== snap.key && dist <= 3000.0){
				receiverIds.push(snap.key)
			}

			console.log("lat: " + lat);
			console.log("lng: " + lng);
			console.log("receiverId: " + snap.key);
			console.log("distance: " + dist);

		});

		console.log("length: ", receiverIds.length);

			//get the token of the user receiving the message and the dsiplayName of the user who is unsafe
			return admin.database().ref("users").orderByKey().once('value').then(snap => { 
				snap.forEach(childSnap => {
					var receiverId = childSnap.key;
					console.log("key: ", receiverId);
					if(receiverIds.includes(receiverId)){
						var token = childSnap.child('messagingToken').val();
						if(token !== null){
							tokens.push(token);
							console.log("token: " + token);
					}
					
					}
				});
				
			const displayName = snap.child(userId).child('name').val();
			const title = displayName + " is unsafe!";
			console.log("displayName: " + displayName);
			
			//we have everything we need
			//Build the message payload and send the message
			console.log("Construction the notification message.");
			const payload = {
				data: {
					data_type: "sos",
					data_message_id: messageId,
					data_title: title,
					data_user_id: userId
				}
			};
			
			return admin.messaging().sendToDevice(tokens, payload)
						.then(function(response) {
							console.log("Successfully sent message:", response); 
						  })
						  .catch(function(error) {
							console.log("Error sending message:", error);
						  });
			});

		});
});

exports.safe = functions.database.ref('sosDetails/{sosId}').onUpdate((change, context) => {
	//get the sosId
	const sosId = context.params.sosId;
	//get the latitude
	const userLat = change.after.child('lat').val();
	//get the longitude
	const userLng = change.after.child('lng').val();
	//get the userId
	const userId = change.after.child('userId').val();
	//get the isSafe value
	var isSafe = "";
	if(change.after.child('isSafe').exists()){
	isSafe = change.after.child('isSafe').val();
	}
	//tokens
	var tokens = [];
	//receiverIds
	var receiverIds = [];

	//define the message id. We'll be sending this in the payload
	const messageId = (Math.floor(Math.random() * 100000)).toString();
		
		if(isSafe){
	//send the notification to all users within 3km radius of the user
	return admin.database().ref("userLocation").orderByKey().once('value').then(snapshot => {
		var dist = 0.0;
		var lat = 0.0;
		var lng = 0.0;

		snapshot.forEach(snap => {

			lat = snap.child('lat').val();	
			lng = snap.child('lng').val();

			dist = distance(userLat, userLng, lat, lng);

			if(userId !== snap.key && dist <= 3000.0){
				receiverIds.push(snap.key)
			}

			console.log("lat: " + lat);
			console.log("lng: " + lng);
			console.log("receiverId: " + snap.key);
			console.log("distance: " + dist);

		});

		console.log("length: ", receiverIds.length);

			//get the token of the user receiving the message and the dsiplayName of the user who is unsafe
			return admin.database().ref("users").orderByKey().once('value').then(snap => { 
				snap.forEach(childSnap => {
					var receiverId = childSnap.key;
					console.log("key: ", receiverId);
					if(receiverIds.includes(receiverId)){
						var token = childSnap.child('messagingToken').val();
						if(token !== null){
							tokens.push(token);
							console.log("token: " + token);
					}
					
					}
				});
				
			const displayName = snap.child(userId).child('name').val();
			const title = displayName + " is now safe";
			console.log("displayName: " + displayName);
			
			//we have everything we need
			//Build the message payload and send the message
			console.log("Construction the notification message.");
			const payload = {
				data: {
					data_type: "safe",
					data_message_id: messageId,
					data_title: title,
					data_user_id: userId
				}
			};
			
			return admin.messaging().sendToDevice(tokens, payload)
						.then(function(response) {
							console.log("Successfully sent message:", response); 
						  })
						  .catch(function(error) {
							console.log("Error sending message:", error);
						  });
			});

		});
}
else{
	return null;
}
});

function distance(lat1, lon1, lat2, lon2){
	theta = lon1 - lon2;
	dist = Math.sin(deg2rad(lat1))
			* Math.sin(deg2rad(lat2))
			+ Math.cos(deg2rad(lat1))
			* Math.cos(deg2rad(lat2))
			* Math.cos(deg2rad(theta));
	dist = Math.acos(dist);
	dist = rad2deg(dist);
	dist = dist * 60 * 1.1515; //In Miles
	dist = dist/0.62137;
	dist = dist * 1000; //In metres

	return dist;
}

function deg2rad(deg){
	return (deg * Math.PI/180.0);
}

function rad2deg(rad){
	return (rad * 180/Math.PI);
}
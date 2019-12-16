const functions = require('firebase-functions');
const admin = require('firebase-admin')
admin.initializeApp(functions.config().firebase)
var db = admin.firestore();

exports.helloWorld = functions.https.onRequest((request, response) => {
	response.send("Hello from Firebase!");
});

exports.registerUsage = functions.https.onRequest((request, response) => {
    let now = new Date();
	let month = now.getFullYear().toString(10) + (now.getMonth()+1).toString(10);
	let v = request.query.version;
	let ipAddress = request.headers['x-forwarded-for'] || req.connection.remoteAddress;
	console.log(ipAddress);
	let loc = request.query.locale;
	let os_str = request.query.os;
	let docRef = db.collection('usage_log').doc('on_launch').collection(month);
	let addLog = docRef.add({
	    ip: ipAddress,
	    version: v,
	    locale: loc,
	    os: os_str,
	    launch_date: now
	}).then(docRef => {
        console.log('Usage added.');
    });
	response.send("RouteMapMaker Responce.");
});

exports.testFunc = functions.https.onRequest((request, response) => {
    let now = new Date();
	let month = now.getFullYear().toString(10) + (now.getMonth()+1).toString(10);
	console.log(month);
	let v = request.query.version;
	let ipAddress = request.headers['x-forwarded-for'] || req.connection.remoteAddress;
	console.log(ipAddress);
	let loc = request.query.locale;
	let os_str = request.query.os;
	let docRef = db.collection('usage_log').doc('test').collection(month);
	docRef.add({
	    ip: ipAddress,
	    version: v,
	    locale: loc,
	    os: os_str,
	    launch_date: now
	}).then(docRef => {
        console.log('Usage added.');
    });
	response.send("RouteMapMaker Responce.");
})
external fun require(module:String):dynamic
external val module:dynamic

val functions = require("firebase-functions");
val admin = require("firebase-admin");

fun main(args: Array<String>) {
    admin.initializeApp();
}
val doEveryDay = functions.pubsub.topic("daily-tick").onPublish { _ ->
    console.log("Daily tick happened!")
}
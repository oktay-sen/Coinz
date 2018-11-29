(function (_, Kotlin) {
  'use strict';
  var Unit = Kotlin.kotlin.Unit;
  var functions;
  var admin;
  function main(args) {
    admin.initializeApp();
  }
  function doEveryDay$lambda(f) {
    console.log('Daily tick happened!');
    return Unit;
  }
  var doEveryDay;
  Object.defineProperty(_, 'functions', {
    get: function () {
      return functions;
    }
  });
  Object.defineProperty(_, 'admin', {
    get: function () {
      return admin;
    }
  });
  _.main_kand9s$ = main;
  Object.defineProperty(_, 'doEveryDay', {
    get: function () {
      return doEveryDay;
    }
  });
  functions = require('firebase-functions');
  admin = require('firebase-admin');
  doEveryDay = functions.pubsub.topic('daily-tick').onPublish(doEveryDay$lambda);
  main([]);
  Kotlin.defineModule('index', _);
  return _;
}(module.exports, require('kotlin')));

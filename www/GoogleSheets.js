var GoogleSheets = function() {};

GoogleSheets.prototype.signIn = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "GoogleSheets", "signIn", []);
};

GoogleSheets.prototype.signOut = function() {
  // exec();
};

GoogleSheets.prototype.getSpreadsheet = function(
    spreadsheetId, spreadsheetRange, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "GoogleSheets", "getSpreadsheet",
               [ spreadsheetId, spreadsheetRange ]);
};

GoogleSheets.prototype.updateSpreadsheetValues = function(
    spreadsheetId, spreadsheetRange, valuesToUpdate, successCallback,
    errorCallback) {
  cordova.exec(successCallback, errorCallback, "GoogleSheets",
               "updateSpreadsheetValues",
               [ spreadsheetId, spreadsheetRange, valuesToUpdate ]);
};

GoogleSheets.prototype.updateSpreadsheetCell = function(
    spreadsheetId, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "GoogleSheets", "updateCell",
               []);
};

GoogleSheets.prototype.isUserSignedIn =
    function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "GoogleSheets",
               "isUserSignedIn");
}

    GoogleSheets.install = function() {
  if (!cordova.plugins) {
    cordova.plugins = {};
  }
  cordova.plugins.googlesheets = new GoogleSheets();
  return cordova.plugins.googlesheets;
};

module.exports = (function() { return new GoogleSheets(); })();

cordova.addConstructor(GoogleSheets.install);

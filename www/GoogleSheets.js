var SheetsOperations = (function() {
  var Operations = function() {};
  Operations.prototype.copyTo = function(success, error, spreadsheetId, sheetId,
                                         destinationSpreadsheetId,
                                         standardParams) {
    cordova.exec(
        success, error, 'GoogleSheets', 'sheetsCopyTo',
        [ spreadsheetId, sheetId, destinationSpreadsheetId, standardParams ]);
  };
  return Operations;
})();

var DeveloperMetadataOperations = (function() {
  var Operations = function() {};
  Operations.prototype.get = function(success, error) {
    cordova.exec(success, error, 'GoogleSheets', 'developerMetadataGet', []);
  };

  Operations.prototype.search = function(success, error) {
    cordova.exec(success, error, 'GoogleSheets', 'developerMetadataSearch', []);
  };

  return Operations;
})();

var ValuesOperations = (function() {
  var Operations = function() {};
  Operations.prototype.append = function(success, error, spreadsheetId, range,
                                         values, valueInputOption,
                                         insertDataOption) {
    cordova.exec(
        success, error, 'GoogleSheets', 'valuesAppend',
        [ spreadsheetId, range, values, valueInputOption, insertDataOption ]);
  };
  Operations.prototype.batchClear = function(success, error, spreadsheetId,
                                             ranges) {
    cordova.exec(success, error, 'GoogleSheets', 'valuesBatchClear',
                 [ spreadsheetId, ranges ]);
  };
  Operations.prototype.batchClearByDataFilter = function(success, error) {
    cordova.exec(success, error, 'GoogleSheets', 'valuesBatchClearByDataFilter',
                 []);
  };
  Operations.prototype.batchGet = function(
      success, error, spreadsheetId, ranges, majorDimension, valueRenderOption,
      dateTimeRenderOption, standardParams) {
    cordova.exec(success, error, 'GoogleSheets', 'valuesBatchGet', [
      spreadsheetId, ranges, majorDimension, valueRenderOption,
      dateTimeRenderOption, standardParams
    ]);
  };
  Operations.prototype.batchGetByDataFilter = function(success, error) {
    cordova.exec(success, error, 'GoogleSheets', 'valuesBatchGetByDataFilter',
                 []);
  };
  Operations.prototype.batchUpdate = function(success, error, spreadsheetId,
                                              data) {
    cordova.exec(success, error, 'GoogleSheets', 'valuesBatchUpdate',
                 [ spreadsheetId, data ]);
  };
  Operations.prototype.batchUpdateByDataFilter = function(success, error) {
    cordova.exec(success, error, 'GoogleSheets',
                 'valuesBatchUpdateByDataFilter', []);
  };
  Operations.prototype.clear = function(success, error, spreadsheetId, range) {
    cordova.exec(success, error, 'GoogleSheets', 'valuesClear',
                 [ spreadsheetId, range ]);
  };
  Operations.prototype.get = function(success, error, spreadsheetId, range) {
    cordova.exec(success, error, 'GoogleSheets', 'valuesGet',
                 [ spreadsheetId, range ]);
  };
  Operations.prototype.update = function(success, error, spreadsheetId, range, values, valueInputOption) {
    cordova.exec(success, error, 'GoogleSheets', 'valuesUpdate', [spreadsheetId, range, values, valueInputOption]);
  };
  return Operations;
})();

var SpreadsheetsOperations = (function() {
  var Operations = function() {
    this.sheets = new SheetsOperations();
    this.developerMetadata = new DeveloperMetadataOperations();
    this.values = new ValuesOperations();
  };

  Operations.prototype.batchUpdate = function(success, error) {
    cordova.exec(success, error, 'GoogleSheets', 'spreadsheetsBatchUpdate', []);
  };

  Operations.prototype.create = function(success, error, spreadsheet) {
    cordova.exec(success, error, 'GoogleSheets', 'spreadsheetsCreate', [spreadsheet]);
  }

  Operations.prototype.get = function(success, error, spreadsheetId, ranges, includeGridData) {
    cordova.exec(success, error, 'GoogleSheets', 'spreadsheetsGet', [spreadsheetId, ranges, includeGridData]);
  };

  Operations.prototype.getByDataFilter = function(success, error) {
    cordova.exec(success, error, 'GoogleSheets', 'spreadsheetsGetByDataFilter', []);
  }

  return Operations;
})();

var GoogleSheets = (function() {
  var Operations = function() {
    this.spreadsheets = new SpreadsheetsOperations();
  };

  Operations.prototype.signIn = function(success, error) {
    cordova.exec(success, error, 'GoogleSheets', 'signIn', []);
  };

  Operations.prototype.signOut = function(success, error) {
    // exec();
  };

  Operations.prototype.isUserSignedIn = function(success, error) {
    cordova.exec(success, error, 'GoogleSheets', 'isUserSignedIn');
  };

  Operations.install = function() {
    if (!cordova.plugins) {
      cordova.plugins = {};
    }
    cordova.plugins.GoogleSheets = new GoogleSheets();
    return cordova.plugins.GoogleSheets;

  };
  return Operations;
})();

module.exports = (function() { return new GoogleSheets(); })();

cordova.addConstructor(GoogleSheets.install);

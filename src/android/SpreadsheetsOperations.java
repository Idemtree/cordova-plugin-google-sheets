package org.sumaq.plugins.googlesheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import java.util.ArrayList;
import java.util.List;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONObject;

public class SpreadsheetsOperations extends OperationsProvider {
  private GoogleSheets mPlugin;
  private static SpreadsheetsOperations mInstance;

  private SpreadsheetsOperations(GoogleSheets plugin) {
    mPlugin = plugin;
  }

  public static SpreadsheetsOperations getInstance(GoogleSheets plugin) {
    if (mInstance == null) {
      mInstance = new SpreadsheetsOperations(plugin);
    }
    return mInstance;
  }

  public Operation batchUpdate(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        mPlugin.handle(
            this,
            context,
            new UnsupportedOperationException(
                "SpreadsheetsOperations' batchUpdate has not been implemented yet"));
      }
    };
  }

  public Operation create(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        try {
          JSONObject spreadsheetJson = params.getJSONObject(0);
          String spreadsheetTitle = spreadsheetJson.getString("title");
          String spreadsheetLocale = spreadsheetJson.getString("locale");
          String spreadsheetAutoRecalc = spreadsheetJson.optString("autoRecalc", null);
          String spreadsheetTimeZone = spreadsheetJson.optString("timeZone", null);

          Spreadsheet requestBody = new Spreadsheet();
          SpreadsheetProperties properties = new SpreadsheetProperties();
          Sheets sheetsService = mPlugin.getService();

          if (!isCordovaNullable(spreadsheetTitle)) {
            properties.setTitle(spreadsheetTitle);
          }

          if (!isCordovaNullable(spreadsheetLocale)) {
            properties.setLocale(spreadsheetLocale);
          }

          if (!isCordovaNullable(spreadsheetAutoRecalc)) {
            properties.setAutoRecalc(spreadsheetAutoRecalc);
          }

          if (!isCordovaNullable(spreadsheetTimeZone)) {
            properties.setTimeZone(spreadsheetTimeZone);
          }

          requestBody.setProperties(properties);
          Sheets.Spreadsheets.Create request = sheetsService.spreadsheets().create(requestBody);

          Spreadsheet response = request.execute();

          context.success(response.toString());

        } catch (Exception exception) {
          mPlugin.handle(this, context, exception);
        }
      }
    };
  }

  public Operation get(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        try {
          String spreadsheetId = params.getString(0);
          JSONArray rangesArray = params.getJSONArray(1);
          boolean includeGridData = params.getBoolean(2);

          List<String> ranges = new ArrayList<>();

          Sheets sheetsService = mPlugin.getService();
          Sheets.Spreadsheets.Get request = sheetsService.spreadsheets().get(spreadsheetId);
          request.setRanges(ranges);
          request.setIncludeGridData(includeGridData);

          Spreadsheet response = request.execute();

          context.success(response.toString());

        } catch (Exception exception) {
          mPlugin.handle(this, context, exception);
        }
      }
    };
  }

  public Operation getByDataFilter(final JSONArray params, final CallbackContext context) {
    // TODO implement this.
    return new Operation() {
      @Override
      public void run() {
        mPlugin.handle(
            this,
            context,
            new UnsupportedOperationException(
                "SpreadsheetsOperations' getByDataFilter has not been implemented yet"));
      }
    };
  }
}

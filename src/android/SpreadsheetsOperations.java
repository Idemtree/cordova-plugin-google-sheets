package org.sumaq.plugins.googlesheets;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.Runnable;
import java.util.ArrayList;
import java.util.List;

public class SpreadsheetsOperations {
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

  public Runnable batchUpdate(final JSONArray params) {
    return new Runnable() {
      public void run() {
        // TODO: implement batcUpdate and request interfaces
        mPlugin.getCallbackContext().error("not implemented yet");
      }
    };
  }

  public Runnable create(final JSONArray params) {
    return new Runnable() {
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

          if (spreadsheetTitle != null && spreadsheetTitle != "null") {
            properties.setTitle(spreadsheetTitle);
          }

          if (spreadsheetLocale != null && spreadsheetLocale != "null") {
            properties.setLocale(spreadsheetLocale);
          }

          if (spreadsheetAutoRecalc != null && spreadsheetLocale != "null") {
            properties.setAutoRecalc(spreadsheetAutoRecalc);
          }
          
          if (spreadsheetTimeZone != null && spreadsheetTimeZone != "null") {
            properties.setTimeZone(spreadsheetTimeZone);
          }

          requestBody.setProperties(properties);
          Sheets.Spreadsheets.Create request = sheetsService.spreadsheets().create(requestBody);

          Spreadsheet response = request.execute();

          mPlugin.getCallbackContext().success(response.toString());

        } catch (UserRecoverableAuthIOException authExcept) {
          mPlugin.requestAuthorization(authExcept, this);
        } catch (Exception e) {
          mPlugin.getCallbackContext().error(e.getMessage());
        }
      }
    };
  }

  public Runnable get(final JSONArray params) {
    return new Runnable() {
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

          mPlugin.getCallbackContext().success(response.toString());

        } catch (UserRecoverableAuthIOException authExcept) {
          mPlugin.requestAuthorization(authExcept, this);
        } catch (Exception e) {
          mPlugin.getCallbackContext().error(e.getMessage());
        }
      }
    };
  }

  public Runnable getByDataFilter(final JSONArray params) {
    //TODO implement this.
    return new Runnable() {
      public void run() {
        mPlugin.getCallbackContext().error("Not implemented yet...");
      }
    };
  }
}

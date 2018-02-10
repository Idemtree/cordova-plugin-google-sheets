package org.sumaq.plugins.googlesheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.CopySheetToAnotherSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.SheetProperties;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;

public class SheetsOperations extends OperationsProvider {
  private GoogleSheets mPlugin;
  private static SheetsOperations mInstance;

  private SheetsOperations(GoogleSheets plugin) {
    mPlugin = plugin;
  }

  public static SheetsOperations getInstance(GoogleSheets plugin) {
    if (mInstance == null) {
      mInstance = new SheetsOperations(plugin);
    }
    return mInstance;
  }

  public Operation copyTo(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        try {
          String spreadsheetId = params.getString(0);
          int sheetId = Integer.parseInt(params.getString(1));
          String destinationSpreadsheetId = params.getString(2);
          CopySheetToAnotherSpreadsheetRequest requestBody =
              new CopySheetToAnotherSpreadsheetRequest();
          requestBody.setDestinationSpreadsheetId(destinationSpreadsheetId);

          Sheets sheetsService = mPlugin.getService();
          Sheets.Spreadsheets.SheetsOperations.CopyTo request =
              sheetsService.spreadsheets().sheets().copyTo(spreadsheetId, sheetId, requestBody);

          SheetProperties response = request.execute();
          context.success(response.toString());
        } catch (Exception exception) {
          mPlugin.handle(this, context, exception);
        }
      }
    };
  }
}

package org.sumaq.plugins.googlesheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchClearValuesRequest;
import com.google.api.services.sheets.v4.model.BatchClearValuesResponse;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.util.ArrayList;
import java.util.List;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONObject;

public class ValuesOperations extends OperationsProvider {
  private GoogleSheets mPlugin;
  private static ValuesOperations mInstance;
  public static String STR_DEFAULT_MAJOR_DIMENSION_OPT = "ROWS";
  public static String DEFAULT_VALUE_INPUT_OPTION = "USER_ENTERED";
  public static String DEFAULT_INSERT_DATA_OPTION = "INSERT_ROWS";
  public static String DEFAULT_VALUE_RENDER_OPTION = "FORMATTED_VALUE";
  public static String DEFAULT_DATE_TIME_RENDER_OPTION = "";

  private ValuesOperations(GoogleSheets plugin) {
    mPlugin = plugin;
  }

  public static ValuesOperations getInstance(GoogleSheets plugin) {
    if (mInstance == null) {
      mInstance = new ValuesOperations(plugin);
    }
    return mInstance;
  }

  public Operation append(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        try {
          String spreadsheetId = params.getString(0);
          String range = params.getString(1);
          JSONArray valuesArray = params.getJSONArray(2);
          List<List<Object>> valuesList = new ArrayList<>();
          String valueInputOption = params.getString(3);
          String insertDataOption = params.getString(4);
          ValueRange requestBody = new ValueRange();

          for (int rowNumber = 0; rowNumber < valuesArray.length(); rowNumber++) {
            JSONArray rowArray = valuesArray.getJSONArray(rowNumber);
            List<Object> rowList = new ArrayList<>();
            for (int colNumber = 0; colNumber < rowArray.length(); colNumber++) {
              rowList.add(rowArray.getString(colNumber));
            }
            valuesList.add(rowList);
          }

          requestBody.setValues(valuesList);

          Sheets sheetsService = mPlugin.getService();
          Sheets.Spreadsheets.Values.Append request =
              sheetsService.spreadsheets().values().append(spreadsheetId, range, requestBody);
          if (isCordovaNullable(valueInputOption)) {
            valueInputOption = ValuesOperations.DEFAULT_VALUE_INPUT_OPTION;
          }
          if (isCordovaNullable(insertDataOption)) {
            insertDataOption = ValuesOperations.DEFAULT_INSERT_DATA_OPTION;
          }
          request.setValueInputOption(valueInputOption);
          request.setInsertDataOption(insertDataOption);

          AppendValuesResponse response = request.execute();
          context.success(response.toString());
        } catch (Exception exception) {
          mPlugin.handle(this, context, exception);
        }
      }
    };
  }

  public Operation batchClear(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        try {
          String spreadsheetId = params.getString(0);
          JSONArray rangesArray = params.getJSONArray(1);
          List<String> rangesList = new ArrayList<>();

          for (int index = 0; index < rangesArray.length(); index++) {
            rangesList.add(rangesArray.getString(index));
          }

          BatchClearValuesRequest requestBody = new BatchClearValuesRequest();
          requestBody.setRanges(rangesList);

          Sheets sheetsService = mPlugin.getService();
          Sheets.Spreadsheets.Values.BatchClear request =
              sheetsService.spreadsheets().values().batchClear(spreadsheetId, requestBody);

          BatchClearValuesResponse response = request.execute();
          context.success(response.toString());
        } catch (Exception exception) {
          mPlugin.handle(this, context, exception);
        }
      }
    };
  }

  public Operation batchClearByDataFilter(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        /*TODO: implement data filter mechanisms.
        try {
        } catch (Exception exception) {
        }
        */
        mPlugin.handle(
            this,
            context,
            new UnsupportedOperationException(
                "ValuesOperations' batchClearByDataFilter has not been implemented yet"));
      }
    };
  }

  public Operation batchGet(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        try {
          String spreadsheetId = params.getString(0);
          JSONArray spreadsheetRanges = params.getJSONArray(1);
          String majorDimension = params.getString(2);
          String valueRenderOption = params.getString(3);
          String dateTimeRenderOption = params.getString(4);
          List<String> rangesList = new ArrayList<String>();

          for (int i = 0; i < spreadsheetRanges.length(); i++) {
            rangesList.add(spreadsheetRanges.getString(i));
          }

          Sheets sheetsService = mPlugin.getService();
          Sheets.Spreadsheets.Values.BatchGet request =
              sheetsService.spreadsheets().values().batchGet(spreadsheetId);
          request.setRanges(rangesList);
          if (!isCordovaNullable(valueRenderOption)) {
            request.setValueRenderOption(valueRenderOption);
          }
          if (!isCordovaNullable(dateTimeRenderOption)) {
            request.setDateTimeRenderOption(dateTimeRenderOption);
          }
          BatchGetValuesResponse response = request.execute();
          context.success(response.toString());
        } catch (Exception exception) {
          mPlugin.handle(this, context, exception);
        }
      }
    };
  }

  public Operation batchGetByDataFilter(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        /* TODO: implement data filter mechanisms.
        try {
        } catch (Exception exception) {
        }
        */
        mPlugin.handle(
            this,
            context,
            new UnsupportedOperationException(
                "ValuesOperations' batchClearByDataFilter has not been implemented yet"));
      }
    };
  }

  public Operation batchUpdate(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        try {
          String spreadsheetId = params.getString(0);
          JSONArray dataArray = params.getJSONArray(1);
          List<ValueRange> data = new ArrayList<>();

          for (int index = 0; index < dataArray.length(); index++) {
            JSONObject valueObject = dataArray.getJSONObject(index);
            JSONArray valuesArray = valueObject.getJSONArray("values");
            ValueRange valueRange = new ValueRange();
            List<List<Object>> values = new ArrayList<>();
            valueRange.setRange(valueObject.getString("range"));

            for (int rowNumber = 0; rowNumber < valuesArray.length(); rowNumber++) {
              JSONArray rowArray = valuesArray.getJSONArray(rowNumber);
              List<Object> rowList = new ArrayList<>();
              for (int columnNumber = 0; columnNumber < rowArray.length(); columnNumber++) {
                rowList.add(rowArray.getString(columnNumber));
              }
              values.add(rowList);
            }

            valueRange.setValues(values);
            data.add(valueRange);
          }

          BatchUpdateValuesRequest requestBody = new BatchUpdateValuesRequest();
          requestBody
              .setValueInputOption(ValuesOperations.DEFAULT_VALUE_INPUT_OPTION)
              .setData(data);

          Sheets sheetsService = mPlugin.getService();
          Sheets.Spreadsheets.Values.BatchUpdate request =
              sheetsService.spreadsheets().values().batchUpdate(spreadsheetId, requestBody);

          BatchUpdateValuesResponse response = request.execute();

          context.success(response.toString());

        } catch (Exception exception) {
          mPlugin.handle(this, context, exception);
        }
      }
    };
  }

  public Operation clear(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        try {
          String spreadsheetId = params.getString(0);
          String range = params.getString(1);

          ClearValuesRequest requestBody = new ClearValuesRequest();

          Sheets sheetsService = mPlugin.getService();
          Sheets.Spreadsheets.Values.Clear request =
              sheetsService.spreadsheets().values().clear(spreadsheetId, range, requestBody);

          ClearValuesResponse response = request.execute();
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
          String range = params.getString(1);
          Sheets sheetsService = mPlugin.getService();
          Sheets.Spreadsheets.Values.Get request =
              sheetsService.spreadsheets().values().get(spreadsheetId, range);
          request.setValueRenderOption(ValuesOperations.DEFAULT_VALUE_RENDER_OPTION);

          ValueRange response = request.execute();
          context.success(response.toString());
        } catch (Exception exception) {
          mPlugin.handle(this, context, exception);
        }
      }
    };
  }

  public Operation update(final JSONArray params, final CallbackContext context) {
    return new Operation() {
      @Override
      public void run() {
        try {
          String spreadsheetId = params.getString(0);
          String range = params.getString(1);
          JSONArray valuesArray = params.getJSONArray(2);
          String valueInputOption = params.getString(3);
          List<List<Object>> values = new ArrayList<>();
          ValueRange requestBody = new ValueRange();

          for (int rowNumber = 0; rowNumber < valuesArray.length(); rowNumber++) {
            JSONArray rowArray = valuesArray.getJSONArray(rowNumber);
            List<Object> rowList = new ArrayList<>();
            for (int columnNumber = 0; columnNumber < rowArray.length(); columnNumber++) {
              rowList.add(rowArray.getString(columnNumber));
            }
            values.add(rowList);
          }

          requestBody.setValues(values);
          Sheets sheetsService = mPlugin.getService();
          Sheets.Spreadsheets.Values.Update request =
              sheetsService.spreadsheets().values().update(spreadsheetId, range, requestBody);
          request.setValueInputOption(ValuesOperations.DEFAULT_VALUE_INPUT_OPTION);

          UpdateValuesResponse response = request.execute();
          context.success(response.toString());
        } catch (Exception exception) {
          mPlugin.handle(this, context, exception);
        }
      }
    };
  }
}

package org.sumaq.plugins.googlesheets;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import java.util.HashMap;
import java.util.Map;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

public class GoogleSheets extends CordovaPlugin {
  private static final String MSG_REQUEST_GOOGLE_PLAY_SERVICES = "google_play_services_request";
  private static final String MSG_REQUEST_ACCOUNT_PERMISSION = "account_permission_request";
  private static final String OPT_SIGN_IN = "signIn";
  private static final String OPT_SIGN_OUT = "signOut";
  private static final String OPT_IS_SIGNED_IN = "isUserSignedIn";
  private static final String OPT_SPREADSHEETS_BATCH_UPDATE = "batchUpdate";
  private static final String OPT_SPREADSHEETS_CREATE = "spreadsheetsCreate";
  private static final String OPT_SPREADSHEETS_GET = "spreadsheetsGet";
  private static final String OPT_SPREADSHEETS_GET_DATA_FILTER = "spreadsheetsGetByDataFilter";
  private static final String OPT_SHEETS_COPY_TO = "sheetsCopyTo";
  private static final String OPT_DEVELOPER_METADATA_GET = "developerMetadataGet";
  private static final String OPT_DEVELOPER_METADATA_SEARCH = "developerMetadataSearch";
  private static final String OPT_VALUES_APPEND = "valuesAppend";
  private static final String OPT_VALUES_BATCH_GET = "valuesBatchGet";
  private static final String OPT_VALUES_BATCH_CLEAR = "valuesBatchClear";
  private static final String OPT_VALUES_BATCH_CLEAR_DATA_FILTER = "valuesBatchClearByDataFilter";
  private static final String OPT_VALUES_BATCH_GET_DATA_FILTER = "valuesBatchGetByDataFilter";
  private static final String OPT_VALUES_BATCH_UPDATE = "valuesBatchUpdate";
  private static final String OPT_VALUES_BATCH_UPDATE_DATA_FILTER = "valuesBatchUpdateByDataFilter";
  private static final String OPT_VALUES_CLEAR = "valuesClear";
  private static final String OPT_VALUES_GET = "valuesGet";
  private static final String OPT_VALUES_UPDATE = "valuesUpdate";
  static final int REQUEST_ACCOUNT_PICKER = 1000;
  static final int REQUEST_AUTHORIZATION = 1001;
  static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
  static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
  private static final String PREF_ACCOUNT_NAME = "accountName";
  public static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};
  private static String mApplicationName = null;
  private AccountOperations mAccountOperations;
  private SheetsOperations mSheetsOperations;
  private ValuesOperations mValuesOperations;
  private SpreadsheetsOperations mSpreadsheetsOperations;
  private Map<String, Operation> mInterruptedTasks;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    mInterruptedTasks = new HashMap<String, Operation>();

    if (mAccountOperations == null) {
      mAccountOperations = AccountOperations.getInstance(this);
    }

    if (mSheetsOperations == null) {
      mSheetsOperations = SheetsOperations.getInstance(this);
    }
    if (mValuesOperations == null) {
      mValuesOperations = ValuesOperations.getInstance(this);
    }
    if (mSpreadsheetsOperations == null) {
      mSpreadsheetsOperations = SpreadsheetsOperations.getInstance(this);
    }
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
      throws JSONException {
    Operation runnable = null;
    boolean result = false;

    switch (action) {
      case OPT_SIGN_IN:
        runnable = mAccountOperations.signIn(callbackContext);
        result = true;
        break;
      case OPT_SIGN_OUT:
        runnable = mAccountOperations.signOut(callbackContext);
        result = true;
        break;
      case OPT_IS_SIGNED_IN:
        runnable = mAccountOperations.isUserSignedIn(callbackContext);
        result = true;
        break;
      case OPT_SPREADSHEETS_BATCH_UPDATE:
        runnable = mSpreadsheetsOperations.batchUpdate(args, callbackContext);
        result = true;
        break;
      case OPT_SPREADSHEETS_CREATE:
        runnable = mSpreadsheetsOperations.create(args, callbackContext);
        result = true;
        break;
      case OPT_SPREADSHEETS_GET:
        runnable = mSpreadsheetsOperations.get(args, callbackContext);
        result = true;
        break;
      case OPT_SPREADSHEETS_GET_DATA_FILTER:
        runnable = mSpreadsheetsOperations.getByDataFilter(args, callbackContext);
        result = true;
        break;
      case OPT_SHEETS_COPY_TO:
        result = true;
        runnable = mSheetsOperations.copyTo(args, callbackContext);
        break;
      case OPT_DEVELOPER_METADATA_GET:
        result = true;
        break;
      case OPT_DEVELOPER_METADATA_SEARCH:
        result = true;
        break;
      case OPT_VALUES_APPEND:
        result = true;
        runnable = mValuesOperations.append(args, callbackContext);
        break;
      case OPT_VALUES_BATCH_GET:
        result = true;
        runnable = mValuesOperations.batchGet(args, callbackContext);
        break;
      case OPT_VALUES_BATCH_CLEAR:
        result = true;
        runnable = mValuesOperations.batchClear(args, callbackContext);
        break;
      case OPT_VALUES_BATCH_CLEAR_DATA_FILTER:
        result = true;
        runnable = mValuesOperations.batchClearByDataFilter(args, callbackContext);
        break;
      case OPT_VALUES_BATCH_GET_DATA_FILTER:
        result = true;
        break;
      case OPT_VALUES_BATCH_UPDATE:
        result = true;
        runnable = mValuesOperations.batchUpdate(args, callbackContext);
        break;
      case OPT_VALUES_BATCH_UPDATE_DATA_FILTER:
        result = true;
        break;
      case OPT_VALUES_CLEAR:
        result = true;
        runnable = mValuesOperations.clear(args, callbackContext);
        break;
      case OPT_VALUES_GET:
        result = true;
        runnable = mValuesOperations.get(args, callbackContext);
        break;
      case OPT_VALUES_UPDATE:
        result = true;
        runnable = mValuesOperations.update(args, callbackContext);
        break;
    }
    if (runnable != null) {
      executeOnBackground(runnable);
    }
    return result;
  }

  private void executeOnBackground(Operation task) {
    if (!isGooglePlayServicesAvailable()) {
      acquireGooglePlayServices();
      cordova.getThreadPool().execute(task);
    } else if (!isDeviceOnline()) {
    } else {
      cordova.getThreadPool().execute(task);
    }
  }

  private String getStringResource(String resourceName) {
    return cordova.getActivity().getString(
        cordova.getActivity().getResources().getIdentifier(resourceName, "string", cordova.getActivity().getPackageName()));
  }

  /** Handles results from activities started from here. */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_ACCOUNT_PICKER:
        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
          String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
          String callbackId = data.getStringExtra(Operation.INTERRUPTED_OPERATION);
          if (accountName != null) {
            SharedPreferences settings = cordova.getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(PREF_ACCOUNT_NAME, accountName);
            editor.apply();
          }
          if (callbackId != null) {
            Operation task = mInterruptedTasks.get(callbackId);
            if (task != null) {
              mInterruptedTasks.remove(task);
              executeOnBackground(task);
            }
          }
        }
        break;
      case REQUEST_GOOGLE_PLAY_SERVICES:
        if (resultCode != Activity.RESULT_OK) {
          String message =
              String.format(getStringResource(MSG_REQUEST_GOOGLE_PLAY_SERVICES), mApplicationName);
          Toast.makeText(cordova.getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } else {
        }
        break;
      case REQUEST_AUTHORIZATION:
        if (resultCode == Activity.RESULT_OK) {
          String callbackId = data.getStringExtra(Operation.INTERRUPTED_OPERATION);
          if (callbackId != null) {
            Operation interruptedTask = mInterruptedTasks.get(callbackId);
            if (interruptedTask != null) {
              mInterruptedTasks.remove(interruptedTask);
              executeOnBackground(interruptedTask);
            }
          }
        }
        break;
    }
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
      throws JSONException {
    switch (requestCode) {
      case REQUEST_PERMISSION_GET_ACCOUNTS:
        Operation task = mInterruptedTasks.get(AccountOperations.ACCOUNT_PERMISSION_INTERRUPTED);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          if (task != null) {
            mInterruptedTasks.remove(task);
            executeOnBackground(task);
          }
        } else {
          if (task != null) {
            mInterruptedTasks.remove(task);
          }
        }
        break;
    }
  }

  /**
   * Checks whether the device currently has a network connection.
   *
   * @return true if the device has a network connection, false otherwise.
   */
  private boolean isDeviceOnline() {
    ConnectivityManager connMgr =
        (ConnectivityManager) cordova.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return (networkInfo != null && networkInfo.isConnected());
  }

  /**
   * Check that Google Play services APK is installed and up to date.
   *
   * @return true if Google Play Services is available and up to date on this device; false
   *     otherwise.
   */
  private boolean isGooglePlayServicesAvailable() {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(cordova.getActivity());
    return connectionStatusCode == ConnectionResult.SUCCESS;
  }

  /**
   * Attempt to resolve a missing, out-of-date, invalid or disabled Google Play Services
   * installation via a user dialog, if possible.
   */
  private void acquireGooglePlayServices() {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(cordova.getActivity());
    if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
      showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
    }
  }

  /**
   * Display an error dialog showing that Google Play Services is missing or out of date.
   *
   * @param connectionStatusCode code describing the presence (or lack of) Google Play Services on
   *     this device.
   */
  void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    Dialog dialog =
        apiAvailability.getErrorDialog(
            cordova.getActivity(), connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
    dialog.show();
  }

  /** Returns true if the user already granted permission to access their contacts. */
  public boolean hasAccountPermissions() {
    return cordova.hasPermission(Manifest.permission.GET_ACCOUNTS);
  }

  public Sheets getService() throws UserNotSignedIn {
    return mAccountOperations.getService();
  }

  public void handle(Operation task, CallbackContext context, Exception except) {
    if (except instanceof UserRecoverableAuthIOException) {
      String callbackId = context.getCallbackId();
      mInterruptedTasks.put(callbackId, task);
      startActivityForResult(
          ((UserRecoverableAuthIOException) except)
              .getIntent()
              .putExtra(Operation.INTERRUPTED_OPERATION, callbackId),
          REQUEST_AUTHORIZATION);
    } else {
      context.error(findErrorMessage(except));
    }
  }

  public void askForAccountPermission(Operation task) {
    mInterruptedTasks.put(AccountOperations.ACCOUNT_PERMISSION_INTERRUPTED, task);
    cordova.requestPermission(
        this, REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
  }

  private void startActivityForResult(Intent intent, int requestCode) {
    cordova.startActivityForResult(this, intent, requestCode);
  }

  public void chooseAccount(Operation task, CallbackContext context, Intent intent) {
    String callbackId = context.getCallbackId();
    mInterruptedTasks.put(callbackId, task);
    startActivityForResult(
        intent.putExtra(Operation.INTERRUPTED_OPERATION, callbackId), REQUEST_ACCOUNT_PICKER);
  }

  public CordovaInterface getCordova() {
    return cordova;
  }

  private String findErrorMessage(Throwable e) {
    String errMessage;
    if (e.getMessage() != null && e.getMessage() != "") {
      errMessage = e.getMessage();
    } else {
      if (e.getCause() != null) {
        errMessage = findErrorMessage(e.getCause());
      } else {
        errMessage = "No helpful message found in exception.getMessage()";
      }
    }
    return errMessage;
  }
}

package org.sumaq.plugins.googlesheets;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import java.lang.Runnable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

public class GoogleSheets extends CordovaPlugin {
  private static final String TAG = "GOOGLE-SHEETS PLUGIN";
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
  private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};
  private static String mApplicationName = null;
  private String mAccountName;
  private GoogleAccountCredential mCredential;
  private Activity mActivity;
  private CallbackContext mCallbackContext;
  private com.google.api.services.sheets.v4.Sheets mService = null;
  private SheetsOperations mSheetsOperations;
  private ValuesOperations mValuesOperations;
  private SpreadsheetsOperations mSpreadsheetsOperations;
  private Queue<Runnable> mPausedTasks;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    mActivity = cordova.getActivity();
    mApplicationName =
        mApplicationName == null
            ? getApplicationName(mActivity.getApplicationContext())
            : mApplicationName;

    mAccountName =
        mActivity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
    mCredential =
        GoogleAccountCredential.usingOAuth2(
                mActivity.getApplicationContext(), Arrays.asList(SCOPES))
            .setBackOff(new ExponentialBackOff());

    mPausedTasks = new LinkedList<>();

    if (mAccountName != null && mAccountName != "") {
      mCredential.setSelectedAccountName(mAccountName);
      mService = this.buildClient();
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

    Log.d(TAG, "@initialize()");
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
      throws JSONException {
    mCallbackContext = callbackContext;
    Runnable runnable = null;
    boolean result = false;

    switch (action) {
      case OPT_SIGN_IN:
        this.signIn();
        result = true;
        break;
      case OPT_SIGN_OUT:
        result = true;
        break;
      case OPT_IS_SIGNED_IN:
        this.isUserSignedIn();
        result = true;
        break;
      case OPT_SPREADSHEETS_BATCH_UPDATE:
        runnable = mSpreadsheetsOperations.batchUpdate(args);
        result = true;
        break;
      case OPT_SPREADSHEETS_CREATE:
	runnable = mSpreadsheetsOperations.create(args);
        result = true;
        break;
      case OPT_SPREADSHEETS_GET:
        runnable = mSpreadsheetsOperations.get(args);
        result = true;
        break;
      case OPT_SPREADSHEETS_GET_DATA_FILTER:
        runnable = mSpreadsheetsOperations.getByDataFilter(args);
        result = true;
        break;
      case OPT_SHEETS_COPY_TO:
        result = true;
        runnable = mSheetsOperations.copyTo(args);
        break;
      case OPT_DEVELOPER_METADATA_GET:
        result = true;
        break;
      case OPT_DEVELOPER_METADATA_SEARCH:
        result = true;
        break;
      case OPT_VALUES_APPEND:
        result = true;
        runnable = mValuesOperations.append(args);
        break;
      case OPT_VALUES_BATCH_GET:
        result = true;
        runnable = mValuesOperations.batchGet(args);
        break;
      case OPT_VALUES_BATCH_CLEAR:
        result = true;
        runnable = mValuesOperations.batchClear(args);
        break;
      case OPT_VALUES_BATCH_CLEAR_DATA_FILTER:
        result = true;
        runnable = mValuesOperations.batchClearByDataFilter(args);
        break;
      case OPT_VALUES_BATCH_GET_DATA_FILTER:
        result = true;
        break;
      case OPT_VALUES_BATCH_UPDATE:
        result = true;
        runnable = mValuesOperations.batchUpdate(args);
        break;
      case OPT_VALUES_BATCH_UPDATE_DATA_FILTER:
        result = true;
        break;
      case OPT_VALUES_CLEAR:
        result = true;
        runnable = mValuesOperations.clear(args);
        break;
      case OPT_VALUES_GET:
        result = true;
        runnable = mValuesOperations.get(args);
        break;
      case OPT_VALUES_UPDATE:
        result = true;
        runnable = mValuesOperations.update(args);
        break;
    }
    if (runnable != null) {
      executeOnBackground(runnable);
    }
    return result;
  }

  public Sheets buildClient() {
    return new com.google.api.services.sheets.v4.Sheets.Builder(
            AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), mCredential)
        .setApplicationName(mApplicationName)
        .build();
  }

  public String getApplicationName(Context context) {
    ApplicationInfo appInfo = context.getApplicationInfo();
    int stringId = appInfo.labelRes;
    String appName =
        stringId == 0 ? appInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    return appName;
  }

  private void signIn() {
    if (hasAccountPermissions()) {
      if (mAccountName == null) {
        chooseAccount();
      } else if (mService == null) {
        mCredential.setSelectedAccountName(mAccountName);
        mService = buildClient();
        mCallbackContext.success("signed in as " + mAccountName);
      }
    } else {
      askForAccountPermission();
    }
    String values = String.format("mAccountName: %s", mAccountName);
    Log.d(TAG, "@signIn() " + values);
  }

  private void signOut(String message, CallbackContext callbackContext) {
    if (message != null && message.length() > 0) {
      callbackContext.success(message);
    } else {
      callbackContext.error("Expected one non-empty string argument.");
    }
  }

  private GoogleSheets getSelfReference() {
    return this;
  }

  private void executeOnBackground(Runnable task) {
    if (!isGooglePlayServicesAvailable()) {
      acquireGooglePlayServices();
      cordova.getThreadPool().execute(task);
    } else if (!isDeviceOnline()) {
    } else if (mCredential.getSelectedAccountName() == null) {
    } else {
      cordova.getThreadPool().execute(task);
    }
  }

  /** Returns true if the user already granted permission to access their contacts. */
  public boolean hasAccountPermissions() {
    return cordova.hasPermission(Manifest.permission.GET_ACCOUNTS);
  }

  /**
   * Attempts to set the account used with the API credentials. If an account name was previously
   * saved it will use that one; otherwise an account picker dialog will be shown to the user. Note
   * that the setting the account to use with the credentials object requires the app to have the
   * GET_ACCOUNTS permission, which is requested here if it is not already present.
   */
  private void chooseAccount() {

    if (hasAccountPermissions()) {
      mAccountName =
          mActivity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
      if (mAccountName == null) {
        // Start a dialog from which the user can choose an account
        cordova.startActivityForResult(
            this, mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
      } else {
        mCredential.setSelectedAccountName(mAccountName);
      }
    } else {
      askForAccountPermission();
    }
    Log.d(TAG, "@chooseAccount() mAccountName: " + mAccountName);
  }

  private void askForAccountPermission() {
    cordova.requestPermission(
        this, REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
  }

  private String getStringResource(String resourceName) {
    return mActivity.getString(
        mActivity.getResources().getIdentifier(resourceName, "string", mActivity.getPackageName()));
  }

  /** Handles results from activities started from here. */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_ACCOUNT_PICKER:
        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
          String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
          if (accountName != null) {
            SharedPreferences settings = mActivity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(PREF_ACCOUNT_NAME, accountName);
            editor.apply();
            mCredential.setSelectedAccountName(accountName);
            mService = buildClient();
            mCallbackContext.success("signed in as" + accountName);
          }
        }
        break;
      case REQUEST_GOOGLE_PLAY_SERVICES:
        if (resultCode != Activity.RESULT_OK) {
          String message =
              String.format(getStringResource(MSG_REQUEST_GOOGLE_PLAY_SERVICES), mApplicationName);
          Toast.makeText(mActivity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } else {
        }
        break;
      case REQUEST_AUTHORIZATION:
        if (resultCode == Activity.RESULT_OK) {
          if (!mPausedTasks.isEmpty()) {
            executeOnBackground(mPausedTasks.remove());
          }
        } else {
          mPausedTasks = new LinkedList<>();
        }
        break;
    }
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
      throws JSONException {
    switch (requestCode) {
      case REQUEST_PERMISSION_GET_ACCOUNTS:
        chooseAccount();
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
        (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mActivity);
    return connectionStatusCode == ConnectionResult.SUCCESS;
  }

  /**
   * Attempt to resolve a missing, out-of-date, invalid or disabled Google Play Services
   * installation via a user dialog, if possible.
   */
  private void acquireGooglePlayServices() {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mActivity);
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
            mActivity, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
    dialog.show();
  }

  public void isUserSignedIn() {
    if (mAccountName != null && mAccountName.length() != 0) {
      mCallbackContext.success(mAccountName);
    } else {
      mCallbackContext.error("No user signed in");
    }
  }

  public Sheets getService() {
    if (mService == null) {
      mService = this.buildClient();
    }
    return mService;
  }

  public CallbackContext getCallbackContext() {
    return mCallbackContext;
  }

  public void requestAuthorization(UserRecoverableAuthIOException except, Runnable task) {
    if (except instanceof UserRecoverableAuthIOException) {
      mPausedTasks.add(task);
      cordova.startActivityForResult(
          getSelfReference(), (except).getIntent(), REQUEST_AUTHORIZATION);
    }
  }
}

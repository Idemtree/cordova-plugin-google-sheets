package org.sumaq.plugins;

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
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.jdeferred.DeferredFutureTask;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class GoogleSheets extends CordovaPlugin implements EasyPermissions.PermissionCallbacks {
  private static final String TAG = "GOOGLE-SHEETS PLUGIN";
  private static final String MSG_REQUEST_GOOGLE_PLAY_SERVICES = "google_play_services_request";
  private static final String MSG_REQUEST_ACCOUNT_PERMISSION = "account_permission_request";
  private static final String OPT_SIGN_IN = "signIn";
  private static final String OPT_SIGN_OUT = "signOut";
  private static final String OPT_GET_SHEET = "getSpreadsheet";
  private static final String OPT_UPDATE_SHEET = "updateSpreadsheetValues";
  private static final String OPT_UPDATE_CELL = "updateCell";
  private static final String OPT_IS_SIGNED_IN = "isUserSignedIn";
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
  private AndroidDeferredObject mDeferredSignIn;
  private AndroidDeferredObject mDeferredBuildClient;
  private CallbackContext mCallbackContext;
  private com.google.api.services.sheets.v4.Sheets mService = null;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    mActivity = cordova.getActivity();
    mDeferredBuildClient = new AndroidDeferredObject();
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

    if (this.isUserSignedIn()) {
      mCredential.setSelectedAccountName(mAccountName);
    }

    Log.d(TAG, "@initialize()");
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
      throws JSONException {
    Log.d(TAG, "@execute()");
    mCallbackContext = callbackContext;
    if (action.equals(OPT_SIGN_IN)) {
      this.signIn();
      return true;
    } else if (action.equals(OPT_SIGN_OUT)) {
      String message = args.getString(0);
      this.signOut(message, callbackContext);
      return true;
    } else if (action.equals(OPT_GET_SHEET)) {
      String spreadsheetId = args.getString(0);
      String spreadsheetRange = args.getString(1);
      this.fetchSpreadsheetData(spreadsheetId, spreadsheetRange);
      return true;
    } else if (action.equals(OPT_UPDATE_SHEET)) {
      String spreadsheetId = args.getString(0);
      String spreadsheetRange = args.getString(1);
      String spreadsheetValues = args.getString(2);
      updateSpreadsheetValues(spreadsheetId, spreadsheetRange, spreadsheetValues);
      return true;
    } else if (action.equals(OPT_UPDATE_CELL)) {
      return true;
    } else if (action.equals(OPT_IS_SIGNED_IN)) {
      this.isUserSignedIn(callbackContext);
      return true;
    }
    return false;
  }

  private Promise trySignIn() {
    mDeferredSignIn = new AndroidDeferredObject();
    return mDeferredSignIn
        .promise()
        .done(
            new DoneCallback() {
              public void onDone(Object result) {
                mService =
                    buildClient(
                        AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance());
              }
            });
  }

  public Sheets buildClient(HttpTransport transport, JsonFactory jsonFactory) {
    return new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, mCredential)
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
      trySignIn()
          .then(
              new DoneCallback() {
                public void onDone(Object result) {
                  mCallbackContext.success(mAccountName);
                }
              })
          .fail(
              new FailCallback() {
                public void onFail(Object result) {
                  mCallbackContext.error("Could not sign In");
                }
              });
      if (mAccountName == null) {
        chooseAccount();
      } else {
        mDeferredSignIn.resolve("");
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

  public void fetchSpreadsheetData(final String spreadsheetId, final String spreadsheetRange) {
    if (spreadsheetId != null && spreadsheetId.length() > 0) {
      Callable callable =
          new Callable<String>() {
            public String call() throws Exception {
              ValueRange response =
                  mService.spreadsheets().values().get(spreadsheetId, spreadsheetRange).execute();
              return response.toString();
            }
          };
      executeOnBackground(callable)
          .then(
              new DoneCallback() {
                public void onDone(Object result) {
                  mCallbackContext.success((String) result);
                }
              })
          .fail(
              new FailCallback<Exception>() {
                public void onFail(Exception e) {
                  mCallbackContext.error("an error man");
                  if (e instanceof UserRecoverableAuthIOException) {
                    cordova.startActivityForResult(
                        getSelfReference(),
                        ((UserRecoverableAuthIOException) e).getIntent(),
                        REQUEST_AUTHORIZATION);
                  }
                }
              });
    } else {
      mCallbackContext.error("Expected one non-empty string argument.");
    }
  }

  private GoogleSheets getSelfReference() {
    return this;
  }

  private Promise executeOnBackground(Callable callable) {
    DeferredFutureTask task = new DeferredFutureTask(callable);
    Promise promise = task.promise();

    if (!isGooglePlayServicesAvailable()) {
      acquireGooglePlayServices();
      task.cancel(true);
    } else if (!isDeviceOnline()) {
      task.cancel(true);
    } else if (mCredential.getSelectedAccountName() == null) {
      task.cancel(true);
    } else {
      cordova.getThreadPool().execute(task);
    }
    return promise;
  }

  private boolean hasAccountPermissions() {
    return EasyPermissions.hasPermissions(mActivity, Manifest.permission.GET_ACCOUNTS);
  }

  /**
   * Attempts to set the account used with the API credentials. If an account name was previously
   * saved it will use that one; otherwise an account picker dialog will be shown to the user. Note
   * that the setting the account to use with the credentials object requires the app to have the
   * GET_ACCOUNTS permission, which is requested here if it is not already present. The
   * AfterPermissionGranted annotation indicates that this function will be rerun automatically
   * whenever the GET_ACCOUNTS permission is granted.
   */
  @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
  private void chooseAccount() {

    if (hasAccountPermissions()) {
      mAccountName =
          mActivity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
      if (mAccountName == null) {
        // Start a dialog from which the user can choose an account
        cordova.startActivityForResult(
            this, mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        // getResultsFromApi();
      } else {
        mCredential.setSelectedAccountName(mAccountName);
      }
    } else {
      askForAccountPermission();
    }
    Log.d(TAG, "@chooseAccount() mAccountName: " + mAccountName);
  }

  private void askForAccountPermission() {
    String message =
        String.format(getStringResource(MSG_REQUEST_ACCOUNT_PERMISSION), mApplicationName);
    EasyPermissions.requestPermissions(
        mActivity, message, REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
  }

  private String getStringResource(String resourceName) {
    return mActivity.getString(
        mActivity.getResources().getIdentifier(resourceName, "string", mActivity.getPackageName()));
  }

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
          }
          mDeferredSignIn.resolve("");
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
    }
  }

  /**
   * Respond to requests for permissions at runtime for API 23 and above.
   *
   * @param requestCode The request code passed in requestPermissions(android.app.Activity, String,
   *     int, String[])
   * @param permissions The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions which is either
   *     PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
   */
  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    this.onRequestPermissionsResult(requestCode, permissions, grantResults);
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  /**
   * Callback for when a permission is granted using the EasyPermissions library.
   *
   * @param requestCode The request code associated with the requested permission
   * @param list The requested permission list. Never null.
   */
  @Override
  public void onPermissionsGranted(int requestCode, List<String> list) {
    // Do nothing.
  }

  /**
   * Callback for when a permission is denied using the EasyPermissions library.
   *
   * @param requestCode The request code associated with the requested permission
   * @param list The requested permission list. Never null.
   */
  @Override
  public void onPermissionsDenied(int requestCode, List<String> list) {
    // Do nothing.
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

  private void updateSpreadsheetValues(
      final String spreadsheetId, final String spreadsheetRange, final String valuesJsonLiteral) {
    Callable updateValuesCallable =
        new Callable<String>() {
          public String call() throws Exception {
            Sheets.Spreadsheets.Values.Update request;
            ValueRange requestBody = new ValueRange();
            JSONObject requestBodyJsonObject;
            JSONObject valuesJsonObject;

            valuesJsonObject = new JSONObject(valuesJsonLiteral);
            JSONArray valuesArray = valuesJsonObject.optJSONArray("values");
            List<List<Object>> requestValues = new ArrayList();

            for (int rowIndex = 0; rowIndex < valuesArray.length(); rowIndex++) {
              List<Object> row = new ArrayList();
              JSONArray rowArray = valuesArray.optJSONArray(rowIndex);
              for (int columnIndex = 0; columnIndex < rowArray.length(); columnIndex++) {
                row.add(columnIndex, rowArray.get(columnIndex));
              }
              requestValues.add(rowIndex, row);
            }

            requestBody.setValues(requestValues);

            request =
                mService
                    .spreadsheets()
                    .values()
                    .update(spreadsheetId, spreadsheetRange, requestBody)
                    .setValueInputOption("USER_ENTERED");
            UpdateValuesResponse response = request.execute();
            return response.toString();
          }
        };

    executeOnBackground(updateValuesCallable)
        .then(
            new DoneCallback<String>() {
              public void onDone(String result) {
                mCallbackContext.success(result);
              }
            })
        .fail(
            new FailCallback<Exception>() {
              public void onFail(Exception e) {
                mCallbackContext.error("Could not perform update");
                if (e instanceof UserRecoverableAuthIOException) {
                  cordova.startActivityForResult(
                      getSelfReference(),
                      ((UserRecoverableAuthIOException) e).getIntent(),
                      REQUEST_AUTHORIZATION);
                }
              }
            });
  }

  public boolean isUserSignedIn() {
    if (mAccountName != null && mAccountName.length() != 0) {
      return true;
    } else {
      return false;
    }
  }

  public void isUserSignedIn(CallbackContext callbacContext) {
    if (mAccountName != null && mAccountName.length() != 0) {
      callbackContext.success(mAccountName);
    } else {
      callbackContext.error("No user signed in");
    }
  }
}

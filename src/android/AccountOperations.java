package org.sumaq.plugins.googlesheets;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import java.util.Arrays;
import org.apache.cordova.CallbackContext;

public class AccountOperations extends OperationsProvider {
  public static final String ACCOUNT_PERMISSION_INTERRUPTED = "accountPermissionInterrupted";
  private static final String PREF_ACCOUNT_NAME = "accountName";
  private static AccountOperations mInstance;
  private GoogleSheets mPlugin;
  private GoogleAccountCredential mCredential;
  private String mApplicationName;
  private String mAccountName;
  private Activity mActivity;
  private com.google.api.services.sheets.v4.Sheets mService = null;

  private AccountOperations(GoogleSheets plugin) {
    mPlugin = plugin;

    mApplicationName =
        mApplicationName == null
            ? getApplicationName(mPlugin.getCordova().getActivity().getApplicationContext())
            : "";

    mAccountName =
        mPlugin
            .getCordova()
            .getActivity()
            .getPreferences(Context.MODE_PRIVATE)
            .getString(PREF_ACCOUNT_NAME, null);

    setCredential();

    if (mAccountName != null && mAccountName != "") {
      mCredential.setSelectedAccountName(mAccountName);
      mService = buildClient();
    }
  }

  private void setCredential() {
    mCredential =
        GoogleAccountCredential.usingOAuth2(
                mPlugin.getCordova().getActivity().getApplicationContext(),
                Arrays.asList(GoogleSheets.SCOPES))
            .setBackOff(new ExponentialBackOff());
  }

  public static AccountOperations getInstance(GoogleSheets plugin) {
    if (mInstance == null) {
      mInstance = new AccountOperations(plugin);
    }
    return mInstance;
  }

  public Operation signIn(final CallbackContext callbackContext) {
    return new Operation() {
      CallbackContext context = callbackContext;

      @Override
      public void run() {
        String accountName =
            mPlugin
                .getCordova()
                .getActivity()
                .getPreferences(Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null);
        if (accountName == null) {
          if (mPlugin.hasAccountPermissions()) {
            mPlugin.chooseAccount(this, callbackContext, mCredential.newChooseAccountIntent());
          } else {
            mPlugin.askForAccountPermission(this);
          }
        } else {
          mCredential.setSelectedAccountName(accountName);
          mService = buildClient();
          callbackContext.success("signed in as " + mAccountName);
        }
      }

      @Override
      public void abort() {
        context.error("Could not sign in");
      }
    };
  }

  public Operation isUserSignedIn(final CallbackContext callbackContext) {
    return new Operation() {
      @Override
      public void run() {
        String accountName = mCredential.getSelectedAccountName();
        if (accountName != null && accountName.length() != 0) {
          callbackContext.success(accountName);
        } else {
          callbackContext.error("No user signed in");
        }
      }
    };
  }

  public Operation signOut(final CallbackContext callbackContext) {
    return new Operation() {
      @Override
      public void run() {
        String accountName = mCredential.getSelectedAccountName();
        if (accountName != "" && accountName != null) {
          setCredential();
          mService = null;
          callbackContext.success("Succesfully signed out");
          SharedPreferences settings = mActivity.getPreferences(Context.MODE_PRIVATE);
          SharedPreferences.Editor editor = settings.edit();
          editor.putString(PREF_ACCOUNT_NAME, accountName);
          editor.apply();
        } else {
          callbackContext.error("Already signed out");
        }
      }
    };
  }

  public String getApplicationName(Context context) {
    ApplicationInfo appInfo = context.getApplicationInfo();
    int stringId = appInfo.labelRes;
    return stringId == 0 ? appInfo.nonLocalizedLabel.toString() : context.getString(stringId);
  }

  private Sheets buildClient() {
    return new com.google.api.services.sheets.v4.Sheets.Builder(
            AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), mCredential)
        .setApplicationName(mApplicationName)
        .build();
  }

  public Sheets getService() throws UserNotSignedIn {
    if (mService != null) {
      return mService;
    } else {
      throw new UserNotSignedIn();
    }
  }
}

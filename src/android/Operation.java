package org.sumaq.plugins.googlesheets;

import org.apache.cordova.CallbackContext;

public abstract class Operation implements Runnable {
  public static final String INTERRUPTED_OPERATION = "OPERATION_INTERRUPTED_OP";

  private CallbackContext mContext;

  public void run() {
    mContext.success("Success!!!");
  };

  public void abort() {
    mContext.error("Aborted");
  };
}

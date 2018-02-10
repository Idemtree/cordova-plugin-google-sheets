package org.sumaq.plugins.googlesheets;

public class UserNotSignedIn extends Exception {
  private static final String MESSAGE = "The user has not signed in";

  public UserNotSignedIn() {
    super(UserNotSignedIn.MESSAGE);
  }
}

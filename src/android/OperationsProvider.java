package org.sumaq.plugins.googlesheets;

public abstract class OperationsProvider {
  public boolean isCordovaNullable(String value) {
    return (value == null || value == "null");
  }
}

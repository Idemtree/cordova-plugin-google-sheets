package org.sumaq.plugins.googlesheets;

public abstract class Operations {
  public boolean isCordovaNullable(String value) {
    return (value == null || value == "null");
  }
}

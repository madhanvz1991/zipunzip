package com.zip.zipunzip.model.enums;

public enum RandomAccessFileMode {

  READ("r"),
  WRITE("rw");

  private String value;

  RandomAccessFileMode(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

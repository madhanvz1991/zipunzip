package com.zip.zipunzip.model;

import com.zip.zipunzip.headers.HeaderSignature;

public abstract class ZipHeader {

  private HeaderSignature signature;

  public HeaderSignature getSignature() {
    return signature;
  }

  public void setSignature(HeaderSignature signature) {
    this.signature = signature;
  }
}

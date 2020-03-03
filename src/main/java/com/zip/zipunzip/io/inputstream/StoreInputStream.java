package com.zip.zipunzip.io.inputstream;

import com.zip.zipunzip.io.inputstream.CipherInputStream;
import com.zip.zipunzip.io.inputstream.DecompressedInputStream;

class StoreInputStream extends DecompressedInputStream {

  public StoreInputStream(CipherInputStream cipherInputStream) {
    super(cipherInputStream);
  }
}

package com.zip.zipunzip.io.outputstream;

import com.zip.zipunzip.io.outputstream.CipherOutputStream;
import com.zip.zipunzip.io.outputstream.CompressedOutputStream;

class StoreOutputStream extends CompressedOutputStream {

  public StoreOutputStream(CipherOutputStream cipherOutputStream) {
    super(cipherOutputStream);
  }

}

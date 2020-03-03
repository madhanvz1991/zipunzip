package com.zip.zipunzip.io.inputstream;

import com.zip.zipunzip.crypto.Decrypter;
import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.io.inputstream.CipherInputStream;
import com.zip.zipunzip.io.inputstream.ZipEntryInputStream;
import com.zip.zipunzip.model.LocalFileHeader;

import java.io.IOException;

class NoCipherInputStream extends CipherInputStream {

  public NoCipherInputStream(ZipEntryInputStream zipEntryInputStream, LocalFileHeader localFileHeader, char[] password) throws IOException, ZipException {
    super(zipEntryInputStream, localFileHeader, password);
  }

  @Override
  protected Decrypter initializeDecrypter(LocalFileHeader localFileHeader, char[] password) {
    return new NoDecrypter();
  }

  static class NoDecrypter implements Decrypter {

    @Override
    public int decryptData(byte[] buff, int start, int len) {
      return len;
    }
  }
}

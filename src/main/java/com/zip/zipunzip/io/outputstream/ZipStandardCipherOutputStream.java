package com.zip.zipunzip.io.outputstream;

import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.io.outputstream.CipherOutputStream;
import com.zip.zipunzip.io.outputstream.ZipEntryOutputStream;
import com.zip.zipunzip.model.ZipParameters;
import com.zip.zipunzip.util.ZipUtil;

import java.io.IOException;
import java.io.OutputStream;

import com.zip.zipunzip.crypto.StandardEncrypter;

class ZipStandardCipherOutputStream extends CipherOutputStream<StandardEncrypter> {

  public ZipStandardCipherOutputStream(ZipEntryOutputStream outputStream, ZipParameters zipParameters, char[] password) throws IOException, ZipException {
    super(outputStream, zipParameters, password);
  }

  @Override
  protected StandardEncrypter initializeEncrypter(OutputStream outputStream, ZipParameters zipParameters, char[] password) throws IOException, ZipException {
    long key = getEncryptionKey(zipParameters);
    StandardEncrypter encrypter = new StandardEncrypter(password, key);
    writeHeaders(encrypter.getHeaderBytes());
    return encrypter;
  }

  @Override
  public void write(int b) throws IOException {
    write(new byte[] {(byte) b});
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    super.write(b, off, len);
  }

  private long getEncryptionKey(ZipParameters zipParameters) {
    if (zipParameters.isWriteExtendedLocalFileHeader()) {
      long dosTime = ZipUtil.javaToDosTime(zipParameters.getLastModifiedFileTime());
      return (dosTime & 0x0000ffff) << 16;
    }

    return zipParameters.getEntryCRC();
  }
}

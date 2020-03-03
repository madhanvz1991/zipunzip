

package com.zip.zipunzip.io.outputstream;

import com.zip.zipunzip.crypto.Encrypter;
import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.io.outputstream.ZipEntryOutputStream;
import com.zip.zipunzip.model.ZipParameters;

import java.io.IOException;
import java.io.OutputStream;

abstract class CipherOutputStream<T extends Encrypter> extends OutputStream {

  private ZipEntryOutputStream zipEntryOutputStream;
  private T encrypter;

  public CipherOutputStream(ZipEntryOutputStream zipEntryOutputStream, ZipParameters zipParameters, char[] password)
      throws IOException, ZipException {
    this.zipEntryOutputStream = zipEntryOutputStream;
    this.encrypter = initializeEncrypter(zipEntryOutputStream, zipParameters, password);
  }

  @Override
  public void write(int b) throws IOException {
    zipEntryOutputStream.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    zipEntryOutputStream.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    encrypter.encryptData(b, off, len);
    zipEntryOutputStream.write(b, off, len);
  }

  public void writeHeaders(byte[] b) throws IOException {
    zipEntryOutputStream.write(b);
  }

  public void closeEntry() throws IOException {
    zipEntryOutputStream.closeEntry();
  }

  @Override
  public void close() throws IOException {
    zipEntryOutputStream.close();
  }

  public long getNumberOfBytesWrittenForThisEntry() {
    return zipEntryOutputStream.getNumberOfBytesWrittenForThisEntry();
  }

  protected T getEncrypter() {
    return encrypter;
  }

  protected abstract T initializeEncrypter(OutputStream outputStream, ZipParameters zipParameters, char[] password)
      throws IOException, ZipException;
}

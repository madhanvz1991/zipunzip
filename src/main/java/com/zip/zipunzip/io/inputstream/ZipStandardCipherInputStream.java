package com.zip.zipunzip.io.inputstream;

import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.io.inputstream.CipherInputStream;
import com.zip.zipunzip.io.inputstream.ZipEntryInputStream;
import com.zip.zipunzip.model.LocalFileHeader;

import java.io.IOException;

import com.zip.zipunzip.crypto.StandardDecrypter;

import static com.zip.zipunzip.util.InternalZipConstants.STD_DEC_HDR_SIZE;

class ZipStandardCipherInputStream extends CipherInputStream<StandardDecrypter> {

  public ZipStandardCipherInputStream(ZipEntryInputStream zipEntryInputStream, LocalFileHeader localFileHeader, char[] password) throws IOException, ZipException {
    super(zipEntryInputStream, localFileHeader, password);
  }

  @Override
  protected StandardDecrypter initializeDecrypter(LocalFileHeader localFileHeader, char[] password) throws ZipException, IOException {
    return new StandardDecrypter(password, localFileHeader.getCrcRawData(), getStandardDecrypterHeaderBytes());
  }

  private byte[] getStandardDecrypterHeaderBytes() throws IOException {
    byte[] headerBytes = new byte[STD_DEC_HDR_SIZE];
    readRaw(headerBytes);
    return headerBytes;
  }
}



package com.zip.zipunzip.crypto;

import com.zip.zipunzip.exception.ZipException;

public interface Encrypter {

  int encryptData(byte[] buff) throws ZipException;

  int encryptData(byte[] buff, int start, int len) throws ZipException;

}

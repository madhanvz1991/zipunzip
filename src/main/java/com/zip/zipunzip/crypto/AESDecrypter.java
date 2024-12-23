

package com.zip.zipunzip.crypto;

import com.zip.zipunzip.crypto.PBKDF2.MacBasedPRF;
import com.zip.zipunzip.crypto.PBKDF2.PBKDF2Engine;
import com.zip.zipunzip.crypto.PBKDF2.PBKDF2Parameters;
import com.zip.zipunzip.crypto.engine.AESEngine;
import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.model.AESExtraDataRecord;
import com.zip.zipunzip.model.enums.AesKeyStrength;

import java.util.Arrays;

import static com.zip.zipunzip.crypto.AesCipherUtil.prepareBuffAESIVBytes;
import static com.zip.zipunzip.util.InternalZipConstants.AES_BLOCK_SIZE;

public class AESDecrypter implements Decrypter {

  public static final int PASSWORD_VERIFIER_LENGTH = 2;

  private AESExtraDataRecord aesExtraDataRecord;
  private char[] password;
  private AESEngine aesEngine;
  private MacBasedPRF mac;

  private int nonce = 1;
  private byte[] iv;
  private byte[] counterBlock;

  public AESDecrypter(AESExtraDataRecord aesExtraDataRecord, char[] password, byte[] salt, byte[] passwordVerifier) throws ZipException {
    this.aesExtraDataRecord = aesExtraDataRecord;
    this.password = password;
    iv = new byte[AES_BLOCK_SIZE];
    counterBlock = new byte[AES_BLOCK_SIZE];
    init(salt, passwordVerifier);
  }

  private void init(byte[] salt, byte[] passwordVerifier) throws ZipException {
    AesKeyStrength aesKeyStrength = aesExtraDataRecord.getAesKeyStrength();

    if (password == null || password.length <= 0) {
      throw new ZipException("empty or null password provided for AES Decryptor");
    }

    byte[] derivedKey = deriveKey(salt, password, aesKeyStrength.getKeyLength(), aesKeyStrength.getMacLength());
    if (derivedKey == null || derivedKey.length != (aesKeyStrength.getKeyLength() + aesKeyStrength.getMacLength()
        + PASSWORD_VERIFIER_LENGTH)) {
      throw new ZipException("invalid derived key");
    }

    byte[] aesKey = new byte[aesKeyStrength.getKeyLength()];
    byte[] macKey = new byte[aesKeyStrength.getMacLength()];
    byte[] derivedPasswordVerifier = new byte[PASSWORD_VERIFIER_LENGTH];

    System.arraycopy(derivedKey, 0, aesKey, 0, aesKeyStrength.getKeyLength());
    System.arraycopy(derivedKey, aesKeyStrength.getKeyLength(), macKey, 0, aesKeyStrength.getMacLength());
    System.arraycopy(derivedKey, aesKeyStrength.getKeyLength() + aesKeyStrength.getMacLength(), derivedPasswordVerifier,
        0, PASSWORD_VERIFIER_LENGTH);

    if (!Arrays.equals(passwordVerifier, derivedPasswordVerifier)) {
      throw new ZipException("Wrong Password", ZipException.Type.WRONG_PASSWORD);
    }

    aesEngine = new AESEngine(aesKey);
    mac = new MacBasedPRF("HmacSHA1");
    mac.init(macKey);
  }

  @Override
  public int decryptData(byte[] buff, int start, int len) throws ZipException {

    for (int j = start; j < (start + len); j += AES_BLOCK_SIZE) {
      int loopCount = (j + AES_BLOCK_SIZE <= (start + len)) ?
          AES_BLOCK_SIZE : ((start + len) - j);

      mac.update(buff, j, loopCount);
      prepareBuffAESIVBytes(iv, nonce);
      aesEngine.processBlock(iv, counterBlock);

      for (int k = 0; k < loopCount; k++) {
        buff[j + k] = (byte) (buff[j + k] ^ counterBlock[k]);
      }

      nonce++;
    }

    return len;
  }

  private byte[] deriveKey(byte[] salt, char[] password, int keyLength, int macLength) {
    PBKDF2Parameters p = new PBKDF2Parameters("HmacSHA1", "ISO-8859-1", salt, 1000);
    PBKDF2Engine e = new PBKDF2Engine(p);
    return e.deriveKey(password, keyLength + macLength + PASSWORD_VERIFIER_LENGTH);
  }

  public byte[] getCalculatedAuthenticationBytes() {
    return mac.doFinal();
  }
}


package com.zip.zipunzip.crypto.PBKDF2;

/*
 * Source referred from Matthias Gartner's PKCS#5 implementation -
 * see http://rtner.de/software/PBKDF2.html
 */

interface PRF {

  void init(byte[] P);

  byte[] doFinal(byte[] M);

  int getHLen();
}

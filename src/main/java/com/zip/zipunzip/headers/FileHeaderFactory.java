package com.zip.zipunzip.headers;

import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.model.AESExtraDataRecord;
import com.zip.zipunzip.model.FileHeader;
import com.zip.zipunzip.model.LocalFileHeader;
import com.zip.zipunzip.model.ZipParameters;
import com.zip.zipunzip.model.enums.AesKeyStrength;
import com.zip.zipunzip.model.enums.CompressionLevel;
import com.zip.zipunzip.model.enums.CompressionMethod;
import com.zip.zipunzip.model.enums.EncryptionMethod;
import com.zip.zipunzip.util.InternalZipConstants;
import com.zip.zipunzip.util.ZipUtil;

import java.nio.charset.Charset;

import static com.zip.zipunzip.util.BitUtils.setBit;
import static com.zip.zipunzip.util.BitUtils.unsetBit;
import static com.zip.zipunzip.util.FileUtils.isZipEntryDirectory;

public class FileHeaderFactory {

  public FileHeader generateFileHeader(ZipParameters zipParameters, boolean isSplitZip, int currentDiskNumberStart, Charset charset)
      throws ZipException {

    FileHeader fileHeader = new FileHeader();
    fileHeader.setSignature(HeaderSignature.CENTRAL_DIRECTORY);
    fileHeader.setVersionMadeBy(20);
    fileHeader.setVersionNeededToExtract(20);

    if (zipParameters.isEncryptFiles() && zipParameters.getEncryptionMethod() == EncryptionMethod.AES) {
      fileHeader.setCompressionMethod(CompressionMethod.AES_INTERNAL_ONLY);
      fileHeader.setAesExtraDataRecord(generateAESExtraDataRecord(zipParameters));
    } else {
      fileHeader.setCompressionMethod(zipParameters.getCompressionMethod());
    }

    if (zipParameters.isEncryptFiles()) {
      if (zipParameters.getEncryptionMethod() == null || zipParameters.getEncryptionMethod() == EncryptionMethod.NONE) {
        throw new ZipException("Encryption method has to be set when encryptFiles flag is set in zip parameters");
      }

      fileHeader.setEncrypted(true);
      fileHeader.setEncryptionMethod(zipParameters.getEncryptionMethod());
    }

    String fileName = validateAndGetFileName(zipParameters.getFileNameInZip());
    fileHeader.setFileName(fileName);
    fileHeader.setFileNameLength(determineFileNameLength(fileName, charset));
    fileHeader.setDiskNumberStart(isSplitZip ? currentDiskNumberStart : 0);

    if (zipParameters.getLastModifiedFileTime() > 0) {
      fileHeader.setLastModifiedTime(ZipUtil.javaToDosTime(zipParameters.getLastModifiedFileTime()));
    } else {
      fileHeader.setLastModifiedTime(ZipUtil.javaToDosTime(System.currentTimeMillis()));
    }

    //For files added by this library, this attribute will be set after closeEntry is done
    fileHeader.setExternalFileAttributes(new byte[4]);
    fileHeader.setDirectory(isZipEntryDirectory(fileName));

    if (zipParameters.isWriteExtendedLocalFileHeader() && zipParameters.getEntrySize() == -1) {
      fileHeader.setUncompressedSize(0);
    } else {
      fileHeader.setUncompressedSize(zipParameters.getEntrySize());
    }

    if (zipParameters.isEncryptFiles() && zipParameters.getEncryptionMethod() == EncryptionMethod.ZIP_STANDARD) {
      fileHeader.setCrc(zipParameters.getEntryCRC());
    }

    fileHeader.setGeneralPurposeFlag(determineGeneralPurposeBitFlag(fileHeader.isEncrypted(), zipParameters, charset));
    fileHeader.setDataDescriptorExists(zipParameters.isWriteExtendedLocalFileHeader());
    fileHeader.setFileComment(zipParameters.getFileComment());
    return fileHeader;
  }

  public LocalFileHeader generateLocalFileHeader(FileHeader fileHeader) {
    LocalFileHeader localFileHeader = new LocalFileHeader();
    localFileHeader.setSignature(HeaderSignature.LOCAL_FILE_HEADER);
    localFileHeader.setVersionNeededToExtract(fileHeader.getVersionNeededToExtract());
    localFileHeader.setCompressionMethod(fileHeader.getCompressionMethod());
    localFileHeader.setLastModifiedTime(fileHeader.getLastModifiedTime());
    localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());
    localFileHeader.setFileNameLength(fileHeader.getFileNameLength());
    localFileHeader.setFileName(fileHeader.getFileName());
    localFileHeader.setEncrypted(fileHeader.isEncrypted());
    localFileHeader.setEncryptionMethod(fileHeader.getEncryptionMethod());
    localFileHeader.setAesExtraDataRecord(fileHeader.getAesExtraDataRecord());
    localFileHeader.setCrc(fileHeader.getCrc());
    localFileHeader.setCompressedSize(fileHeader.getCompressedSize());
    localFileHeader.setGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag().clone());
    localFileHeader.setDataDescriptorExists(fileHeader.isDataDescriptorExists());
    return localFileHeader;
  }

  private byte[] determineGeneralPurposeBitFlag(boolean isEncrypted, ZipParameters zipParameters, Charset charset) {
    byte[] generalPurposeBitFlag = new byte[2];
    generalPurposeBitFlag[0] = generateFirstGeneralPurposeByte(isEncrypted, zipParameters);
    if(charset.equals(InternalZipConstants.CHARSET_UTF_8)) {
      generalPurposeBitFlag[1] = setBit(generalPurposeBitFlag[1], 3); // set 3rd bit which corresponds to utf-8 file name charset
    }
    return generalPurposeBitFlag;
  }

  private byte generateFirstGeneralPurposeByte(boolean isEncrypted, ZipParameters zipParameters) {

    byte firstByte = 0;

    if (isEncrypted) {
      firstByte = setBit(firstByte, 0);
    }

    if (CompressionMethod.DEFLATE.equals(zipParameters.getCompressionMethod())) {
      if (CompressionLevel.NORMAL.equals(zipParameters.getCompressionLevel())) {
        firstByte = unsetBit(firstByte, 1);
        firstByte = unsetBit(firstByte, 2);
      } else if (CompressionLevel.MAXIMUM.equals(zipParameters.getCompressionLevel())) {
        firstByte = setBit(firstByte, 1);
        firstByte = unsetBit(firstByte, 2);
      } else if (CompressionLevel.FAST.equals(zipParameters.getCompressionLevel())) {
        firstByte = unsetBit(firstByte, 1);
        firstByte = setBit(firstByte, 2);
      } else if (CompressionLevel.FASTEST.equals(zipParameters.getCompressionLevel())
          || CompressionLevel.ULTRA.equals(zipParameters.getCompressionLevel())) {
        firstByte = setBit(firstByte, 1);
        firstByte = setBit(firstByte, 2);
      }
    }

    if (zipParameters.isWriteExtendedLocalFileHeader()) {
      firstByte = setBit(firstByte, 3);
    }

    return firstByte;
  }

  private String validateAndGetFileName(String fileNameInZip) throws ZipException {
    if (!ZipUtil.isStringNotNullAndNotEmpty(fileNameInZip)) {
      throw new ZipException("fileNameInZip is null or empty");
    }
    return fileNameInZip;
  }

  private AESExtraDataRecord generateAESExtraDataRecord(ZipParameters parameters) throws ZipException {
    AESExtraDataRecord aesExtraDataRecord = new AESExtraDataRecord();

    if (parameters.getAesVersion() != null) {
      aesExtraDataRecord.setAesVersion(parameters.getAesVersion());
    }

    if (parameters.getAesKeyStrength() == AesKeyStrength.KEY_STRENGTH_128) {
      aesExtraDataRecord.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_128);
    } else if (parameters.getAesKeyStrength() == AesKeyStrength.KEY_STRENGTH_192) {
      aesExtraDataRecord.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_192);
    } else if (parameters.getAesKeyStrength() == AesKeyStrength.KEY_STRENGTH_256) {
      aesExtraDataRecord.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
    } else {
      throw new ZipException("invalid AES key strength");
    }

    aesExtraDataRecord.setCompressionMethod(parameters.getCompressionMethod());
    return aesExtraDataRecord;
  }

  private int determineFileNameLength(String fileName, Charset charset) {
    return fileName.getBytes(charset).length;
  }
}

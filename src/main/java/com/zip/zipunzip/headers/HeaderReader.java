package com.zip.zipunzip.headers;



import static com.zip.zipunzip.headers.HeaderUtil.decodeStringWithCharset;
import static com.zip.zipunzip.util.BitUtils.isBitSet;
import static com.zip.zipunzip.util.InternalZipConstants.ENDHDR;
import static com.zip.zipunzip.util.ZipUtil.readFully;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.model.CentralDirectory;
import com.zip.zipunzip.model.DataDescriptor;
import com.zip.zipunzip.model.DigitalSignature;
import com.zip.zipunzip.model.EndOfCentralDirectoryRecord;
import com.zip.zipunzip.model.FileHeader;
import com.zip.zipunzip.model.LocalFileHeader;
import com.zip.zipunzip.model.ZipModel;
import com.zip.zipunzip.model.enums.CompressionMethod;
import com.zip.zipunzip.model.enums.EncryptionMethod;
import com.zip.zipunzip.util.RawIO;

/**
 * Helper class to read header information for the zip file
 */
public class HeaderReader {

  private ZipModel zipModel;
  private RawIO rawIO = new RawIO();
  private byte[] intBuff = new byte[4];

  public ZipModel readAllHeaders(RandomAccessFile zipRaf, Charset charset) throws IOException {

    if (zipRaf.length() < ENDHDR) {
      throw new ZipException("Zip file size less than minimum expected zip file size. " +
          "Probably not a zip file or a corrupted zip file");
    }

    zipModel = new ZipModel();

    try {
      zipModel.setEndOfCentralDirectoryRecord(readEndOfCentralDirectoryRecord(zipRaf, rawIO, charset));
    } catch (ZipException e){
      throw e;
    } catch (IOException e) {
      throw new ZipException("Zip headers not found. Probably not a zip file or a corrupted zip file", e);
    }

    if (zipModel.getEndOfCentralDirectoryRecord().getTotalNumberOfEntriesInCentralDirectory() == 0) {
      return zipModel;
    }

    zipModel.setCentralDirectory(readCentralDirectory(zipRaf, rawIO, charset));

    return zipModel;
  }

  private EndOfCentralDirectoryRecord readEndOfCentralDirectoryRecord(RandomAccessFile zipRaf, RawIO rawIO, Charset charset)
      throws IOException {
    long zipFileLengthWithoutEndHeader = zipRaf.length() - ENDHDR;
    long pos = zipFileLengthWithoutEndHeader;

    EndOfCentralDirectoryRecord endOfCentralDirectoryRecord = new EndOfCentralDirectoryRecord();

    int counter = 0;
    int headerSignature;
    do {
      zipRaf.seek(pos--);
      counter++;
    } while (((headerSignature = rawIO.readIntLittleEndian(zipRaf))
        != HeaderSignature.END_OF_CENTRAL_DIRECTORY.getValue()) && counter <= zipFileLengthWithoutEndHeader);

    if (headerSignature != HeaderSignature.END_OF_CENTRAL_DIRECTORY.getValue()) {
      throw new ZipException("Zip headers not found. Probably not a zip file");
    }

    endOfCentralDirectoryRecord.setSignature(HeaderSignature.END_OF_CENTRAL_DIRECTORY);
    endOfCentralDirectoryRecord.setNumberOfThisDisk(rawIO.readShortLittleEndian(zipRaf));
    endOfCentralDirectoryRecord.setNumberOfThisDiskStartOfCentralDir(rawIO.readShortLittleEndian(zipRaf));
    endOfCentralDirectoryRecord.setTotalNumberOfEntriesInCentralDirectoryOnThisDisk(
        rawIO.readShortLittleEndian(zipRaf));
    endOfCentralDirectoryRecord.setTotalNumberOfEntriesInCentralDirectory(rawIO.readShortLittleEndian(zipRaf));
    endOfCentralDirectoryRecord.setSizeOfCentralDirectory(rawIO.readIntLittleEndian(zipRaf));

    zipRaf.readFully(intBuff);
    endOfCentralDirectoryRecord.setOffsetOfStartOfCentralDirectory(rawIO.readLongLittleEndian(intBuff, 0));

    zipModel.setSplitArchive(endOfCentralDirectoryRecord.getNumberOfThisDisk() > 0);
    return endOfCentralDirectoryRecord;
  }

  private CentralDirectory readCentralDirectory(RandomAccessFile zipRaf, RawIO rawIO, Charset charset) throws IOException {
    CentralDirectory centralDirectory = new CentralDirectory();
    List<FileHeader> fileHeaders = new ArrayList<>();

    long offSetStartCentralDir = getOffsetCentralDirectory(zipModel);
    long centralDirEntryCount = getNumberOfEntriesInCentralDirectory(zipModel);

    if (zipModel.isZip64Format()) {
      offSetStartCentralDir = zipModel.getZip64EndOfCentralDirectoryRecord()
          .getOffsetStartCentralDirectoryWRTStartDiskNumber();
      centralDirEntryCount = (int) zipModel.getZip64EndOfCentralDirectoryRecord()
          .getTotalNumberOfEntriesInCentralDirectory();
    }

    zipRaf.seek(offSetStartCentralDir);

    byte[] shortBuff = new byte[2];
    byte[] intBuff = new byte[4];

    for (int i = 0; i < centralDirEntryCount; i++) {
      FileHeader fileHeader = new FileHeader();
      if (rawIO.readIntLittleEndian(zipRaf) != HeaderSignature.CENTRAL_DIRECTORY.getValue()) {
        throw new ZipException("Expected central directory entry not found (#" + (i + 1) + ")");
      }
      fileHeader.setSignature(HeaderSignature.CENTRAL_DIRECTORY);
      fileHeader.setVersionMadeBy(rawIO.readShortLittleEndian(zipRaf));
      fileHeader.setVersionNeededToExtract(rawIO.readShortLittleEndian(zipRaf));

      byte[] generalPurposeFlags = new byte[2];
      zipRaf.readFully(generalPurposeFlags);
      fileHeader.setEncrypted(isBitSet(generalPurposeFlags[0], 0));
      fileHeader.setDataDescriptorExists(isBitSet(generalPurposeFlags[0], 3));
      fileHeader.setFileNameUTF8Encoded(isBitSet(generalPurposeFlags[1], 3));
      fileHeader.setGeneralPurposeFlag(generalPurposeFlags.clone());

      fileHeader.setCompressionMethod(CompressionMethod.getCompressionMethodFromCode(rawIO.readShortLittleEndian(
          zipRaf)));
      fileHeader.setLastModifiedTime(rawIO.readIntLittleEndian(zipRaf));

      zipRaf.readFully(intBuff);
      fileHeader.setCrc(rawIO.readLongLittleEndian(intBuff, 0));
      fileHeader.setCrcRawData(intBuff);

      fileHeader.setCompressedSize(rawIO.readLongLittleEndian(zipRaf, 4));
      fileHeader.setUncompressedSize(rawIO.readLongLittleEndian(zipRaf, 4));

      int fileNameLength = rawIO.readShortLittleEndian(zipRaf);
      fileHeader.setFileNameLength(fileNameLength);

      fileHeader.setExtraFieldLength(rawIO.readShortLittleEndian(zipRaf));

      int fileCommentLength = rawIO.readShortLittleEndian(zipRaf);
      fileHeader.setFileCommentLength(fileCommentLength);

      fileHeader.setDiskNumberStart(rawIO.readShortLittleEndian(zipRaf));

      zipRaf.readFully(shortBuff);
      fileHeader.setInternalFileAttributes(shortBuff.clone());

      zipRaf.readFully(intBuff);
      fileHeader.setExternalFileAttributes(intBuff.clone());

      zipRaf.readFully(intBuff);
      fileHeader.setOffsetLocalHeader(rawIO.readLongLittleEndian(intBuff, 0));

      if (fileNameLength > 0) {
        byte[] fileNameBuff = new byte[fileNameLength];
        zipRaf.readFully(fileNameBuff);
        String fileName = decodeStringWithCharset(fileNameBuff, fileHeader.isFileNameUTF8Encoded(), charset);

        if (fileName.contains(":\\")) {
          fileName = fileName.substring(fileName.indexOf(":\\") + 2);
        }

        fileHeader.setFileName(fileName);
        fileHeader.setDirectory(fileName.endsWith("/") || fileName.endsWith("\\"));
      } else {
        fileHeader.setFileName(null);
      }

      if (fileCommentLength > 0) {
        byte[] fileCommentBuff = new byte[fileCommentLength];
        zipRaf.readFully(fileCommentBuff);
        fileHeader.setFileComment(decodeStringWithCharset(fileCommentBuff, fileHeader.isFileNameUTF8Encoded(), charset));
      }

      if (fileHeader.isEncrypted()) {
        if (fileHeader.getAesExtraDataRecord() != null) {
          fileHeader.setEncryptionMethod(EncryptionMethod.AES);
        } else {
          fileHeader.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
        }
      }

      fileHeaders.add(fileHeader);
    }

    centralDirectory.setFileHeaders(fileHeaders);

    DigitalSignature digitalSignature = new DigitalSignature();
    if (rawIO.readIntLittleEndian(zipRaf) == HeaderSignature.DIGITAL_SIGNATURE.getValue()) {
      digitalSignature.setSignature(HeaderSignature.DIGITAL_SIGNATURE);
      digitalSignature.setSizeOfData(rawIO.readShortLittleEndian(zipRaf));

      if (digitalSignature.getSizeOfData() > 0) {
        byte[] signatureDataBuff = new byte[digitalSignature.getSizeOfData()];
        zipRaf.readFully(signatureDataBuff);
        digitalSignature.setSignatureData(new String(signatureDataBuff));
      }
    }

    return centralDirectory;
  }

  public LocalFileHeader readLocalFileHeader(InputStream inputStream, Charset charset) throws IOException {
    LocalFileHeader localFileHeader = new LocalFileHeader();
    byte[] intBuff = new byte[4];

    //signature
    int sig = rawIO.readIntLittleEndian(inputStream);
    if (sig != HeaderSignature.LOCAL_FILE_HEADER.getValue()) {
      return null;
    }
    localFileHeader.setSignature(HeaderSignature.LOCAL_FILE_HEADER);
    localFileHeader.setVersionNeededToExtract(rawIO.readShortLittleEndian(inputStream));

    byte[] generalPurposeFlags = new byte[2];
    if (readFully(inputStream, generalPurposeFlags) != 2) {
      throw new ZipException("Could not read enough bytes for generalPurposeFlags");
    }
    localFileHeader.setEncrypted(isBitSet(generalPurposeFlags[0], 0));
    localFileHeader.setDataDescriptorExists(isBitSet(generalPurposeFlags[0], 3));
    localFileHeader.setFileNameUTF8Encoded(isBitSet(generalPurposeFlags[1], 3));
    localFileHeader.setGeneralPurposeFlag(generalPurposeFlags.clone());

    localFileHeader.setCompressionMethod(CompressionMethod.getCompressionMethodFromCode(
        rawIO.readShortLittleEndian(inputStream)));
    localFileHeader.setLastModifiedTime(rawIO.readIntLittleEndian(inputStream));

    readFully(inputStream, intBuff);
    localFileHeader.setCrc(rawIO.readLongLittleEndian(intBuff, 0));
    localFileHeader.setCrcRawData(intBuff.clone());

    localFileHeader.setCompressedSize(rawIO.readLongLittleEndian(inputStream, 4));
    localFileHeader.setUncompressedSize(rawIO.readLongLittleEndian(inputStream, 4));

    int fileNameLength = rawIO.readShortLittleEndian(inputStream);
    localFileHeader.setFileNameLength(fileNameLength);

    localFileHeader.setExtraFieldLength(rawIO.readShortLittleEndian(inputStream));

    if (fileNameLength > 0) {
      byte[] fileNameBuf = new byte[fileNameLength];
      readFully(inputStream, fileNameBuf);
      // Modified after user reported an issue http://www.lingala.net/zip4j/forum/index.php?topic=2.0
//				String fileName = new String(fileNameBuf, "Cp850");
//				String fileName = Zip4jUtil.getCp850EncodedString(fileNameBuf);
      String fileName = decodeStringWithCharset(fileNameBuf, localFileHeader.isFileNameUTF8Encoded(), charset);

      if (fileName == null) {
        throw new ZipException("file name is null, cannot assign file name to local file header");
      }

      if (fileName.contains(":" + System.getProperty("file.separator"))) {
        fileName = fileName.substring(fileName.indexOf(":" + System.getProperty("file.separator")) + 2);
      }

      localFileHeader.setFileName(fileName);
      localFileHeader.setDirectory(fileName.endsWith("/") || fileName.endsWith("\\"));
    } else {
      localFileHeader.setFileName(null);
    }

    if (localFileHeader.isEncrypted()) {

      if (localFileHeader.getEncryptionMethod() == EncryptionMethod.AES) {
        //Do nothing
      } else {
        if (BigInteger.valueOf(localFileHeader.getGeneralPurposeFlag()[0]).testBit(6)) {
          localFileHeader.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD_VARIANT_STRONG);
        } else {
          localFileHeader.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
        }
      }

    }

    return localFileHeader;
  }

  public DataDescriptor readDataDescriptor(InputStream inputStream, boolean isZip64Format) throws IOException {

    DataDescriptor dataDescriptor = new DataDescriptor();

    byte[] intBuff = new byte[4];
    readFully(inputStream, intBuff);
    long sigOrCrc = rawIO.readLongLittleEndian(intBuff, 0);

    //According to zip specification, presence of extra data record header signature is optional.
    //If this signature is present, read it and read the next 4 bytes for crc
    //If signature not present, assign the read 4 bytes for crc
    if (sigOrCrc == HeaderSignature.EXTRA_DATA_RECORD.getValue()) {
      dataDescriptor.setSignature(HeaderSignature.EXTRA_DATA_RECORD);
      readFully(inputStream, intBuff);
      dataDescriptor.setCrc(rawIO.readLongLittleEndian(intBuff, 0));
    } else {
      dataDescriptor.setCrc(sigOrCrc);
    }

    if (isZip64Format) {
      dataDescriptor.setCompressedSize(rawIO.readLongLittleEndian(inputStream));
      dataDescriptor.setUncompressedSize(rawIO.readLongLittleEndian(inputStream));
    } else {
      dataDescriptor.setCompressedSize(rawIO.readIntLittleEndian(inputStream));
      dataDescriptor.setUncompressedSize(rawIO.readIntLittleEndian(inputStream));
    }

    return dataDescriptor;
  }

  

  private long getOffsetCentralDirectory(ZipModel zipModel) {
    return zipModel.getEndOfCentralDirectoryRecord().getOffsetOfStartOfCentralDirectory();
  }

  private long getNumberOfEntriesInCentralDirectory(ZipModel zipModel) {
    return zipModel.getEndOfCentralDirectoryRecord().getTotalNumberOfEntriesInCentralDirectory();
  }
}

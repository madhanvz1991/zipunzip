

package com.zip.zipunzip.model;

import com.zip.zipunzip.model.enums.AesKeyStrength;
import com.zip.zipunzip.model.enums.AesVersion;
import com.zip.zipunzip.model.enums.CompressionLevel;
import com.zip.zipunzip.model.enums.CompressionMethod;
import com.zip.zipunzip.model.enums.EncryptionMethod;

public class ZipParameters {

  private CompressionMethod compressionMethod = CompressionMethod.DEFLATE;
  private CompressionLevel compressionLevel = CompressionLevel.NORMAL;
  private boolean encryptFiles = false;
  private EncryptionMethod encryptionMethod = EncryptionMethod.NONE;
  private boolean readHiddenFiles = true;
  private boolean readHiddenFolders = true;
  private AesKeyStrength aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256;
  private AesVersion aesVersion = AesVersion.TWO;
  private boolean includeRootFolder = true;
  private long entryCRC;
  private String defaultFolderPath;
  private String fileNameInZip;
  private long lastModifiedFileTime = System.currentTimeMillis();
  private long entrySize = -1;
  private boolean writeExtendedLocalFileHeader = true;
  private boolean overrideExistingFilesInZip = true;
  private String rootFolderNameInZip;
  private String fileComment;

  public ZipParameters() {
  }

  public ZipParameters(ZipParameters zipParameters) {
    this.compressionMethod = zipParameters.getCompressionMethod();
    this.compressionLevel = zipParameters.getCompressionLevel();
    this.encryptFiles = zipParameters.isEncryptFiles();
    this.encryptionMethod = zipParameters.getEncryptionMethod();
    this.readHiddenFiles = zipParameters.isReadHiddenFiles();
    this.readHiddenFolders = zipParameters.isReadHiddenFolders();
    this.aesKeyStrength = zipParameters.getAesKeyStrength();
    this.aesVersion = zipParameters.getAesVersion();
    this.includeRootFolder = zipParameters.isIncludeRootFolder();
    this.entryCRC = zipParameters.getEntryCRC();
    this.defaultFolderPath = zipParameters.getDefaultFolderPath();
    this.fileNameInZip = zipParameters.getFileNameInZip();
    this.lastModifiedFileTime = zipParameters.getLastModifiedFileTime();
    this.entrySize = zipParameters.getEntrySize();
    this.writeExtendedLocalFileHeader = zipParameters.isWriteExtendedLocalFileHeader();
    this.overrideExistingFilesInZip = zipParameters.isOverrideExistingFilesInZip();
    this.rootFolderNameInZip = zipParameters.getRootFolderNameInZip();
    this.fileComment = zipParameters.getFileComment();
  }

  public CompressionMethod getCompressionMethod() {
    return compressionMethod;
  }

  public void setCompressionMethod(CompressionMethod compressionMethod) {
    this.compressionMethod = compressionMethod;
  }

  public boolean isEncryptFiles() {
    return encryptFiles;
  }

  public void setEncryptFiles(boolean encryptFiles) {
    this.encryptFiles = encryptFiles;
  }

  public EncryptionMethod getEncryptionMethod() {
    return encryptionMethod;
  }

  public void setEncryptionMethod(EncryptionMethod encryptionMethod) {
    this.encryptionMethod = encryptionMethod;
  }

  public CompressionLevel getCompressionLevel() {
    return compressionLevel;
  }

  public void setCompressionLevel(CompressionLevel compressionLevel) {
    this.compressionLevel = compressionLevel;
  }

  public boolean isReadHiddenFiles() {
    return readHiddenFiles;
  }

  public void setReadHiddenFiles(boolean readHiddenFiles) {
    this.readHiddenFiles = readHiddenFiles;
  }

  public boolean isReadHiddenFolders() {
    return readHiddenFolders;
  }

  public void setReadHiddenFolders(boolean readHiddenFolders) {
    this.readHiddenFolders = readHiddenFolders;
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public AesKeyStrength getAesKeyStrength() {
    return aesKeyStrength;
  }

  public void setAesKeyStrength(AesKeyStrength aesKeyStrength) {
    this.aesKeyStrength = aesKeyStrength;
  }

  public AesVersion getAesVersion() {
    return aesVersion;
  }

  public void setAesVersion(AesVersion aesVersion) {
    this.aesVersion = aesVersion;
  }

  public boolean isIncludeRootFolder() {
    return includeRootFolder;
  }

  public void setIncludeRootFolder(boolean includeRootFolder) {
    this.includeRootFolder = includeRootFolder;
  }

  public long getEntryCRC() {
    return entryCRC;
  }

  public void setEntryCRC(long entryCRC) {
    this.entryCRC = entryCRC;
  }

  public String getDefaultFolderPath() {
    return defaultFolderPath;
  }

  public void setDefaultFolderPath(String defaultFolderPath) {
    this.defaultFolderPath = defaultFolderPath;
  }

  public String getFileNameInZip() {
    return fileNameInZip;
  }

  public void setFileNameInZip(String fileNameInZip) {
    this.fileNameInZip = fileNameInZip;
  }

  public long getLastModifiedFileTime() {
    return lastModifiedFileTime;
  }

  public void setLastModifiedFileTime(long lastModifiedFileTime) {
    if (lastModifiedFileTime <= 0) {
      return;
    }

    this.lastModifiedFileTime = lastModifiedFileTime;
  }

  public long getEntrySize() {
    return entrySize;
  }

  public void setEntrySize(long entrySize) {
    this.entrySize = entrySize;
  }

  public boolean isWriteExtendedLocalFileHeader() {
    return writeExtendedLocalFileHeader;
  }

  public void setWriteExtendedLocalFileHeader(boolean writeExtendedLocalFileHeader) {
    this.writeExtendedLocalFileHeader = writeExtendedLocalFileHeader;
  }

  public boolean isOverrideExistingFilesInZip() {
    return overrideExistingFilesInZip;
  }

  public void setOverrideExistingFilesInZip(boolean overrideExistingFilesInZip) {
    this.overrideExistingFilesInZip = overrideExistingFilesInZip;
  }

  public String getRootFolderNameInZip() {
    return rootFolderNameInZip;
  }

  public void setRootFolderNameInZip(String rootFolderNameInZip) {
    this.rootFolderNameInZip = rootFolderNameInZip;
  }

  public String getFileComment() {
    return fileComment;
  }

  public void setFileComment(String fileComment) {
    this.fileComment = fileComment;
  }
}

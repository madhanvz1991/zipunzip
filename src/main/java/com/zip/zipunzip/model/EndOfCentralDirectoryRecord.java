package com.zip.zipunzip.model;

import com.zip.zipunzip.headers.HeaderSignature;

public class EndOfCentralDirectoryRecord extends ZipHeader {

  private int numberOfThisDisk;
  private int numberOfThisDiskStartOfCentralDir;
  private int totalNumberOfEntriesInCentralDirectoryOnThisDisk;
  private int totalNumberOfEntriesInCentralDirectory;
  private int sizeOfCentralDirectory;
  private long offsetOfStartOfCentralDirectory;
  private String comment = "";

  public EndOfCentralDirectoryRecord() {
    setSignature(HeaderSignature.END_OF_CENTRAL_DIRECTORY);
  }

  public int getNumberOfThisDisk() {
    return numberOfThisDisk;
  }

  public void setNumberOfThisDisk(int numberOfThisDisk) {
    this.numberOfThisDisk = numberOfThisDisk;
  }

  public int getNumberOfThisDiskStartOfCentralDir() {
    return numberOfThisDiskStartOfCentralDir;
  }

  public void setNumberOfThisDiskStartOfCentralDir(int numberOfThisDiskStartOfCentralDir) {
    this.numberOfThisDiskStartOfCentralDir = numberOfThisDiskStartOfCentralDir;
  }

  public int getTotalNumberOfEntriesInCentralDirectoryOnThisDisk() {
    return totalNumberOfEntriesInCentralDirectoryOnThisDisk;
  }

  public void setTotalNumberOfEntriesInCentralDirectoryOnThisDisk(
      int totalNumberOfEntriesInCentralDirectoryOnThisDisk) {
    this.totalNumberOfEntriesInCentralDirectoryOnThisDisk = totalNumberOfEntriesInCentralDirectoryOnThisDisk;
  }

  public int getTotalNumberOfEntriesInCentralDirectory() {
    return totalNumberOfEntriesInCentralDirectory;
  }

  public void setTotalNumberOfEntriesInCentralDirectory(int totNoOfEntrisInCentralDir) {
    this.totalNumberOfEntriesInCentralDirectory = totNoOfEntrisInCentralDir;
  }

  public int getSizeOfCentralDirectory() {
    return sizeOfCentralDirectory;
  }

  public void setSizeOfCentralDirectory(int sizeOfCentralDirectory) {
    this.sizeOfCentralDirectory = sizeOfCentralDirectory;
  }

  public long getOffsetOfStartOfCentralDirectory() {
    return offsetOfStartOfCentralDirectory;
  }

  public void setOffsetOfStartOfCentralDirectory(long offSetOfStartOfCentralDir) {
    this.offsetOfStartOfCentralDirectory = offSetOfStartOfCentralDir;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    if (comment != null) {
      this.comment = comment;
    }
  }

}

package com.zip.zipunzip.io.outputstream;

import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.io.outputstream.SplitOutputStream;

import java.io.IOException;
import java.io.OutputStream;

public class CountingOutputStream extends OutputStream {

  private OutputStream outputStream;
  private long numberOfBytesWritten = 0;

  public CountingOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
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
    outputStream.write(b, off, len);
    numberOfBytesWritten += len;
  }

  public int getCurrentSplitFileCounter() {
    if (isSplitZipFile()) {
      return ((SplitOutputStream) outputStream).getCurrentSplitFileCounter();
    }

    return 0;
  }

  public long getOffsetForNextEntry() throws IOException {
    if (outputStream instanceof SplitOutputStream) {
      return ((SplitOutputStream) outputStream).getFilePointer();
    }

    return numberOfBytesWritten;
  }

  public long getSplitLength() {
    if (isSplitZipFile()) {
      return ((SplitOutputStream) outputStream).getSplitLength();
    }

    return 0;
  }

  public boolean isSplitZipFile() {
    return outputStream instanceof SplitOutputStream
        && ((SplitOutputStream)outputStream).isSplitZipFile();
  }

  public long getNumberOfBytesWritten() throws IOException {
    if (outputStream instanceof SplitOutputStream) {
      return ((SplitOutputStream) outputStream).getFilePointer();
    }

    return numberOfBytesWritten;
  }

  public boolean checkBuffSizeAndStartNextSplitFile(int bufferSize) throws ZipException {
    if (!isSplitZipFile()) {
      return false;
    }

    return ((SplitOutputStream)outputStream).checkBufferSizeAndStartNextSplitFile(bufferSize);
  }

  public long getFilePointer() throws IOException {
    if (outputStream instanceof SplitOutputStream) {
      return ((SplitOutputStream) outputStream).getFilePointer();
    }

    return numberOfBytesWritten;
  }

  @Override
  public void close() throws IOException {
    outputStream.close();
  }
}

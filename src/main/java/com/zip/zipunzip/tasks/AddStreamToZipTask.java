package com.zip.zipunzip.tasks;

import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.headers.HeaderUtil;
import com.zip.zipunzip.headers.HeaderWriter;
import com.zip.zipunzip.io.outputstream.SplitOutputStream;
import com.zip.zipunzip.io.outputstream.ZipOutputStream;
import com.zip.zipunzip.model.FileHeader;
import com.zip.zipunzip.model.ZipModel;
import com.zip.zipunzip.model.ZipParameters;
import com.zip.zipunzip.model.enums.CompressionMethod;
import com.zip.zipunzip.progress.ProgressMonitor;
import com.zip.zipunzip.tasks.AddStreamToZipTask.AddStreamToZipTaskParameters;
import com.zip.zipunzip.tasks.RemoveEntryFromZipFileTask.RemoveEntryFromZipFileTaskParameters;
import com.zip.zipunzip.util.ZipUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static com.zip.zipunzip.util.InternalZipConstants.BUFF_SIZE;

public class AddStreamToZipTask extends AbstractAddFileToZipTask<AddStreamToZipTaskParameters> {

  public AddStreamToZipTask(ProgressMonitor progressMonitor, boolean runInThread, ZipModel zipModel, char[] password,
                            HeaderWriter headerWriter) {
    super(progressMonitor, runInThread, zipModel, password, headerWriter);
  }

  @Override
  protected void executeTask(AddStreamToZipTaskParameters taskParameters, ProgressMonitor progressMonitor)
      throws IOException {

    verifyZipParameters(taskParameters.zipParameters);

    if (!ZipUtil.isStringNotNullAndNotEmpty(taskParameters.zipParameters.getFileNameInZip())) {
      throw new ZipException("fileNameInZip has to be set in zipParameters when adding stream");
    }

    removeFileIfExists(getZipModel(), taskParameters.charset, taskParameters.zipParameters.getFileNameInZip(), progressMonitor);

    // For streams, it is necessary to write extended local file header because of Zip standard encryption.
    // If we do not write extended local file header, zip standard encryption needs a crc upfront for key,
    // which cannot be calculated until we read the complete stream. If we use extended local file header,
    // last modified file time is used, or current system time if not available.
    taskParameters.zipParameters.setWriteExtendedLocalFileHeader(true);

    if (taskParameters.zipParameters.getCompressionMethod().equals(CompressionMethod.STORE)) {
      // Set some random value here. This will be updated again when closing entry
      taskParameters.zipParameters.setEntrySize(0);
    }

    try(SplitOutputStream splitOutputStream = new SplitOutputStream(getZipModel().getZipFile(), getZipModel().getSplitLength());
        ZipOutputStream zipOutputStream = initializeOutputStream(splitOutputStream, taskParameters.charset)) {

      byte[] readBuff = new byte[BUFF_SIZE];
      int readLen = -1;

      ZipParameters zipParameters = taskParameters.zipParameters;
      zipOutputStream.putNextEntry(zipParameters);

      if (!zipParameters.getFileNameInZip().endsWith("/") &&
          !zipParameters.getFileNameInZip().endsWith("\\")) {
        while ((readLen = taskParameters.inputStream.read(readBuff)) != -1) {
          zipOutputStream.write(readBuff, 0, readLen);
        }
      }

      FileHeader fileHeader = zipOutputStream.closeEntry();

      if (fileHeader.getCompressionMethod().equals(CompressionMethod.STORE)) {
        updateLocalFileHeader(fileHeader, splitOutputStream);
      }
    }
  }

  @Override
  protected long calculateTotalWork(AddStreamToZipTaskParameters taskParameters) {
    return 0;
  }

  private void removeFileIfExists(ZipModel zipModel, Charset charset, String fileNameInZip, ProgressMonitor progressMonitor)
      throws ZipException {

    FileHeader fileHeader = HeaderUtil.getFileHeader(zipModel, fileNameInZip);
    if (fileHeader  != null) {
      RemoveEntryFromZipFileTask removeEntryFromZipFileTask = new RemoveEntryFromZipFileTask(progressMonitor, false,
          zipModel);
      removeEntryFromZipFileTask.execute(new RemoveEntryFromZipFileTaskParameters(fileHeader, charset));
    }
  }

  public static class AddStreamToZipTaskParameters extends AbstractZipTaskParameters {
    private InputStream inputStream;
    private ZipParameters zipParameters;

    public AddStreamToZipTaskParameters(InputStream inputStream, ZipParameters zipParameters, Charset charset) {
      super(charset);
      this.inputStream = inputStream;
      this.zipParameters = zipParameters;
    }
  }
}

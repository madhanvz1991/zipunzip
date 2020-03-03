package com.zip.zipunzip.tasks;

import com.zip.zipunzip.io.inputstream.SplitInputStream;
import com.zip.zipunzip.io.inputstream.ZipInputStream;
import com.zip.zipunzip.model.FileHeader;
import com.zip.zipunzip.model.ZipModel;
import com.zip.zipunzip.progress.ProgressMonitor;
import com.zip.zipunzip.tasks.ExtractFileTask.ExtractFileTaskParameters;

import java.io.IOException;
import java.nio.charset.Charset;

public class ExtractFileTask extends AbstractExtractFileTask<ExtractFileTaskParameters> {

  private char[] password;
  private SplitInputStream splitInputStream;

  public ExtractFileTask(ProgressMonitor progressMonitor, boolean runInThread, ZipModel zipModel, char[] password) {
    super(progressMonitor, runInThread, zipModel);
    this.password = password;
  }

  @Override
  protected void executeTask(ExtractFileTaskParameters taskParameters, ProgressMonitor progressMonitor)
      throws IOException {
    try(ZipInputStream zipInputStream = createZipInputStream(taskParameters.fileHeader, taskParameters.charset)) {
      extractFile(zipInputStream, taskParameters.fileHeader, taskParameters.outputPath, taskParameters.newFileName,
          progressMonitor);
    } finally {
      if (splitInputStream != null) {
        splitInputStream.close();
      }
    }
  }

  @Override
  protected long calculateTotalWork(ExtractFileTaskParameters taskParameters) {
    return taskParameters.fileHeader.getUncompressedSize();
  }

  protected ZipInputStream createZipInputStream(FileHeader fileHeader, Charset charset) throws IOException {
    splitInputStream = new SplitInputStream(getZipModel().getZipFile(),
        getZipModel().isSplitArchive(), getZipModel().getEndOfCentralDirectoryRecord().getNumberOfThisDisk());
    splitInputStream.prepareExtractionForFileHeader(fileHeader);
    return new ZipInputStream(splitInputStream, password, charset);
  }

  public static class ExtractFileTaskParameters extends AbstractZipTaskParameters {
    private String outputPath;
    private FileHeader fileHeader;
    private String newFileName;

    public ExtractFileTaskParameters(String outputPath, FileHeader fileHeader, String newFileName, Charset charset) {
      super(charset);
      this.outputPath = outputPath;
      this.fileHeader = fileHeader;
      this.newFileName = newFileName;
    }
  }
}

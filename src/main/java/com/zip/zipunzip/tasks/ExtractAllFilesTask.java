package com.zip.zipunzip.tasks;

import com.zip.zipunzip.io.inputstream.SplitInputStream;
import com.zip.zipunzip.io.inputstream.ZipInputStream;
import com.zip.zipunzip.model.FileHeader;
import com.zip.zipunzip.model.ZipModel;
import com.zip.zipunzip.progress.ProgressMonitor;
import com.zip.zipunzip.tasks.ExtractAllFilesTask.ExtractAllFilesTaskParameters;

import java.io.IOException;
import java.nio.charset.Charset;

public class ExtractAllFilesTask extends AbstractExtractFileTask<ExtractAllFilesTaskParameters> {

  private char[] password;
  private SplitInputStream splitInputStream;

  public ExtractAllFilesTask(ProgressMonitor progressMonitor, boolean runInThread, ZipModel zipModel, char[] password) {
    super(progressMonitor, runInThread, zipModel);
    this.password = password;
  }

  @Override
  protected void executeTask(ExtractAllFilesTaskParameters taskParameters, ProgressMonitor progressMonitor)
      throws IOException {
    try (ZipInputStream zipInputStream = prepareZipInputStream(taskParameters.charset)) {
      for (FileHeader fileHeader : getZipModel().getCentralDirectory().getFileHeaders()) {
        if (fileHeader.getFileName().startsWith("__MACOSX")) {
          progressMonitor.updateWorkCompleted(fileHeader.getUncompressedSize());
          continue;
        }

        splitInputStream.prepareExtractionForFileHeader(fileHeader);

        extractFile(zipInputStream, fileHeader, taskParameters.outputPath, null, progressMonitor);
        System.out.println("Successfully unzipped the Files and Folders!!!!! \n");
        verifyIfTaskIsCancelled();
      }
    } finally {
      if (splitInputStream != null) {
        splitInputStream.close();
      }
    }
  }

  @Override
  protected long calculateTotalWork(ExtractAllFilesTaskParameters taskParameters) {
    long totalWork = 0;

    for (FileHeader fileHeader : getZipModel().getCentralDirectory().getFileHeaders()) {
      if (fileHeader.getZip64ExtendedInfo() != null &&
          fileHeader.getZip64ExtendedInfo().getUncompressedSize() > 0) {
        totalWork += fileHeader.getZip64ExtendedInfo().getUncompressedSize();
      } else {
        totalWork += fileHeader.getUncompressedSize();
      }
    }

    return totalWork;
  }

  private ZipInputStream prepareZipInputStream(Charset charset) throws IOException {
    splitInputStream = new SplitInputStream(getZipModel().getZipFile(),
        getZipModel().isSplitArchive(), getZipModel().getEndOfCentralDirectoryRecord().getNumberOfThisDisk());

    FileHeader fileHeader = getFirstFileHeader(getZipModel());
    if (fileHeader != null) {
      splitInputStream.prepareExtractionForFileHeader(fileHeader);
    }

    return new ZipInputStream(splitInputStream, password, charset);
  }

  private FileHeader getFirstFileHeader(ZipModel zipModel) {
    if (zipModel.getCentralDirectory() == null
        || zipModel.getCentralDirectory().getFileHeaders() == null
        || zipModel.getCentralDirectory().getFileHeaders().size() == 0) {
      return null;
    }

    return zipModel.getCentralDirectory().getFileHeaders().get(0);
  }

  public static class ExtractAllFilesTaskParameters extends AbstractZipTaskParameters {
    private String outputPath;

    public ExtractAllFilesTaskParameters(String outputPath, Charset charset) {
      super(charset);
      this.outputPath = outputPath;
    }
  }

}

package com.zip.zipunzip.tasks;

import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.headers.HeaderWriter;
import com.zip.zipunzip.model.ZipModel;
import com.zip.zipunzip.model.ZipParameters;
import com.zip.zipunzip.progress.ProgressMonitor;
import com.zip.zipunzip.tasks.AddFilesToZipTask.AddFilesToZipTaskParameters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class AddFilesToZipTask extends AbstractAddFileToZipTask<AddFilesToZipTaskParameters> {

  public AddFilesToZipTask(ProgressMonitor progressMonitor, boolean runInThread, ZipModel zipModel, char[] password,
                           HeaderWriter headerWriter) {
    super(progressMonitor, runInThread, zipModel, password, headerWriter);
  }

  @Override
  protected void executeTask(AddFilesToZipTaskParameters taskParameters, ProgressMonitor progressMonitor)
      throws IOException {

    verifyZipParameters(taskParameters.zipParameters);
    addFilesToZip(taskParameters.filesToAdd, progressMonitor, taskParameters.zipParameters, taskParameters.charset);
  }

  @Override
  protected long calculateTotalWork(AddFilesToZipTaskParameters taskParameters) throws ZipException {
    return calculateWorkForFiles(taskParameters.filesToAdd, taskParameters.zipParameters);
  }

  @Override
  protected ProgressMonitor.Task getTask() {
    return super.getTask();
  }

  public static class AddFilesToZipTaskParameters extends AbstractZipTaskParameters {
    private List<File> filesToAdd;
    private ZipParameters zipParameters;

    public AddFilesToZipTaskParameters(List<File> filesToAdd, ZipParameters zipParameters, Charset charset) {
      super(charset);
      this.filesToAdd = filesToAdd;
      this.zipParameters = zipParameters;
    }
  }
}

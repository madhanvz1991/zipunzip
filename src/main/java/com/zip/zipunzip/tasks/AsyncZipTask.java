package com.zip.zipunzip.tasks;



import java.io.IOException;
import java.util.concurrent.Executors;

import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.progress.ProgressMonitor;

public abstract class AsyncZipTask<T> {

  private ProgressMonitor progressMonitor;
  private boolean runInThread;

  public AsyncZipTask(ProgressMonitor progressMonitor, boolean runInThread) {
    this.progressMonitor = progressMonitor;
    this.runInThread = runInThread;
  }

  public void execute(T taskParameters) throws ZipException {
    progressMonitor.fullReset();
    progressMonitor.setState(ProgressMonitor.State.BUSY);
    progressMonitor.setCurrentTask(getTask());

    if (runInThread) {
      long totalWorkToBeDone = calculateTotalWork(taskParameters);
      progressMonitor.setTotalWork(totalWorkToBeDone);

      Executors.newSingleThreadExecutor().execute(() -> {
        try {
          performTaskWithErrorHandling(taskParameters, progressMonitor);
        } catch (ZipException e) {
          //Do nothing. Exception will be passed through progress monitor
        }
      });
    } else {
      performTaskWithErrorHandling(taskParameters, progressMonitor);
    }
  }

  private void performTaskWithErrorHandling(T taskParameters, ProgressMonitor progressMonitor) throws ZipException {
    try {
      executeTask(taskParameters, progressMonitor);
      progressMonitor.endProgressMonitor();
      System.out.println("Successfully zipped the Files and Folders!!!!! \n");
    } catch (ZipException e) {
      progressMonitor.endProgressMonitor(e);
      throw e;
    } catch (Exception e) {
      progressMonitor.endProgressMonitor(e);
      throw new ZipException(e);
    }
  }

  protected void verifyIfTaskIsCancelled() throws ZipException {
    if (!progressMonitor.isCancelAllTasks()) {
      return;
    }

    progressMonitor.setResult(ProgressMonitor.Result.CANCELLED);
    progressMonitor.setState(ProgressMonitor.State.READY);
    throw new ZipException("Task cancelled", ZipException.Type.TASK_CANCELLED_EXCEPTION);
  }

  protected abstract void executeTask(T taskParameters, ProgressMonitor progressMonitor) throws IOException;

  protected abstract long calculateTotalWork(T taskParameters) throws ZipException;

  protected abstract ProgressMonitor.Task getTask();
}

package com.zip.zipunzip.tasks;

import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.headers.HeaderWriter;
import com.zip.zipunzip.io.outputstream.SplitOutputStream;
import com.zip.zipunzip.io.outputstream.ZipOutputStream;
import com.zip.zipunzip.model.FileHeader;
import com.zip.zipunzip.model.ZipModel;
import com.zip.zipunzip.model.ZipParameters;
import com.zip.zipunzip.model.enums.CompressionMethod;
import com.zip.zipunzip.model.enums.EncryptionMethod;
import com.zip.zipunzip.progress.ProgressMonitor;
import com.zip.zipunzip.tasks.RemoveEntryFromZipFileTask.RemoveEntryFromZipFileTaskParameters;
import com.zip.zipunzip.util.FileUtils;
import com.zip.zipunzip.util.ZipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.zip.zipunzip.headers.HeaderUtil.getFileHeader;
import static com.zip.zipunzip.model.enums.CompressionMethod.DEFLATE;
import static com.zip.zipunzip.model.enums.CompressionMethod.STORE;
import static com.zip.zipunzip.model.enums.EncryptionMethod.NONE;
import static com.zip.zipunzip.model.enums.EncryptionMethod.ZIP_STANDARD;
import static com.zip.zipunzip.progress.ProgressMonitor.Task.ADD_ENTRY;
import static com.zip.zipunzip.progress.ProgressMonitor.Task.CALCULATE_CRC;
import static com.zip.zipunzip.progress.ProgressMonitor.Task.REMOVE_ENTRY;
import static com.zip.zipunzip.util.CrcUtil.computeFileCrc;
import static com.zip.zipunzip.util.FileUtils.getRelativeFileName;
import static com.zip.zipunzip.util.InternalZipConstants.BUFF_SIZE;
import static com.zip.zipunzip.util.ZipUtil.javaToDosTime;

public abstract class AbstractAddFileToZipTask<T> extends AsyncZipTask<T> {

  private ZipModel zipModel;
  private char[] password;
  private HeaderWriter headerWriter;

  AbstractAddFileToZipTask(ProgressMonitor progressMonitor, boolean runInThread, ZipModel zipModel,
                           char[] password, HeaderWriter headerWriter) {
    super(progressMonitor, runInThread);
    this.zipModel = zipModel;
    this.password = password;
    this.headerWriter = headerWriter;
  }

  void addFilesToZip(List<File> filesToAdd, ProgressMonitor progressMonitor, ZipParameters zipParameters, Charset charset)
      throws IOException {

    List<File> updatedFilesToAdd = removeFilesIfExists(filesToAdd, zipParameters, progressMonitor, charset);

    try (SplitOutputStream splitOutputStream = new SplitOutputStream(zipModel.getZipFile(), zipModel.getSplitLength());
         ZipOutputStream zipOutputStream = initializeOutputStream(splitOutputStream, charset)) {
      byte[] readBuff = new byte[BUFF_SIZE];
      int readLen = -1;

      for (File fileToAdd : updatedFilesToAdd) {
        verifyIfTaskIsCancelled();
        ZipParameters clonedZipParameters = cloneAndAdjustZipParameters(zipParameters, fileToAdd, progressMonitor);
        progressMonitor.setFileName(fileToAdd.getAbsolutePath());

        zipOutputStream.putNextEntry(clonedZipParameters);
        if (fileToAdd.isDirectory()) {
          zipOutputStream.closeEntry();
          continue;
        }

        try (InputStream inputStream = new FileInputStream(fileToAdd)) {
          while ((readLen = inputStream.read(readBuff)) != -1) {
            zipOutputStream.write(readBuff, 0, readLen);
            progressMonitor.updateWorkCompleted(readLen);
            verifyIfTaskIsCancelled();
          }
        }

        FileHeader fileHeader = zipOutputStream.closeEntry();
        fileHeader.setExternalFileAttributes(FileUtils.getFileAttributes(fileToAdd));

        updateLocalFileHeader(fileHeader, splitOutputStream);
      }
    }
  }

  long calculateWorkForFiles(List<File> filesToAdd, ZipParameters zipParameters) throws ZipException {
    long totalWork = 0;

    for (File fileToAdd : filesToAdd) {
      if (!fileToAdd.exists()) {
        continue;
      }

      if (zipParameters.isEncryptFiles() && zipParameters.getEncryptionMethod() == EncryptionMethod.ZIP_STANDARD) {
        totalWork += (fileToAdd.length() * 2); // for CRC calculation
      } else {
        totalWork += fileToAdd.length();
      }

      //If an entry already exists, we have to remove that entry first and then add content again.
      //In this case, add corresponding work
      String relativeFileName = getRelativeFileName(fileToAdd.getAbsolutePath(), zipParameters);
      FileHeader fileHeader = getFileHeader(getZipModel(), relativeFileName);
      if (fileHeader != null) {
        totalWork += (getZipModel().getZipFile().length() - fileHeader.getCompressedSize());
      }
    }

    return totalWork;
  }

  ZipOutputStream initializeOutputStream(SplitOutputStream splitOutputStream, Charset charset) throws IOException {
    if (zipModel.getZipFile().exists()) {
      if (zipModel.getEndOfCentralDirectoryRecord() == null) {
        throw new ZipException("invalid end of central directory record");
      }
      splitOutputStream.seek(zipModel.getEndOfCentralDirectoryRecord().getOffsetOfStartOfCentralDirectory());
    }

    return new ZipOutputStream(splitOutputStream, password, charset, zipModel);
  }

  void verifyZipParameters(ZipParameters parameters) throws ZipException {
    if (parameters == null) {
      throw new ZipException("cannot validate zip parameters");
    }

    if (parameters.getCompressionMethod() != STORE && parameters.getCompressionMethod() != DEFLATE) {
      throw new ZipException("unsupported compression type");
    }

    if (parameters.isEncryptFiles()) {
      if (parameters.getEncryptionMethod() == NONE) {
        throw new ZipException("Encryption method has to be set, when encrypt files flag is set");
      }

      if (password == null || password.length <= 0) {
        throw new ZipException("input password is empty or null");
      }
    } else {
      parameters.setEncryptionMethod(NONE);
    }
  }

  void updateLocalFileHeader(FileHeader fileHeader, SplitOutputStream splitOutputStream) throws IOException {
    headerWriter.updateLocalFileHeader(fileHeader, getZipModel(), splitOutputStream);
  }

  private ZipParameters cloneAndAdjustZipParameters(ZipParameters zipParameters, File fileToAdd,
                                                    ProgressMonitor progressMonitor) throws IOException {
    ZipParameters clonedZipParameters = new ZipParameters(zipParameters);
    clonedZipParameters.setLastModifiedFileTime(javaToDosTime((fileToAdd.lastModified())));

    if (fileToAdd.isDirectory()) {
      clonedZipParameters.setEntrySize(0);
    } else {
      clonedZipParameters.setEntrySize(fileToAdd.length());
    }

    clonedZipParameters.setWriteExtendedLocalFileHeader(false);
    clonedZipParameters.setLastModifiedFileTime(fileToAdd.lastModified());

    if (!ZipUtil.isStringNotNullAndNotEmpty(zipParameters.getFileNameInZip())) {
      String relativeFileName = getRelativeFileName(fileToAdd.getAbsolutePath(), zipParameters);
      clonedZipParameters.setFileNameInZip(relativeFileName);
    }

    if (fileToAdd.isDirectory()) {
      clonedZipParameters.setCompressionMethod(CompressionMethod.STORE);
      clonedZipParameters.setEncryptionMethod(EncryptionMethod.NONE);
      clonedZipParameters.setEncryptFiles(false);
    } else {
      if (clonedZipParameters.isEncryptFiles() && clonedZipParameters.getEncryptionMethod() == ZIP_STANDARD) {
        progressMonitor.setCurrentTask(CALCULATE_CRC);
        clonedZipParameters.setEntryCRC(computeFileCrc(fileToAdd, progressMonitor));
        progressMonitor.setCurrentTask(ADD_ENTRY);
      }

      if (fileToAdd.length() == 0) {
        clonedZipParameters.setCompressionMethod(CompressionMethod.STORE);
      }
    }

    return clonedZipParameters;
  }

  private List<File> removeFilesIfExists(List<File> files, ZipParameters zipParameters, ProgressMonitor progressMonitor, Charset charset)
      throws ZipException {

    List<File> filesToAdd = new ArrayList<>(files);
    if (!zipModel.getZipFile().exists()) {
      return filesToAdd;
    }

    for (File file : files) {
      String fileName = getRelativeFileName(file.getAbsolutePath(), zipParameters);

      FileHeader fileHeader = getFileHeader(zipModel, fileName);
      if (fileHeader != null) {
        if (zipParameters.isOverrideExistingFilesInZip()) {
          progressMonitor.setCurrentTask(REMOVE_ENTRY);
          removeFile(fileHeader, progressMonitor, charset);
          verifyIfTaskIsCancelled();
          progressMonitor.setCurrentTask(ADD_ENTRY);
        } else {
          filesToAdd.remove(file);
        }
      }
    }

    return filesToAdd;
  }

  private void removeFile(FileHeader fileHeader, ProgressMonitor progressMonitor, Charset charset) throws ZipException {
    RemoveEntryFromZipFileTask removeEntryFromZipFileTask = new RemoveEntryFromZipFileTask(progressMonitor, false,
        zipModel);
    removeEntryFromZipFileTask.execute(new RemoveEntryFromZipFileTaskParameters(fileHeader, charset));
  }

  @Override
  protected ProgressMonitor.Task getTask() {
    return ProgressMonitor.Task.ADD_ENTRY;
  }

  protected ZipModel getZipModel() {
    return zipModel;
  }
}

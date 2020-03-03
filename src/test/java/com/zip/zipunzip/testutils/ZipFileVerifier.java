package com.zip.zipunzip.testutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.zip.zipunzip.ZipFile;
import com.zip.zipunzip.progress.ProgressMonitor;
import com.zip.zipunzip.util.CrcUtil;
import com.zip.zipunzip.util.FileUtils;
import com.zip.zipunzip.util.InternalZipConstants;

public class ZipFileVerifier {

  public static void verifyZipFileByExtractingAllFiles(File zipFileToExtract, File outputFolder,
                                                       int expectedNumberOfEntries) throws IOException {
    verifyZipFileByExtractingAllFiles(zipFileToExtract, null, outputFolder, expectedNumberOfEntries);
  }

  public static void verifyZipFileByExtractingAllFiles(File zipFileToExtract, char[] password, File outputFolder,
                                                       int expectedNumberOfEntries) throws IOException {
    verifyZipFileByExtractingAllFiles(zipFileToExtract, password, outputFolder, expectedNumberOfEntries, true);
  }

  public static void verifyZipFileByExtractingAllFiles(File zipFileToExtract, char[] password, File outputFolder,
                                                       int expectedNumberOfEntries, boolean verifyFileContents)
          throws IOException {
    verifyZipFileByExtractingAllFiles(zipFileToExtract, password, outputFolder, expectedNumberOfEntries, verifyFileContents, InternalZipConstants.CHARSET_UTF_8);
  }

  public static void verifyZipFileByExtractingAllFiles(File zipFileToExtract, char[] password, File outputFolder,
                                                       int expectedNumberOfEntries, boolean verifyFileContents, Charset charset)
      throws IOException {

    assertThat(zipFileToExtract).isNotNull();
    assertThat(zipFileToExtract).exists();

    ZipFile zipFile = new ZipFile(zipFileToExtract, password);

    if (charset != null) {
      zipFile.setCharset(charset);
    }

    zipFile.extractAll(outputFolder.getPath());
    assertThat(zipFile.getFileHeaders().size()).as("Number of file headers").isEqualTo(expectedNumberOfEntries);

    List<File> extractedFiles = FileUtils.getFilesInDirectoryRecursive(outputFolder, true, true);
    assertThat(extractedFiles).hasSize(expectedNumberOfEntries);

    if (verifyFileContents) {
      verifyFolderContentsSameAsSourceFiles(outputFolder);
    }
  }

  public static void verifyFileContent(File sourceFile, File extractedFile) throws IOException {
    assertThat(extractedFile.length()).isEqualTo(sourceFile.length());
    verifyFileCrc(sourceFile, extractedFile);
  }

  public static void verifyFolderContentsSameAsSourceFiles(File outputFolder) throws IOException {
    File[] filesInOutputFolder = outputFolder.listFiles();

    for (File file : filesInOutputFolder) {
      if (file.isDirectory()) {
        continue;
      }

      File sourceFile = TestUtils.getTestFileFromResources(file.getName());
      verifyFileContent(sourceFile, file);
    }
  }

  private static void verifyFileCrc(File sourceFile, File extractedFile) throws IOException {
    ProgressMonitor progressMonitor = new ProgressMonitor();
    long sourceFileCrc = CrcUtil.computeFileCrc(sourceFile, progressMonitor);
    long extractedFileCrc = CrcUtil.computeFileCrc(extractedFile, progressMonitor);

    assertThat(sourceFileCrc).isEqualTo(extractedFileCrc);
  }
}

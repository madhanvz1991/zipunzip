package com.zip.zipunzip;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.model.ZipParameters;

// Tests only failure scenarios. All other tests are covered in the corresponding Integration test
public class ZipFileTest {

  private File sourceZipFile;
  private ZipFile zipFile;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setup() {
    sourceZipFile = mockFile(false);
    zipFile = new ZipFile(sourceZipFile);
  }


  @Test
  public void testCreateZipFileFromFolderThrowsExceptionWheFolderIsNull() throws ZipException {
    expectedException.expect(ZipException.class);
    expectedException.expectMessage("folderToAdd is null, cannot create zip file from folder");

    zipFile.createSplitZipFileFromFolder(null, new ZipParameters(), true, 10000);
  }

  @Test
  public void testCreateZipFileFromFolderThrowsExceptionWhenParametersAreNull() throws ZipException {
    File folderToAdd = mockFile(true);

    expectedException.expect(ZipException.class);
    expectedException.expectMessage("input parameters are null, cannot create zip file from folder");

    zipFile.createSplitZipFileFromFolder(folderToAdd, null, true, 10000);
  }

  @Test
  public void testCreateZipFileFromFolderThrowsExceptionWhenZipFileExists() throws ZipException {
    reset(sourceZipFile);
    when(sourceZipFile.exists()).thenReturn(true);
    File folderToAdd = mockFile(true);

    expectedException.expect(ZipException.class);
    expectedException.expectMessage("zip file: " + sourceZipFile
        + " already exists. To add files to existing zip file use addFolder method");

    zipFile.createSplitZipFileFromFolder(folderToAdd, new ZipParameters(), true, 10000);
  }


  private File mockFile(boolean fileExists) {
    File file = mock(File.class);
    when(file.exists()).thenReturn(fileExists);
    when(file.toString()).thenReturn("SOME_PATH");
    return file;
  }

}
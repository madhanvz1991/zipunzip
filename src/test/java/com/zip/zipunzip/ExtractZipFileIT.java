package com.zip.zipunzip;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.zip.zipunzip.testutils.ZipFileVerifier;

public class ExtractZipFileIT extends AbstractIT {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testExtractAllSuccessfully() throws IOException {
		ZipFile zipFile = new ZipFile(generatedZipFile);
		zipFile.addFiles(FILES_TO_ADD);

		zipFile.extractAll(outputFolder.getPath());

		ZipFileVerifier.verifyFolderContentsSameAsSourceFiles(outputFolder);
		verifyNumberOfFilesInOutputFolder(outputFolder, 3);
	}

	private void verifyNumberOfFilesInOutputFolder(File outputFolder, int numberOfExpectedFiles) {
		assertThat(outputFolder.listFiles()).hasSize(numberOfExpectedFiles);
	}

}

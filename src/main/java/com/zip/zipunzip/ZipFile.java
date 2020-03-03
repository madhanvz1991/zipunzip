package com.zip.zipunzip;

import static com.zip.zipunzip.util.FileUtils.assertFilesExist;
import static com.zip.zipunzip.util.InternalZipConstants.CHARSET_UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import com.zip.zipunzip.exception.ZipException;
import com.zip.zipunzip.headers.HeaderReader;
import com.zip.zipunzip.headers.HeaderWriter;
import com.zip.zipunzip.model.FileHeader;
import com.zip.zipunzip.model.ZipModel;
import com.zip.zipunzip.model.ZipParameters;
import com.zip.zipunzip.model.enums.RandomAccessFileMode;
import com.zip.zipunzip.progress.ProgressMonitor;
import com.zip.zipunzip.tasks.AddFilesToZipTask;
import com.zip.zipunzip.tasks.AddFilesToZipTask.AddFilesToZipTaskParameters;
import com.zip.zipunzip.tasks.AddFolderToZipTask;
import com.zip.zipunzip.tasks.AddFolderToZipTask.AddFolderToZipTaskParameters;
import com.zip.zipunzip.tasks.ExtractAllFilesTask;
import com.zip.zipunzip.tasks.ExtractAllFilesTask.ExtractAllFilesTaskParameters;
import com.zip.zipunzip.tasks.MergeSplitZipFileTask;
import com.zip.zipunzip.tasks.MergeSplitZipFileTask.MergeSplitZipFileTaskParameters;
import com.zip.zipunzip.util.ZipUtil;

public class ZipFile {

	private File zipFile;
	private ZipModel zipModel;
	private ProgressMonitor progressMonitor;
	private boolean runInThread;
	private char[] password;
	private HeaderWriter headerWriter = new HeaderWriter();
	private Charset charset = CHARSET_UTF_8;

	
	public ZipFile(String zipFile) {
		this(new File(zipFile), null);
	}

	public ZipFile(String zipFile, char[] password) {
		this(new File(zipFile), password);
	}

	public ZipFile(File zipFile) {
		this(zipFile, null);
	}

	public ZipFile(File zipFile, char[] password) {
		this.zipFile = zipFile;
		this.password = password;
		this.runInThread = false;
		this.progressMonitor = new ProgressMonitor();
	}

	/**
	 * Creates a zip file and adds the files/folders from the specified folder to
	 * the zip file. creates split zip file
	 *
	 * @param folderToAdd
	 * @param parameters
	 * @param splitArchive
	 * @param splitLength
	 * @throws ZipException
	 */
	public void createSplitZipFileFromFolder(File folderToAdd, ZipParameters parameters, boolean splitArchive,
			long splitLength) throws ZipException {
		System.out.println("Compression Process Started!!! Pls wait.....");
		if (folderToAdd == null) {
			throw new ZipException("folderToAdd is null, cannot create zip file from folder");
		}

		if (parameters == null) {
			throw new ZipException("input parameters are null, cannot create zip file from folder");
		}

		if (zipFile.exists()) {
			throw new ZipException(
					"zip file: " + zipFile + " already exists. To add files to existing zip file use addFolder method");
		}

		createNewZipModel();
		zipModel.setSplitArchive(splitArchive);

		if (splitArchive) {
			zipModel.setSplitLength(splitLength);
		}

		addFolder(folderToAdd, parameters, false);
	}

	/**
	 * Creates a new instance of zip model
	 *
	 * @throws ZipException
	 */
	private void createNewZipModel() {
		zipModel = new ZipModel();
		zipModel.setZipFile(zipFile);
	}

	/**
	 * Internal method to add a folder to the zip file.
	 *
	 * @param folderToAdd
	 * @param zipParameters
	 * @param checkSplitArchive
	 * @throws ZipException
	 */
	private void addFolder(File folderToAdd, ZipParameters zipParameters, boolean checkSplitArchive)
			throws ZipException {

		//readZipInfo();

		if (zipModel == null) {
			throw new ZipException("internal error: zip model is null");
		}

		if (checkSplitArchive) {
			if (zipModel.isSplitArchive()) {
				throw new ZipException(
						"This is a split archive. Zip file format does not allow updating split/spanned files");
			}
		}

		new AddFolderToZipTask(progressMonitor, runInThread, zipModel, password, headerWriter)
				.execute(new AddFolderToZipTaskParameters(folderToAdd, zipParameters, charset));
	}

	/**
	 * Reads the zip header information for this zip file. If the zip file does not
	 * exist, it creates an empty zip model
	 *
	 * @throws ZipException
	 */
	private void readZipInfo() throws ZipException {
		if (zipModel != null) {
			return;
		}

		if (!zipFile.exists()) {
			createNewZipModel();
			return;
		}

		if (!zipFile.canRead()) {
			throw new ZipException("no read access for the input zip file");
		}

		try (RandomAccessFile randomAccessFile = new RandomAccessFile(zipFile, RandomAccessFileMode.READ.getValue())) {
			HeaderReader headerReader = new HeaderReader();
			zipModel = headerReader.readAllHeaders(randomAccessFile, charset);
			zipModel.setZipFile(zipFile);
		} catch (ZipException e) {
			throw e;
		} catch (IOException e) {
			throw new ZipException(e);
		}
	}

	/**
	 * Extracts all the files in the given zip file to the input destination path.
	 *
	 * @param destinationPath
	 * @throws ZipException
	 */
	public void extractAll(String destinationPath) throws ZipException {

		if (!ZipUtil.isStringNotNullAndNotEmpty(destinationPath)) {
			throw new ZipException("output path is null or invalid");
		}

		if (!ZipUtil.createDirectoryIfNotExists(new File(destinationPath))) {
			throw new ZipException("invalid output path");
		}

		if (zipModel == null) {
			readZipInfo();
		}

		// Throw an exception if zipModel is still null
		if (zipModel == null) {
			throw new ZipException("Internal error occurred when extracting zip file");
		}

		if (progressMonitor.getState() == ProgressMonitor.State.BUSY) {
			throw new ZipException("invalid operation - Zip4j is in busy state");
		}

		new ExtractAllFilesTask(progressMonitor, runInThread, zipModel, password)
				.execute(new ExtractAllFilesTaskParameters(destinationPath, charset));
	}
	
	/**
	   * Merges split zip files into a single zip file without the need to extract the
	   * files in the archive
	   *
	   * @param outputZipFile
	   * @throws ZipException
	   */
	  public void mergeSplitFiles(File outputZipFile) throws ZipException {
	    if (outputZipFile == null) {
	      throw new ZipException("outputZipFile is null, cannot merge split files");
	    }

	    if (outputZipFile.exists()) {
	      throw new ZipException("output Zip File already exists");
	    }

	    readZipInfo();

	    if (this.zipModel == null) {
	      throw new ZipException("zip model is null, corrupt zip file?");
	    }

	    new MergeSplitZipFileTask(progressMonitor, runInThread, zipModel).execute(
	            new MergeSplitZipFileTaskParameters(outputZipFile, charset));
	  }


	public void addFiles(List<File> filesToAdd) throws ZipException {
		addFiles(filesToAdd, new ZipParameters());
	}
	
	public void addFiles(List<File> filesToAdd, ZipParameters parameters) throws ZipException {

	    if (filesToAdd == null || filesToAdd.size() == 0) {
	      throw new ZipException("input file List is null or empty");
	    }

	    if (parameters == null) {
	      throw new ZipException("input parameters are null");
	    }

	    if (progressMonitor.getState() == ProgressMonitor.State.BUSY) {
	      throw new ZipException("invalid operation - Zip4j is in busy state");
	    }

	    assertFilesExist(filesToAdd);
	    readZipInfo();

	    if (zipModel == null) {
	      throw new ZipException("internal error: zip model is null");
	    }

	    if (zipFile.exists() && zipModel.isSplitArchive()) {
	      throw new ZipException("Zip file already exists. Zip file format does not allow updating split/spanned files");
	    }

	    new AddFilesToZipTask(progressMonitor, runInThread, zipModel, password, headerWriter).execute(
	        new AddFilesToZipTaskParameters(filesToAdd, parameters, charset));
	  }

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) throws IllegalArgumentException {
		if (charset == null) {
			throw new IllegalArgumentException("charset cannot be null");
		}
		this.charset = charset;
	}

	public List<FileHeader> getFileHeaders() throws ZipException {
		readZipInfo();
		if (zipModel == null || zipModel.getCentralDirectory() == null) {
			return Collections.emptyList();
		}
		return zipModel.getCentralDirectory().getFileHeaders();
	}

	@Override
	public String toString() {
		return zipFile.toString();
	}

}

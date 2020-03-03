

ZipUnzip - A java library for compression and decompression of files and directories
================================================================================

## About

ZipUnzip - A java library for compression and decompression of files and directories using file compression size in MB.
This library is available as an executable jar and can be used for compression and decompression of files and directories.

## Features
~~~
 * Compression of files and directories
 * Decompression of files and directories

~~~

## Usage

### Compression of files and directories

a) Compression Process

1. Please enter the Path to Input directory ::Input Path is ::(ex: C:\Samples)
   In Example: Samples folder can contain multiple files and multiple directories.

2. Please enter the Path to Output directory with zipfile Name ::(ex: C:\Samples\FileCompression.zip)
  
3. Please enter Maximum compressed size per file expressed in MB :: (ex: 10) (10MB)
 	You can observe the split files getting created and common zip file also created with the combined split files 
  	You can observe the output zip file size getting varied based on the file compression size input

Successfully zipped the Files and Folders!!!!!   	
Finally, output split files has been created successfully in the directory.

b) DeCompression Process

1. Please enter the Path to Input directory with zipfile Name ::(ex: C:\Samples\FileCompression.zip)
   In Example: Samples folder can contain multiple files and multiple directories.

2. Please enter the Path to Output directory with zipfile Name ::(ex: C:\Samples\Output)

Successfully zipped the Files and Folders!!!!! 
Finally, split files has been merged and extracted to the target folder

  	
Finally, output split files has been created successfully in the directory.

### Creating a split zip file

If you want to split the zip file over several files when the size exceeds a particular limit, you can do so like this:

~~~~
ZipFile zipFile = new ZipFile("filename.zip");
zipFile.createSplitZipFileFromFolder(new File(inputDirPath), new ZipParameters(), splitArchive,
						fileMaxCompressedSize);
~~~~

new ZipParameters() - these parameters can be used to specify compression model and compression level.

### DeCompression of files and directories

~~~~
ZipFile zipFile = new ZipFile(inputDirPath);
				zipFile.extractAll(outputDirPath);
~~~~



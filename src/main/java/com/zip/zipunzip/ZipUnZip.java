package com.zip.zipunzip;

import java.io.File;
import java.util.Scanner;

import com.zip.zipunzip.model.ZipParameters;

public class ZipUnZip {

	public void display_menu() {
		System.out.println("Compression & Decompression: ");
		System.out.println("\n");
		System.out.println("   1.    Compression Process");
		System.out.println("   2.    Decompression Process");
		System.out.println("   3.    Exit the program");
		System.out.println("\n");
		System.out.println("Select one of the options above: ");
	}

	{
		Scanner in = new Scanner(System.in);
		display_menu();

		switch (in.nextInt()) {
		case 1:
			try {
				Scanner scanner = new Scanner(System.in);
				System.out.print("Welcome to Compression Process!!! \n\n");
				System.out.print("Please enter the Path to Input directory ::");
				String inputDirPath = scanner.nextLine();
				System.out.println("Input Path is ::" + inputDirPath);

				System.out.print("Please enter the Path to Output directory with zipfile Name ::");
				String outputDirPath = scanner.nextLine();
				System.out.println("Output Path is ::" + outputDirPath);

				System.out.print("Please enter Maximum compressed size per file expressed in MB ::");
				long fileMaxCompressedSize = scanner.nextLong();
				fileMaxCompressedSize = fileMaxCompressedSize * 1024 * 1024;
				System.out.println("Output Path is ::" + outputDirPath);

				boolean splitArchive = true;

				ZipFile zipFile = new ZipFile(outputDirPath);
				zipFile.createSplitZipFileFromFolder(new File(inputDirPath), new ZipParameters(), splitArchive,
						fileMaxCompressedSize);
				new ZipUnZip();
				break;

			} catch (Exception e) {
				e.printStackTrace();
				System.err
						.println("Exception happened during zip/unzip process - Exception Message: " + e.getMessage());
				new ZipUnZip();
				break;
			}

		case 2:
			try {
				Scanner scanner = new Scanner(System.in);
				System.out.print("Welcome to De-Compression Process!!! \n\n");
				System.out.print("Please enter the Path to Input directory with zipfile Name ::");
				String inputDirPath = scanner.nextLine();
				System.out.println("Input Path is ::" + inputDirPath);

				System.out.print("Please enter the Path to Output directory :: ");
				String outputDirPath = scanner.nextLine();
				System.out.println("Output Path is ::" + outputDirPath);

				ZipFile zipFile = new ZipFile(inputDirPath);
				zipFile.extractAll(outputDirPath);
				
				new ZipUnZip();
				break;

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Exception happened during zip/unzip process - Exception Message: " + e.getMessage());
				new ZipUnZip();
				break;
			}

		case 3:
			System.out.println("Program terminated.");
			break;

		default:
			System.err.println("Please enter a valid option.");
			new ZipUnZip();
		}
	}

	public static void main(String[] args) {
		new ZipUnZip();
	}

}

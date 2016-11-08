package de.octofox.java.starcitizen.rsidownloader.helper;

import java.io.File;

public class FileTools {
    /**
     * returns the file extension from a given path
     * @param file path of the file
     * @return fileextension
     */
	public static String getFileExtension(String file) {
		String extension = "";

		int i = file.lastIndexOf('.');
		int p = Math.max(file.lastIndexOf('/'), file.lastIndexOf('\\'));

		if (i > p) {
		    extension = file.substring(i + 1);
		}

		return extension;
	}
	

	/**
	 * creates the given savefolder if needed
	 * @param saveFolder save folder to check and create
	 * @return true on success false on failure
	 */
	public static boolean createSaveFolderIfNotExist(File saveFolder) {
		if (!saveFolder.exists()) {
			if (!saveFolder.mkdirs()) {
				System.err.println("Creating the Folder failed. Run as Administrator.");
				return false;
			}
		}
		return true;
	}
}

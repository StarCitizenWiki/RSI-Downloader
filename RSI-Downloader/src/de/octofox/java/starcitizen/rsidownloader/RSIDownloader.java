package de.octofox.java.starcitizen.rsidownloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.octofox.java.starcitizen.rsidownloader.gui.GUIController;
import de.octofox.java.starcitizen.rsidownloader.helper.FileTools;
import de.octofox.java.starcitizen.rsidownloader.helper.RandomString;
import de.octofox.java.starcitizen.rsidownloader.helper.URLTools;
import de.octofox.java.starcitizen.rsidownloader.helper.Validate;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 *
 * @author FoXFTW
 * RSI Gallery Downloader
 */
public class RSIDownloader {

	private static final int BYTELEN = 2048;

	/**
	 * The current programms dir
	 */
	private static final String CWD = System.getProperty("user.dir");

	/**
	 * URL from where the Gallery URLs will be loaded
	 */
	public static final String SHIPURL = "https://star-citizen.wiki/Benutzer:FoXFTW/Ships.txt";

	/**
	 * Filename of the local URL list
	 */
	public static final String SOURCE_FILE = "ships.txt";

	private String saveFolder;

	/**
	 * GUIController, needed to set the content of the statuslabel
	 */
	private GUIController gc;

	/**
	 * Constructor
	 * @param gc GUIController
	 */
	public RSIDownloader(GUIController gc) {
		this.gc = gc;
		// TODO Set the Savefolder in the GUI
		this.setSaveFolder("");
	}

	/**
	 * Sets the Savepath, adds a / if the folder is not empty
	 * @param path Subfolder in the current working dir
	 */
	public void setSaveFolder(String path) {
		this.saveFolder = RSIDownloader.CWD + (path.isEmpty() ? "" : "/" + path) + "/";
	}


	/**
	 * Reads the ships.txt and adds each line as an URL
	 * @param sourceFile filename of the sourcefile
	 * @return GalleryURL Array with all URLs
	 */
	public GalleryURL[] getURLsFromFile(String sourceFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.saveFolder + sourceFile));
		    List<String> shipURLs = this.makeListFromReader(br);
		    GalleryURL[] urls = new GalleryURL[shipURLs.size()];
		    int i = 0;
		    for (String url : shipURLs) {
				urls[i] = new GalleryURL(url);
				i++;
			}
		    this.gc.status.set("URLs loaded");
		    return urls;
		} catch (Exception e) {
			this.gc.status.set(RSIDownloader.SOURCE_FILE + " missing");
			return null;
		}

	}


	/**
	 * Reads the online URL List and adds each line as an URL
	 * @param sourceFile filename of the sourcefile
	 * @return GalleryURL Array with all URLs
	 */
	public GalleryURL[] getURLsFromWeb(String sourceURL) {
		try {
			URL u = new URL(sourceURL);
			URLConnection conn = u.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    List<String> shipURLs = this.makeListFromReader(br);
		    GalleryURL[] urls = new GalleryURL[shipURLs.size()];
		    int i = 0;
		    for (String url : shipURLs) {
				urls[i] = new GalleryURL(url);
				i++;
			}
		    return urls;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * creates an ArrayList from a given BufferedReader
	 * @param br Reader of either source file or online list
	 * @return ArrayList with all URLs as strings
	 */
	private List<String> makeListFromReader(BufferedReader br) {
		List<String> shipURLs = new ArrayList<String>();

		try {
		    String line = null;
		    do {
		        line = br.readLine();
		        if (line != null && !line.trim().isEmpty()) {
		        	shipURLs.add(line);
				}
		    } while (line != null);

		    Collections.sort(shipURLs, new Comparator<String>() {
		        @Override
		        public int compare(String s1, String s2) {
		        	s1 = URLTools.getLastSubstring(s1);
		        	s2 = URLTools.getLastSubstring(s2);
		            return s1.compareToIgnoreCase(s2);
		        }
		    });
		    return shipURLs;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * starts the download for each array entry
	 * @param urlList String Array with all URLs
	 * @param saveFolder folder to save to
	 */
	public void startDownload(String[] urlList) {
		Task<Void> task = new Task<Void>() {
    		@Override
    		public Void call() throws Exception {
	    		for (String string : urlList) {
	    	        String folder = URLTools.getLastSubstring(string);
	    			if (folder != null) {
			    		Platform.runLater(new Runnable() {
				    		@Override
				    		public void run() {
				    			gc.status.set("Downloading " + folder);
				    		}
			    		});
	    				downloadImages(string, saveFolder + folder);
	    			}
	    		}
	    		Platform.runLater(new Runnable() {
		    		@Override
		    		public void run() {
		    			gc.status.set("Download finished");
		    		}
	    		});
				return null;
    		}
	    };
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
	}


	/**
	 * Downloads all images from the given Gallery URL
	 * @param galleryURL Gallery URL
	 * @param savePath path to save to
	 */
	private void downloadImages(String galleryURL, String savePath) {
		File saveFolder = new File(savePath);

		if (Validate.url(galleryURL) && FileTools.createSaveFolderIfNotExist(saveFolder)) {
			Document doc = URLTools.getURLContent(galleryURL);
			Elements gallery = doc.select(".ship-slideshow .slide img");
			String[][] urls = URLTools.parseImageUrl(gallery);

			int i = 0;
			for (String[] img : urls) {
    			final int j = i + 1;
	    		Platform.runLater(new Runnable() {
		    		@Override
		    		public void run() {
		    			gc.status.set("Downloading " + URLTools.getLastSubstring(savePath) + " (" + j + ")");
		    		}
	    		});
				saveImageToDisk(img[1], savePath + "/" + img[0]);
				i++;
			}
		}
	}


	/**
	 * saves the image to disk, adds a random substring if the image exist
	 * @param imageUrl url to load the image from
	 * @param filePath path to save the image to
	 * @return true on success
	 */
    private void saveImageToDisk(String imageUrl, String filePath) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            URL url = new URL(imageUrl);
            inputStream = url.openStream();
            if (new File(filePath).exists()) {
            	String extension = FileTools.getFileExtension(filePath);
            	filePath = filePath.replaceAll("." + extension, "");
            	filePath = filePath + "-" + RandomString.getAlNum(5) + "." + extension;
			}
            outputStream = new FileOutputStream(filePath);

            byte[] buffer = new byte[RSIDownloader.BYTELEN];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

        } catch (MalformedURLException e) {
        	this.gc.status.set("Not a valid URL (" + imageUrl + ")");
        } catch (FileNotFoundException e) {
        	this.gc.status.set("FileNotFoundException :- " + e.getMessage());
        } catch (IOException e) {
        	this.gc.status.set("IOException :- " + e.getMessage());
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) { }
        }
    }
}
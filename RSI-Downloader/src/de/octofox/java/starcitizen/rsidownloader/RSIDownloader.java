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

	private static final String CWD = System.getProperty("user.dir");

	public static final String SHIPURL = "https://star-citizen.wiki/Benutzer:FoXFTW/Ships.txt";
	public static final String SOURCE_FILE = "ships.txt";

	private String saveFolder;
	private GUIController gc;

	public RSIDownloader(GUIController gc) {
		this.gc = gc;
		// TODO
		this.setSaveFolder("");
	}

	public void setSaveFolder(String path) {
		this.saveFolder = RSIDownloader.CWD + (path.isEmpty() ? "" : "/" + path) + "/";
	}


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
		    this.gc.status.set("URLs loaded");
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

		    return shipURLs;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * starts the download for each array entry
	 * @param urlList
	 * @param saveFolder
	 */
	public void startDownload(String[] urlList) {
		for (String string : urlList) {
	        String folder = URLTools.getLastSubstring(string);
			if (folder != null) {
    			downloadImages(string, saveFolder + folder);
			}
		}
	}


	private void downloadImages(String galleryURL, String savePath) {
		File saveFolder = new File(savePath);

		if (Validate.url(galleryURL) && FileTools.createSaveFolderIfNotExist(saveFolder)) {
			Document doc = URLTools.getURLContent(galleryURL);
			Elements gallery = doc.select(".ship-slideshow .slide img");
			String[][] urls = URLTools.parseImageUrl(gallery);

			for (String[] img : urls) {
		    	Task<Void> task = new Task<Void>() {
		    		@Override
		    		public Void call() throws Exception {
			    		Platform.runLater(new Runnable() {
				    		@Override
				    		public void run() {
			    				saveImageToDisk(img[1], savePath + "/" + img[0]);
				    		}
			    		});
						return null;
		    		}
			    };
				Thread th = new Thread(task);
				th.setDaemon(true);
				th.start();
			    task.setOnSucceeded(e -> {
			    	gc.status.set("Download finished");
			    });
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
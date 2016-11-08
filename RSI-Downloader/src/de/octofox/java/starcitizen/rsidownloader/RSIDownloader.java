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

import de.octofox.java.starcitizen.rsidownloader.gui.RSI_Gui;
import de.octofox.java.starcitizen.rsidownloader.helper.FileTools;
import de.octofox.java.starcitizen.rsidownloader.helper.RandomString;
import de.octofox.java.starcitizen.rsidownloader.helper.URLTools;
import de.octofox.java.starcitizen.rsidownloader.helper.Validate;

/**
 * 
 * @author FoXFTW
 * RSI Gallery Downloader
 */
public class RSIDownloader {

	private static final int BYTELEN = 2048;

	private static final String HELP = 	"\nRSI Gallery Downloader by FoXFTW\n" +
										"Example: java -jar rsi.jar " +
										"https://robertsspaceindustries.com/pledge/ships/aegis-idris/Idris-M Idris-M\n\n" +
										"java -jar rsi.jar <URL> <Foldername>\n" +
										"Downloads all images from the given URL to the specified folder.\n\n" +
										"java -jar rsi.jar <Foldername>\n" +
										"Downloads all images from ships.txt to the specified folder, " +
										"ships will be saved in subfolders.\n\n" +
										"java -jar rsi.jar\n" +
										"Downloads all images to the folder where this .jar is located.\n\n\n" + 
										"java -jar rsi.jar online\n" +
										"Downloads all images from the online urllist (" + RSIDownloader.SHIPURL + ")\n\n" +
										"java -jar rsi.jar online <Foldername>\n" +										
										"Downloads all images from the online urllist and saves it to the specified folder" +
										"\n\n\n" +
										"Version: 0.0.1";	
	
	private static final String CWD = System.getProperty("user.dir");

	public static final String SHIPURL = "https://star-citizen.wiki/Benutzer:FoXFTW/Ships.txt";
	public static final String SOURCE_FILE = "ships.txt";
	
	private String saveFolder;
		
	/**
	 * Entrypoint
	 * @param args cmd args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			RSI_Gui.main(null);
		} else {
			RSIDownloader dl = new RSIDownloader();
			if (args.length == 2) {
				dl.setSaveFolder(args[1]);
				if (args[0].toLowerCase().equals("online")) {
					dl.startDownload(dl.getURLsFromWeb(RSIDownloader.SHIPURL + "?action=raw"));
				} else {
					String[] argList = {args[0]};
					dl.startDownload(argList);
				}
			} else if(args.length == 1) {
				if (args[0].toLowerCase().equals("online")) {
					dl.startDownload(dl.getURLsFromWeb(RSIDownloader.SHIPURL + "?action=raw"));
				} else {
					if (args[0].toLowerCase().equals("help")) {
						System.out.println(RSIDownloader.HELP);		
					} else {
						dl.setSaveFolder(args[0]);
						dl.startDownload(dl.getURLsFromFile(RSIDownloader.SOURCE_FILE));	
					}
				}
			} else {
				System.out.println(RSIDownloader.HELP);
			}
		}
	}

	
	public RSIDownloader() {
		this.setSaveFolder("");
	}
	
	
	public RSIDownloader(String saveFolderPath) {
		this.setSaveFolder(saveFolderPath);
	}
	

	public void setSaveFolder(String path) {
		this.saveFolder = RSIDownloader.CWD + (path.isEmpty() ? "" : "/" + path) + "/";
	}
	
	
	public String[] getURLsFromFile(String sourceFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.saveFolder + sourceFile));
		    List<String> shipURLs = this.makeListFromReader(br);
		    String[] urls = new String[shipURLs.size()];
		    shipURLs.toArray(urls);
		    return urls;
		} catch (Exception e) {
			System.err.println(RSIDownloader.SOURCE_FILE + " is missing");
			System.out.println(RSIDownloader.HELP);
		}
		return null;
	}
	
	
	public String[] getURLsFromWeb(String sourceURL) {
		try {
			URL u = new URL(sourceURL);
			URLConnection conn = u.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    List<String> shipURLs = this.makeListFromReader(br);
		    String[] urls = new String[shipURLs.size()];
		    shipURLs.toArray(urls);
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
				if (!this.downloadImages(string, this.saveFolder + folder)) {
					System.err.println("Error loading " + string + " - " + folder);
				}		        						
			}	
		}			
	}


	private boolean downloadImages(String galleryURL, String savePath) {
		File saveFolder = new File(savePath);

		if (Validate.url(galleryURL) && FileTools.createSaveFolderIfNotExist(saveFolder)) {	
			Document doc = URLTools.getURLContent(galleryURL);
			Elements gallery = doc.select(".ship-slideshow .slide img");
			String[][] urls = URLTools.parseImageUrl(gallery);
			
			int i = 0;
			for (String[] img : urls) {
				if (!this.saveImageToDisk(img[1], savePath + "/" + img[0])) {
					System.err.println("Error downloading " + img[0]);
				} else {
					i++;
				}
			}
			
			String curStatus = "Downloaded " + URLTools.getLastSubstring(galleryURL) + " (" + i + " images)";
			System.out.println(curStatus);
			return true;	
		}
		
		return false;
	}

	
	/**
	 * saves the image to disk, adds a random substring if the image exist
	 * @param imageUrl url to load the image from
	 * @param filePath path to save the image to
	 * @return true on success
	 */
    private boolean saveImageToDisk(String imageUrl, String filePath) {
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
            System.err.println("Not a valid URL");
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException :- " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException :- " + e.getMessage());
        } finally {
            try {
                inputStream.close();
                outputStream.close();
                return true;
            } catch (IOException e) { }
        }
        return false;
    }
}
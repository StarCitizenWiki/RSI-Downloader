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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author FoXFTW
 * RSI Gallery Downloader
 */
public class RSIDownloader {

	private static final int BYTELEN = 2048;
	private static final String ALNUM_SEQ = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

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
	private static final String RSI_PROTO = "https://";
	private static final String RSIURL = "robertsspaceindustries.com/";
	private static final String SHIPURL = "https://star-citizen.wiki/Benutzer:FoXFTW/Ships.txt";
	private static final String SOURCE_FILE = "ships.txt";
	
	/**
	 * Entrypoint
	 * @param args cmd args
	 */
	public static void main(String[] args) {
		switch (args.length) {
		case 0:
			RSIDownloader.startDownloadbyFile(SOURCE_FILE, "");
			break;
		
		case 1:
			switch (args[0].toLowerCase()) {
			case "help":
				System.out.println(RSIDownloader.HELP);				
				break;
				
			case "online":
				RSIDownloader.startDownloadbyURL(RSIDownloader.SHIPURL + "?action=raw", "");
				break;
				
			default:
				RSIDownloader.startDownloadbyFile(SOURCE_FILE, args[0]);
				break;
			}
			break;
			
		case 2:
			switch (args[0].toLowerCase()) {
			case "online":
				RSIDownloader.startDownloadbyURL(RSIDownloader.SHIPURL + "?action=raw", args[1]);
				break;
				
			default:
				RSIDownloader.downloadImage(args[0], args[1]);
				break;
			}			
			break;

		default:
			System.out.println(RSIDownloader.HELP);
			break;
		}
	}
	
	/**
	 * Downloads all urls from ships.txt
	 * @param saveFolder main folder for all saved ships
	 */
	private static void startDownloadbyFile(String sourceFile, String saveFolder) {
		saveFolder = parseSaveFolder(saveFolder);
		List<String> shipURLs = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(RSIDownloader.CWD + "/" + sourceFile))) {
		    String line = null;
		    do {
		        line = br.readLine();
		        if (line != null && !line.trim().isEmpty()) {
		        	shipURLs.add(line);
				}
		    } while (line != null);

		    String[] urls = new String[shipURLs.size()];
		    shipURLs.toArray(urls);
		    RSIDownloader.startDownload(urls, saveFolder);
		} catch (FileNotFoundException e) {
			System.err.println(RSIDownloader.SOURCE_FILE + " is missing");
			System.out.println(RSIDownloader.HELP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the shiplist from the given url
	 * @param sourceURL URL to load
	 * @param saveFolder folder to save to
	 */
	private static void startDownloadbyURL(String sourceURL, String saveFolder) {
		saveFolder = parseSaveFolder(saveFolder);
		List<String> shipURLs = new ArrayList<String>();
		try {
			URL u = new URL(sourceURL);
			URLConnection conn = u.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String line;
		    do {
		        line = in.readLine();
		        if (null != line && !line.trim().isEmpty()) {
		        	shipURLs.add(line);
				}
		    } while (line != null);
			in.close();	    
	
		    String[] urls = new String[shipURLs.size()];
		    shipURLs.toArray(urls);
		    RSIDownloader.startDownload(urls, saveFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * adds a pre slash if folder is not empty
	 * @param saveFolder
	 * @return
	 */
	private static String parseSaveFolder(String saveFolder) {
		saveFolder = (saveFolder.isEmpty() ? "" : "/" + saveFolder);
		return saveFolder;
	}
	
	/**
	 * starts the download for each array entry
	 * @param urlList
	 * @param saveFolder
	 */
	private static void startDownload(String[] urlList, String saveFolder) {
		for (String string : urlList) {
	        String folder = RSIDownloader.getLastSubstring(string);
			if (folder != null) {
				if (!RSIDownloader.downloadImage(string, saveFolder + "/" + folder)) {
					System.err.println("Error loading " + string + " - " + folder);
				}		        						
			}	
		}			
	}

	/**
	 * sets the filepath, and starts the saving process
	 * @param imageURL URL of the image
	 * @param folder foldername
	 * @return true if download succeded false on failure
	 */
	private static boolean downloadImage(String imageURL, String folder) {
		String savePath = RSIDownloader.CWD + "/" + folder;
		File saveFolder = new File(savePath);

		if (isValidUrl(imageURL) && createSaveFolderIfNotExistent(saveFolder)) {
			Document doc = getURLContent(imageURL);
			Elements gallery = doc.select(".ship-slideshow .slide img");
			String[][] urls = parseImageUrl(gallery);
			
			int i = 0;
			for (String[] img : urls) {
				if (!RSIDownloader.saveImageToDisk(img[1], savePath + "/" + img[0])) {
					System.err.println("Error downloading " + img[0]);
				} else {
					i++;
				}
			}
			System.out.println(folder + " Downloaded " + i + " images");
			return true;	
		}
		
		return false;
	}

	/**
	 * returns the sourcecode from one url
	 * @param contentURL URL to get the content from
	 * @return Document class
	 */
	private static Document getURLContent(String contentURL) {
		Document doc = null;
		try {
			doc = Jsoup.connect(contentURL).get();
		} catch (IOException e) {
			System.err.println("Problem while fetching the URL");
		}
		return doc;
	}

	/**
	 * extracts the src from the elements and adds the url if needed, generates hte filename
	 * @param gallery all elements from the ship gallery
	 * @return String[][] array with 0 name and 1 abs url
	 */
	private static String[][] parseImageUrl(Elements gallery) {
		String[][] urls = new String[gallery.size()][2];
		
		int i = 0;
		for (Element img : gallery) {
			String tmp = img.attr("src");
			tmp = tmp.replaceAll("store_slideshow_large", "source");
			if (!tmp.toLowerCase().contains(RSIDownloader.RSIURL)) {
				tmp = RSIDownloader.RSI_PROTO + RSIDownloader.RSIURL + tmp;				
			}
			urls[i][0] = RSIDownloader.getLastSubstring(tmp);
			urls[i][1] = tmp;
			i++;
		}
		return urls;
	}

	/**
	 * creates the given savefolder if needed
	 * @param saveFolder save folder to check and create
	 * @return true on success false on failure
	 */
	private static boolean createSaveFolderIfNotExistent(File saveFolder) {
		if (!saveFolder.exists()) {
			if (!saveFolder.mkdirs()) {
				System.err.println("Creating the Folder failed. Run as Administrator.");
				return false;
			}
		}
		return true;
	}

	/**
	 * hacky approach to check if the given url is valid
	 * @param rawURL URL to check
	 * @return true if valid false if invalid
	 */
	private static boolean isValidUrl(String rawURL) {
		URL u;
		try {
			u = new URL(rawURL);
			u.getProtocol();
			return true;
		} catch (MalformedURLException e) {
			System.err.println("This is not a valid URL!");
			return false;
		}
	}

	/**
	 * returns the last / substring from an url
	 * @param tmp url to get the last substring from
	 * @return null if null substring in not empty
	 */
	private static String getLastSubstring(String tmp) {
		if (tmp == null) {
			return null;
		}
		return tmp.substring( tmp.lastIndexOf('/') + 1, tmp.length() );
	}
	
	/**
	 * generates a random alphanumeric string with a given length
	 * @param len length of the string
	 * @return random alphanumeric substring with given length
	 */
	private static String randomString( int len ) {

		SecureRandom rnd = new SecureRandom();
		StringBuilder sb = new StringBuilder( len );
        
		for ( int i = 0; i < len; i++ ) {
        	sb.append( RSIDownloader.ALNUM_SEQ.charAt( rnd.nextInt(RSIDownloader.ALNUM_SEQ.length()) ) );
        }
        return sb.toString();
    }
	
	/**
	 * saves the image to disk, adds a random substring if the image exist
	 * @param imageUrl url to load the image from
	 * @param filePath path to save the image to
	 * @return true on success
	 */
    private static boolean saveImageToDisk(String imageUrl, String filePath) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
 
        try {
            URL url = new URL(imageUrl);
            inputStream = url.openStream();
            if (new File(filePath).exists()) {
            	String extension = getFileExtension(filePath);
            	filePath = filePath.replaceAll("." + extension, "");
            	filePath = filePath + "-" + RSIDownloader.randomString(5) + "." + extension;
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

    /**
     * returns the file extension from a given path
     * @param filePath path of the file
     * @return fileextension
     */
	private static String getFileExtension(String filePath) {
		String extension = "";

		int i = filePath.lastIndexOf('.');
		int p = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

		if (i > p) {
		    extension = filePath.substring(i + 1);
		}

		return extension;
	}
}

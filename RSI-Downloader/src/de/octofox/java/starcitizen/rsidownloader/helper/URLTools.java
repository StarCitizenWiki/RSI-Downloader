package de.octofox.java.starcitizen.rsidownloader.helper;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class URLTools {
	private static final String RSI_PROTO = "https://";
	private static final String RSIURL = "robertsspaceindustries.com/";
	
	/**
	 * extracts the src from the elements and adds the url if needed, generates hte filename
	 * @param gallery all elements from the ship gallery
	 * @return String[][] array with 0 name and 1 abs url
	 */
	public static String[][] parseImageUrl(Elements gallery) {
		String[][] urls = new String[gallery.size()][2];
		
		int i = 0;
		for (Element img : gallery) {
			String tmp = img.attr("src");
			tmp = tmp.replaceAll("store_slideshow_large", "source");
			if (!tmp.toLowerCase().contains(URLTools.RSIURL)) {
				tmp = URLTools.RSI_PROTO + URLTools.RSIURL + tmp;				
			}
			urls[i][0] = URLTools.getLastSubstring(tmp);
			urls[i][1] = tmp;
			i++;
		}
		return urls;
	}

	
	/**
	 * returns the last / substring from an url
	 * @param string url to get the last substring from
	 * @return null if null substring in not empty
	 */
	public static String getLastSubstring(String string) {
		if (string == null) {
			return null;
		}
		return string.substring( string.lastIndexOf('/') + 1, string.length() );
	}
	

	/**
	 * returns the sourcecode from one url
	 * @param contentURL URL to get the content from
	 * @return Document class
	 */
	public static Document getURLContent(String contentURL) {
		Document doc = null;
		try {
			doc = Jsoup.connect(contentURL).get();
		} catch (IOException e) {
			System.err.println("Problem while fetching the URL");
		}
		return doc;
	}	
}

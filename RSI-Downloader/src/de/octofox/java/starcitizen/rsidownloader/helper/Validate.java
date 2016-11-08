package de.octofox.java.starcitizen.rsidownloader.helper;

import java.net.MalformedURLException;
import java.net.URL;

public class Validate {
	/**
	 * hacky approach to check if the given url is valid
	 * @param rawURL URL to check
	 * @return true if valid false if invalid
	 */
	public static boolean url(String rawURL) {
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
}

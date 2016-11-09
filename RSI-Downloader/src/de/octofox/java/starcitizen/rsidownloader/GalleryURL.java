package de.octofox.java.starcitizen.rsidownloader;

import de.octofox.java.starcitizen.rsidownloader.helper.URLTools;
import de.octofox.java.starcitizen.rsidownloader.helper.Validate;

/**
 * GalleryURL class
 * Saves the last Substring as the Name of the URL
 * @author Hannes
 *
 */
public class GalleryURL {

	/**
	 * String URL of the Gallery
	 */
	private String url;

	/**
	 * Substring of the URL equals this name
	 */
	private String name;

	/**
	 * Constructor
	 * @param galleryURL the Gallery URL to process
	 */
	public GalleryURL(String galleryURL) {
		if (Validate.url(galleryURL)) {
			this.url = galleryURL;
			this.name = URLTools.getLastSubstring(galleryURL);
		}
	}

	/**
	 * returns the set URL
	 * @return URL String
	 */
	public String getURL() {
		return this.url;
	}

	/**
	 * returns the URL's name
	 */
	@Override
	public String toString() {
		return this.name;
	}
}

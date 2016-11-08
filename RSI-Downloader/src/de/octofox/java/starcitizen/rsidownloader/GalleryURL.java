package de.octofox.java.starcitizen.rsidownloader;

import de.octofox.java.starcitizen.rsidownloader.helper.URLTools;
import de.octofox.java.starcitizen.rsidownloader.helper.Validate;

public class GalleryURL {

	private String url;
	private String name;
	
	public GalleryURL(String galleryURL) {
		if (Validate.url(galleryURL)) {
			this.url = galleryURL;
			this.name = URLTools.getLastSubstring(galleryURL);
		}
	}
	
	public String getURL() {
		return this.url;
	}
	
	public String toString() {
		return this.name;
	}
}

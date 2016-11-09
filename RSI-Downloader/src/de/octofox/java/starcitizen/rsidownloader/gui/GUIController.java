package de.octofox.java.starcitizen.rsidownloader.gui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import de.octofox.java.starcitizen.rsidownloader.GalleryURL;
import de.octofox.java.starcitizen.rsidownloader.RSIDownloader;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

public class GUIController implements Initializable {
	/**
	 * GUI Elements
	 */
	@FXML private ListView<GalleryURL> listView;
    @FXML private Button download_button;
    @FXML private Button select_all_button;
    @FXML private Button select_none_button;
    @FXML private Label status_label;

    /**
     * Label Status
     */
    public SimpleStringProperty status;

    /**
     * Downloader Class
     */
    RSIDownloader dl;


    /**
     * Binds the status to the status label
     * initializes the List view with URLs
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	this.dl = new RSIDownloader(this);
    	this.status = new SimpleStringProperty();
    	status_label.textProperty().bind(this.status);
    	listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    	Task<Void> task = new Task<Void>() {
    		@Override
    		public Void call() throws Exception {
	    		Platform.runLater(new Runnable() {
		    		@Override
		    		public void run() {
		    			status.set("Loading URLs");
		    		}
	    		});
    			initList();
				return null;
    		}
		};
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();

	    task.setOnSucceeded(e -> {
	    	this.status.set("Done");
	    });
    }


    /**
     * checks if ships.txt exists, if not loads all URLs from the online repo
     * adds each URL to the ListView
     */
    private void initList() {
    	ObservableList<GalleryURL> items = listView.getItems();

    	GalleryURL[] urls;
    	if (new File(RSIDownloader.SOURCE_FILE).exists()) {
    		urls = dl.getURLsFromWeb(RSIDownloader.SOURCE_FILE);
		} else {
			urls = dl.getURLsFromWeb(RSIDownloader.SHIPURL + "?action=raw");
		}

        for (GalleryURL galleryURL : urls) {
        	items.add(galleryURL);
		}
    }


    /**
     * selects the whole list
     */
    public void selectAll() {
    	listView.getSelectionModel().selectAll();
    }


    /**
     * deselects the whole list
     */
    public void selectNone() {
    	listView.getSelectionModel().clearSelection();
    }


    /**
     * parses the selected list items and downloads them
     * @param event
     */
    public void downloadSelection(ActionEvent event) {
    	ObservableList<GalleryURL> items = listView.getSelectionModel().getSelectedItems();
    	String[] urls = new String[items.size()];

    	int i = 0;
    	for (GalleryURL galleryURL : items) {
			urls[i] = galleryURL.getURL();
			i++;
		}

    	Task<Void> task = new Task<Void>() {
    		@Override
    		public Void call() throws Exception {
	    		Platform.runLater(new Runnable() {
		    		@Override
		    		public void run() {
		    			status.set("Downloading...");
		    		}
	    		});
	    		dl.startDownload(urls);
				return null;
    		}
		};
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
	    task.setOnSucceeded(e -> {
	    	this.status.set("Download finished");
	    });
    }
}

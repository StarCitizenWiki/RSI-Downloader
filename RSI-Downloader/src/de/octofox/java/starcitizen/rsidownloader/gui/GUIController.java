package de.octofox.java.starcitizen.rsidownloader.gui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import de.octofox.java.starcitizen.rsidownloader.GalleryURL;
import de.octofox.java.starcitizen.rsidownloader.RSIDownloader;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;

public class GUIController implements Initializable {
	/**
	 * GUI Elements
	 */
	@FXML private ListView<GalleryURL> listView;
    @FXML private Button downloadButton;
    @FXML private Button selectAllButton;
    @FXML private Button selectNoneButton;
    @FXML private Label statusLabel;
    @FXML private TextField searchInput;

    private ObservableList<GalleryURL> ships;

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
    	this.statusLabel.textProperty().bind(this.status);

    	this.searchInput.textProperty().addListener(
			(ChangeListener<String>) (observable, oldVal, newVal) -> searchList(oldVal, newVal)
    	);

    	this.listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
    	ObservableList<GalleryURL> items = this.listView.getItems();

    	GalleryURL[] urls;
    	if (new File(RSIDownloader.SOURCE_FILE).exists()) {
    		urls = this.dl.getURLsFromFile(RSIDownloader.SOURCE_FILE);
		} else {
			urls = this.dl.getURLsFromWeb(RSIDownloader.SHIPURL + "?action=raw");
		}

        for (GalleryURL galleryURL : urls) {
        	items.add(galleryURL);
		}
        this.ships = items;
    }


    /**
     * selects the whole list
     */
    public void selectAll() {
    	this.listView.getSelectionModel().selectAll();
    }


    /**
     * deselects the whole list
     */
    public void selectNone() {
    	this.listView.getSelectionModel().clearSelection();
    }


    /**
     * allows to search the list "google style"
     * splits the input on whitespace and searches by substrings
     * restores everything on empty field or if newVal < oldVal (backspace)
     * @param oldVal old search string
     * @param newVal new search string
     */
    public void searchList(String oldVal, String newVal) {
        if ( oldVal != null && (newVal.length() < oldVal.length()) ) {
            this.listView.setItems( this.ships );
        }

        String[] parts = newVal.toUpperCase().split(" ");

        ObservableList<GalleryURL> subentries = FXCollections.observableArrayList();
        for ( GalleryURL entry: this.listView.getItems() ) {
            boolean match = true;
            GalleryURL entryText = entry;
            for ( String part: parts ) {
                if ( !entryText.toString().toUpperCase().contains(part) ) {
                    match = false;
                    break;
                }
            }

            if ( match ) {
                subentries.add(entryText);
            }
        }
        this.listView.setItems(subentries);
    }


    /**
     * parses the selected list items and downloads them
     * @param event
     */
    public void downloadSelection(ActionEvent event) {
    	ObservableList<GalleryURL> items = this.listView.getSelectionModel().getSelectedItems();
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

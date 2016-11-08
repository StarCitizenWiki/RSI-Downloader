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
    @FXML private ListView<String> listView;
    @FXML private Button download_button;
    @FXML private Button select_all_button;
    @FXML private Button select_none_button;
    @FXML private Label status_label;

    public SimpleStringProperty status;

    RSIDownloader dl;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	this.dl = new RSIDownloader(this);
    	this.status = new SimpleStringProperty();
    	status_label.textProperty().bind(this.status);
    	listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    	this.status.set("Starting");
    	Platform.runLater(new Runnable() {
    		@Override
    		public void run() {
    			initList();
    		}
    	});
    }

    private void initList() {
    	Task<Void> task = new Task<Void>() {
    		@Override
    		public Void call() throws Exception {
	    		Platform.runLater(new Runnable() {
		    		@Override
		    		public void run() {
		    	    	ObservableList<String> items = listView.getItems();

		    	    	GalleryURL[] urls;
		    	    	if (new File(RSIDownloader.SOURCE_FILE).exists()) {
		    	    		urls = dl.getURLsFromWeb(RSIDownloader.SOURCE_FILE);
						} else {
							urls = dl.getURLsFromWeb(RSIDownloader.SHIPURL + "?action=raw");
						}

		    	    	Platform.runLater(new Runnable() {
		    	    		@Override
		    	    		public void run() {
				    	        for (GalleryURL galleryURL : urls) {
				    	        	items.add(galleryURL.getURL());
				    			}
				    	        status.set("Done");
		    	    		}
		    	    	});
		    		}
	    		});
				return null;
    		}
		};
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
    }

    public void selectAll(ActionEvent event) {
    	listView.getSelectionModel().selectAll();
    }

    public void selectNone(ActionEvent event) {
    	listView.getSelectionModel().clearSelection();
    }

    public void setStatusLabel(String status) {
    	this.status_label.setText(status);
    }

    public void downloadSelection(ActionEvent event) {
    	ObservableList<String> items = listView.getSelectionModel().getSelectedItems();
    	String[] urls = new String[items.size()];

    	Task<Void> task = new Task<Void>() {
    		@Override
    		public Void call() throws Exception {
	    		Platform.runLater(new Runnable() {
		    		@Override
		    		public void run() {
		    			dl.startDownload(items.toArray(urls));
		    		}
	    		});
				return null;
    		}
		};
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();

	    task.setOnSucceeded(e -> {
	    	this.status.set("Downloading...");
	    });
    }

}

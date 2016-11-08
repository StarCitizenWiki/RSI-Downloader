package de.octofox.java.starcitizen.rsidownloader.gui;

import java.net.URL;
import java.util.ResourceBundle;

import de.octofox.java.starcitizen.rsidownloader.RSIDownloader;
import javafx.collections.ObservableList;
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
    
    RSIDownloader dl = new RSIDownloader();
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    	listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    	
    	ObservableList<String> items = listView.getItems();
        
        String[] urls = this.dl.getURLsFromWeb(RSIDownloader.SHIPURL + "?action=raw");
        
        for (String string : urls) {
        	items.add(string);
		}
    }

    public void selectAll(ActionEvent event) {
    	listView.getSelectionModel().selectAll();
    	this.setStatusLabel("All");
    }
    
    public void selectNone(ActionEvent event) {
    	listView.getSelectionModel().clearSelection();
    	this.setStatusLabel("None");
    }
    
    public void setStatusLabel(String status) {
    	this.status_label.setText(status);
    }
    
    public void downloadSelection(ActionEvent event) {
    	ObservableList<String> items = listView.getSelectionModel().getSelectedItems();
    	String[] urls = new String[items.size()];
    	urls = items.toArray(urls);
		this.dl.startDownload(urls);
    }

}

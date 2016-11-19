package de.octofox.java.starcitizen.rsidownloader.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class RSIGui extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("RSIGui.fxml"));

			Scene scene = new Scene(root);
			primaryStage.setTitle("RSI Downloader by FoXFTW");
			primaryStage.getIcons().add(new Image(RSIGui.class.getResourceAsStream("files/Wiki_Logo.png")));
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}

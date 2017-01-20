package application;


import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application{
	private static Stage window;
	@Override
	public void start(Stage primaryStage){
		try{
			// load the FXML
			window = primaryStage;
			FXMLLoader loader = new FXMLLoader(getClass().getResource("FaceDetection.fxml"));
			BorderPane root = (BorderPane) loader.load();
			//set style
			root.setStyle("-fx-background-color: whitesmoke;");
			//scene
			Scene scene = new Scene(root, 800, 600);
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			window.setTitle("Face Detection");
			window.setScene(scene);
			window.show();
			
			//initialize the variables of the controller class
			FaceDetectionController controller = loader.getController();
			controller.init();
			window.setOnCloseRequest(e -> {
				e.consume();
				checkifCameraClosed(controller.getCameraStatus());
			});
			
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static Stage getStage(){
		return window;
	}


	public static void main(String[] args){
		//load library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}

	private static void checkifCameraClosed(boolean cameraStatus){
		if(cameraStatus){
			AlertBox.display("Warning!","The Camera Is Not Closed!");
		} else{
			window.close();
		}
	}
}
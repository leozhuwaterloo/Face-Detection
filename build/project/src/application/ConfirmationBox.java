package application;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmationBox {

	private static ImageView view1 = new ImageView();
	private static final int IMG_WIDTH = 200;
	private static final int IMG_HEIGHT = 200;

	public static void display(Image img1, Rect[] facesArray1,
			Image img2, Rect[] facesArray2,
			Image img3, Rect[] facesArray3){

		
		Stage window = new Stage();
		//Block events to other windows
		window.initModality(Modality.APPLICATION_MODAL); //Must handle the window first
		window.setTitle("Confirm");

		Image fxImage1 = cutImage(img1,facesArray1);
		Image fxImage2 = cutImage(img2,facesArray2);
		Image fxImage3 = cutImage(img3,facesArray3);

		Label askForFileName = new Label("Enter a file name:");
		//grouped buttons
		final ToggleGroup group = new ToggleGroup();
		RadioButton button1 = new RadioButton("Picture 1");
		button1.setToggleGroup(group);
		button1.setSelected(true);
		RadioButton button2 = new RadioButton("Picture 2");
		button2.setToggleGroup(group);
		RadioButton button3 = new RadioButton("Picture 3");
		button3.setToggleGroup(group);

		button1.setOnAction(e -> view1.setImage(fxImage1));
		button2.setOnAction(e -> view1.setImage(fxImage2));
		button3.setOnAction(e -> view1.setImage(fxImage3));


		HBox selections = new HBox(10);
		selections.setPadding(new Insets(10, 10, 10, 10));
		selections.getChildren().addAll(button1,button2, button3);
		selections.setAlignment(Pos.CENTER);

		TextField fileName = new TextField();
		//Image view
		view1 = new ImageView();
		view1.setImage(fxImage1);//default
		view1.setFitHeight(IMG_HEIGHT);
		view1.setFitWidth(IMG_WIDTH);

		Label label = new Label ("Choose A Picture:");
		Button yesButton = new Button("Yes");
		yesButton.setOnAction(e -> savePic(fileName.getText()));


		Button closeButton = new Button("Go back");
		closeButton.setOnAction(e -> window.close());

		VBox layout = new VBox(10);
		layout.setPadding(new Insets(10, 10, 10, 10));
		layout.getChildren().addAll(label,selections,view1, askForFileName,fileName, yesButton,closeButton);
		layout.setAlignment(Pos.CENTER);	//center everything

		//Display window and wait for it to be closed before returning
		Scene scene = new Scene(layout);
		window.setScene(scene);
		window.showAndWait(); //block interactions before the AlertBox is closed
	}


	//cut the image with the rectangle coordinates
	private static Image cutImage(Image img,  Rect[] facesArray){
		BufferedImage scaledImg = null;
		int x2 = 0;
		int y2 = 0;
		try{
			scaledImg = SwingFXUtils.fromFXImage(img, null);
			x2 = scaledImg.getHeight();
			y2 = scaledImg.getWidth();
		} catch(NullPointerException e){}
		//initialize
		String startCord = "";
		String endCord = "";
		int x1 = 0;
		int y1 = 0;

		int height = x2-x1;
		int width = y2-y1;
		//change the value based on the rectangle
		try{
			startCord = facesArray[0].tl().toString();
			endCord = facesArray[0].br().toString();
			x1 = (int) Double.parseDouble(startCord.split(",")[0].substring(1, startCord.split(",")[0].length()));
			y1 = (int) Double.parseDouble(startCord.split(",")[1].substring(1, startCord.split(",")[1].length()-1));
			x2 = (int) Double.parseDouble(endCord.split(",")[0].substring(1, endCord.split(",")[0].length()));
			y2 = (int) Double.parseDouble(endCord.split(",")[1].substring(1, endCord.split(",")[1].length()-1));
			height = x2-x1;
			width = y2-y1;

		} catch (ArrayIndexOutOfBoundsException e){}

		Image fxImage = null;
		try{
			scaledImg = scaledImg.getSubimage(x1, y1, height , width );
			BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(scaledImg, 0, 0, IMG_WIDTH, IMG_HEIGHT,null);
			g.dispose();
			fxImage = SwingFXUtils.toFXImage(resizedImage, null);
		} catch (Exception e){}
		
		
		return fxImage;
	}

	
	//save to file
	private static void savePic(String fileName){
		if(view1.getImage()!=null){
			BufferedImage image = SwingFXUtils.fromFXImage(view1.getImage(), null);
			try{
				if (!fileName.isEmpty()){
					ImageIO.write(image, "png",new File("C:/Users/Sony/Desktop/ISU3U1/zzz.MoreFaceDetection/faces/" + fileName +".png"));
					AlertBox.display("Saved", "Successfully Saved.");
				}else{
					AlertBox.display("Warning", "Enter a Filename.");
				}
			} catch (IOException e){
				System.err.println("File unable to be saved.");
			}
		} else{
			AlertBox.display("Warning", "No Image Founded.");
		}
	}
}

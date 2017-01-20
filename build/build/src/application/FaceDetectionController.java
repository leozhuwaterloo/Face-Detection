package application;

import java.io.ByteArrayInputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


public class FaceDetectionController{
	//FXML button
	@FXML
	private Button cameraButton;
	@FXML
	private Button exitButton;
	@FXML
	private Button saveImageButton;
	@FXML
	private Button refreshButton;
	@FXML
	private Button pauseButton;
	
	//FXML ImageView
	@FXML
	private ImageView originalFrame;
	//2 filters
	@FXML
	private CheckBox grayFilterCheckBox;
	@FXML
	private CheckBox histoFilterCheckBox;

	//Timer
	private Timer timer;
	//Video capture
	private VideoCapture capture;
	//boolean for the status of the camera
	private boolean cameraActive;
	//2 filters
	private boolean grayFilterActive;
	private boolean histoFilterActive;
	//Face cascade classifier
	private CascadeClassifier faceCascade;
	private int absoluteFaceSize;
	private TimerTask frameGrabber;

	//initialize the variables
	public void init(){
		this.capture = new VideoCapture();
		this.faceCascade = new CascadeClassifier();
		this.faceCascade.load("resources/lbpcascades/lbpcascade_frontalface.xml");
		this.absoluteFaceSize = 0;
		this.saveImageButton.setDisable(true);
		this.histoFilterCheckBox.setDisable(true);
	}


	@FXML
	public void grayFilterSelected(){
		if (this.grayFilterCheckBox.isSelected()){
			this.grayFilterActive=true;
			this.histoFilterCheckBox.setDisable(false);
		}else{
			this.grayFilterActive=false;
			this.histoFilterCheckBox.setSelected(false);
			HistoFilterSelected();
			this.histoFilterCheckBox.setDisable(true);
		}
	}
	
	@FXML
	public void pause(){
		if (this.timer != null){
				this.timer.cancel();
			this.timer = null;
		} else if(this.timer == null){
			refreshCamera();
		}
	}
	

	@FXML
	public void HistoFilterSelected(){
		if (this.histoFilterCheckBox.isSelected())
			this.histoFilterActive=true;
		else
			this.histoFilterActive=false;
	}

	@FXML
	public void refreshCamera(){
		if (this.cameraActive){
			//start the video capture
			startCamera();
			startCamera();
		}
	}



	public boolean getCameraStatus(){
		return this.cameraActive;
	}

	@FXML
	public void saveImage(){
		ConfirmationBox.display(grabFrame(), getFacesArray(), grabFrame(), getFacesArray(),grabFrame(),  getFacesArray());
	}

	@FXML
	public void exit(){
		Stage window = Main.getStage();
		window.close();
	}


	@FXML
	public void startCamera(){
		//bind an image property with the originalFrame container
		final ObjectProperty<Image> imageProp = new SimpleObjectProperty<>();
		this.originalFrame.imageProperty().bind(imageProp);
		//originalFrame.setFitWidth(380);
		//originalFrame.setPreserveRatio(true);
		if (!this.cameraActive){
			//start the video capture
			this.capture.open(0);
			//check if available
			if (this.capture.isOpened()){
				this.cameraActive = true;
				this.exitButton.setDisable(true);
				this.saveImageButton.setDisable(false);
				//grab a frame every 33 ms (30 frames/sec)
				frameGrabber = new TimerTask() {
					@Override
					public void run(){
						//update the frame
						//shown in the UI
						final Image frame = grabFrame();
						Platform.runLater(new Runnable() {

							@Override
							public void run(){
								//show the original frames
								imageProp.set(frame);
							}
						});
					}
				};
				this.timer = new Timer();
				this.timer.schedule(frameGrabber, 0, 33);

				//update the button content
				this.cameraButton.setText("Stop Camera");
			}else{
				//error
				System.err.println("Failed to open the camera connection...");
			}
		}else{
			//the camera is not active
			this.cameraActive = false;
			this.exitButton.setDisable(false);
			this.saveImageButton.setDisable(true);
			//update button content
			this.cameraButton.setText("Start Camera");

			//stop the timer
			if (this.timer != null){
				this.timer.cancel();
				this.timer = null;
			}
			//release the camera
			this.capture.release();
		}
	}

	private Image grabFrame(){
		//initialize everything
		Image imageToShow = null;
		Mat frame = new Mat();

		//check if the capture is open
		if (this.capture.isOpened()){
			try{
				//read the current frame
				this.capture.read(frame);
				//if the frame is not empty, process it
				if (!frame.empty()){
					//face detection
					this.detectAndDisplay(frame);
					//convert the frame in gray scale
					if(grayFilterActive)
						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
					if(histoFilterActive)
						Imgproc.equalizeHist(frame, frame);


					imageToShow = mat2Image(frame);
				}
			}
			catch (Exception e){
				//error
				//System.err.println("ERROR: " + e);
			}
		}
		return imageToShow;
	}

	private Rect[] facesArray;

	private void detectAndDisplay(Mat frame){
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();

		//convert the frame in gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		//equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayFrame, grayFrame);

		//compute minimum face size (20% of the frame height, in our case)
		if (this.absoluteFaceSize == 0){
			int height = grayFrame.rows();
			if (Math.round(height * 0.2f) > 0){
				this.absoluteFaceSize = Math.round(height * 0.2f);
			}
		}

		//detect faces
		this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(
				this.absoluteFaceSize, this.absoluteFaceSize), new Size());
		//draw
		Rect[] facesArray = faces.toArray();

		this.facesArray = facesArray;
		
		
		for (int i = 0; i < facesArray.length; i++){
			Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
			//System.out.println(facesArray[i].tl()+" " + facesArray[i].br());
		}
	}

	
	public Rect[] getFacesArray(){
		return this.facesArray;
	}

	private Image mat2Image(Mat frame){
		//create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		//encode the frame in the buffer, according to the PNG format
		Imgcodecs.imencode(".png", frame, buffer);
		//build and return an Image created from the image encoded in the
		//buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}

}
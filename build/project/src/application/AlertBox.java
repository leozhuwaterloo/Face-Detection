package application;

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

public class AlertBox {

    public static void display(String title, String message) {
        Stage alertBox = new Stage();

        //Block events to other alertBoxs
        alertBox.initModality(Modality.APPLICATION_MODAL); //Must handle the alertBox first
        alertBox.setTitle(title);
        alertBox.setMinWidth(250);

        Label label = new Label();
        label.setText(message);
        Button closeButton = new Button("Go back");
        closeButton.setOnAction(e -> alertBox.close());

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.getChildren().addAll(label, closeButton);
        layout.setAlignment(Pos.CENTER);	//center everything

        //Display alertBox and wait for it to be closed before returning
        Scene scene = new Scene(layout);
        alertBox.setScene(scene);
        alertBox.showAndWait(); //block interactions before the AlertBox is closed
    }

}

package RouteMapMaker;
	
import java.util.Optional;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("UIController.fxml"));
			AnchorPane root = (AnchorPane)loader.load();
			UIController uic = loader.getController();
			uic.setObject(primaryStage);
			Scene scene = new Scene(root,800,500);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("路線図メーカー - main");
			//primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("ro.png")));
			primaryStage.setOnCloseRequest((WindowEvent t) ->{
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle("終了の確認");
				alert.setHeaderText(null);
				alert.setContentText("路線図メーカーを終了してよろしいですか？");
				Optional<ButtonType> result = alert.showAndWait();
				if(result.get() == ButtonType.OK){
					System.exit(0);
				}else{
					t.consume();
				}
			});
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

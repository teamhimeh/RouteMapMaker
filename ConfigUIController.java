package RouteMapMaker;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ConfigUIController implements Initializable{
	Configuration config;//設定はここに保存。
	Stage stage;
	UIController uic;
	
	@FXML CheckBox showGrid;
	@FXML Spinner<Integer> GridInterval;
	@FXML CheckBox BindX;
	@FXML CheckBox BindY;
	@FXML CheckBox menubarMode;
	@FXML ColorPicker fixedColor;
	@FXML ColorPicker nonFixedColor;
	@FXML RadioButton gridTriangle;
	@FXML RadioButton gridSquare;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		GridInterval.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5,1000));
		showGrid.setOnAction((ActionEvent) ->{
			config.setR_grid(showGrid.isSelected());
			uic.ReDraw();
		});
		GridInterval.valueProperty().addListener((obs, oldVal, newVal) -> {
			config.setR_gridInterval(GridInterval.getValue());
			uic.ReDraw();
		});
		BindX.setOnAction((ActionEvent) ->{
			config.setR_bindToGridX(BindX.isSelected());
			uic.ReDraw();
		});
		BindY.setOnAction((ActionEvent) ->{
			config.setR_bindToGridY(BindY.isSelected());
			uic.ReDraw();
		});
		menubarMode.setOnAction((ActionEvent) ->{
			config.setMenubarMode(menubarMode.isSelected());
			uic.setMenuBarMode(menubarMode.isSelected());
		});
		fixedColor.setOnAction((ActionEvent) ->{
			config.setFixedColor(fixedColor.getValue());
			uic.ReDraw();
		});
		nonFixedColor.setOnAction((ActionEvent) ->{
			config.setNonFixedColor(nonFixedColor.getValue());
			uic.ReDraw();
		});
		
		final ToggleGroup gridGroup = new ToggleGroup();
		gridTriangle.setToggleGroup(gridGroup);
		gridSquare.setToggleGroup(gridGroup);
		gridTriangle.setOnAction((ActionEvent) ->{
			config.setGridTriangle(true);
			uic.ReDraw();
		});
		gridSquare.setOnAction((ActionEvent) ->{
			config.setGridTriangle(false);
			uic.ReDraw();
		});
	}
	
	public void setObject(Stage stage, Configuration config, UIController uic){
		this.stage = stage;
		this.config = config;
		this.uic = uic;
		//以下初期設定
		showGrid.setSelected(config.getR_grid());
		GridInterval.getValueFactory().setValue(config.getR_gridInterval());
		BindX.setSelected(config.getR_bindToGridX());
		BindY.setSelected(config.getR_bindToGridY());
		fixedColor.setValue(config.getFixedColor());
		nonFixedColor.setValue(config.getNonFixedColor());
		menubarMode.setSelected(config.getMenubarMode());
		(config.isGridTriangle() ? gridTriangle : gridSquare).setSelected(true);
	}

}

package RouteMapMaker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class FreeItemsController implements Initializable{
	ObservableList<FreeItem> freeItems;
	UIController uic;
	
	@FXML Button addImage;
	@FXML Button addText;
	@FXML Button Delete;
	@FXML Button copy;
	@FXML ListView<FreeItem> itemList;
	@FXML Label paramL0;
	@FXML Label paramL1;
	@FXML Spinner<Integer> paramS0;
	@FXML Spinner<Integer> paramS1;
	@FXML Spinner<Integer> paramSX;
	@FXML Spinner<Integer> paramSY;
	@FXML Spinner<Integer> paramSR;
	@FXML ColorPicker p_color;
	@FXML TextField p_text;
	@FXML ChoiceBox<String> p_direction;
	@FXML ChoiceBox<String> p_fill;
	@FXML ChoiceBox<String> p_style;
	@FXML Button selectFont;
	
	public FreeItemsController(ObservableList<FreeItem> freeItems, UIController uic){
		this.freeItems = freeItems;
		this.uic = uic;
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 今回はここは自動では呼ばれません。呼び出し元で呼び出す必要あり。
		itemList.setCellFactory(new FreeItemCell());
		itemList.setItems(this.freeItems);
		addImage.setOnAction((ActionEvent) ->{
			FreeItem fi = new FreeItem(FreeItem.IMAGE);
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("画像ファイルを選択してください。");
			fileChooser.getExtensionFilters().add(new ExtensionFilter("Image Files(jpg,png,gif,bmp)", 
					"*.png", "*.jpg", "*.jpeg", "*.gif","*.bmp", "*.PNG", "*.JPG", "*.JPEG", "*.GIF","*.BMP"));
			File imageFile = fileChooser.showOpenDialog(null);
			if(imageFile != null){
				try {
					fi.setImage(new Image(new BufferedInputStream(new FileInputStream(imageFile))));
					fi.setText(imageFile.getName());
					if(fi.getImage().isError()){//イメージのロード中にエラーが検出されたことを示す。
						fi.getImage().getException().printStackTrace();
						Alert alert = new Alert(AlertType.ERROR,"画像の読み込みエラー",ButtonType.CLOSE);
						alert.getDialogPane().setContentText("画像の読み込みでエラーが発生しました。画像ファイルでない可能性があります。");
						alert.showAndWait();
					}else{//エラーなし
						//初期値設定
						fi.getParams()[0].set(100);//X座標
						fi.getParams()[1].set(100);//Y座標
						fi.getParams()[2].set(fi.getImage().getWidth());
						fi.getParams()[3].set(fi.getImage().getHeight());
						fi.getParams()[4].set(0);//回転角度
						freeItems.add(0,fi);
						itemList.getSelectionModel().select(0);
						uic.ReDraw();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Alert alert = new Alert(AlertType.ERROR,"ファイルのエラー",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("選択されたファイルを開くことができませんでした。");
					alert.showAndWait();
				}
			}
		});
		addText.setOnAction((ActionEvent) ->{
			FreeItem fi = new FreeItem(FreeItem.TEXT);
			fi.getParams()[0].set(100);
			fi.getParams()[1].set(100);
			fi.getParams()[2].set(36);
			fi.getParams()[3].set(1);
			fi.getParams()[4].set(0);
			fi.getParams()[5].set(0);
			fi.getParams()[6].set(0);
			fi.getParams()[7].set(0);
			fi.setText("Text");
			fi.setFontName("System");
			freeItems.add(0,fi);
			itemList.getSelectionModel().select(0);
			uic.ReDraw();
		});
		copy.setOnAction((ActionEvent) ->{
			int index = itemList.getSelectionModel().getSelectedIndex();
			if(index == -1){
				Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("コピーするアイテムを選択してください。");
				alert.showAndWait();
			}else{
				freeItems.add(index+1, freeItems.get(index).clone());
				uic.ReDraw();
			}
		});
		Delete.setOnAction((ActionEvent) ->{
			int index = itemList.getSelectionModel().getSelectedIndex();
			if(index == -1){
				Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("削除するアイテムを選択してください。");
				alert.showAndWait();
			}else{
				freeItems.remove(index);
				uic.ReDraw();
			}
		});
		paramS0.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0));
		paramS1.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0));
		paramSX.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0));
		paramSY.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0));
		paramSR.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-359,359,0));//角度なので。
		p_direction.setItems(FXCollections.observableArrayList("横書き","縦書き"));
		p_fill.setItems(FXCollections.observableArrayList("Fill","Stroke"));
		p_style.setItems(FXCollections.observableArrayList("REGULAR","BOLD","ITALIC","BOLD_ITALIC"));
		itemList.getSelectionModel().selectedItemProperty().addListener((ov,oldVal,newVal)->{
			int index = itemList.getSelectionModel().getSelectedIndex();
			if(index != -1){
				FreeItem item = itemList.getSelectionModel().getSelectedItem();
				if(item.getType() == FreeItem.IMAGE){
					p_color.setDisable(true);
					p_text.setDisable(true);
					p_direction.setDisable(true);
					p_fill.setDisable(true);
					p_style.setDisable(true);
					selectFont.setDisable(true);
					paramL0.setText("幅:");
					paramL1.setText("高さ:");
					paramSX.getValueFactory().setValue(item.getParams()[0].getValue().intValue());
					paramSY.getValueFactory().setValue(item.getParams()[1].getValue().intValue());
					paramS0.getValueFactory().setValue(item.getParams()[2].getValue().intValue());
					paramS1.getValueFactory().setValue(item.getParams()[3].getValue().intValue());
					paramSR.getValueFactory().setValue(item.getParams()[4].getValue().intValue());
				}
				if(item.getType() == FreeItem.TEXT){
					p_color.setDisable(false);
					p_text.setDisable(false);
					p_direction.setDisable(false);
					p_fill.setDisable(false);
					p_style.setDisable(false);
					selectFont.setDisable(false);
					paramL0.setText("サイズ:");
					paramL1.setText("線太さ:");
					paramSX.getValueFactory().setValue(item.getParams()[0].getValue().intValue());
					paramSY.getValueFactory().setValue(item.getParams()[1].getValue().intValue());
					paramS0.getValueFactory().setValue(item.getParams()[2].getValue().intValue());
					paramS1.getValueFactory().setValue(item.getParams()[3].getValue().intValue());
					paramSR.getValueFactory().setValue(item.getParams()[4].getValue().intValue());
					p_color.setValue(item.getColor());
					p_text.setText(item.getText());
					p_direction.getSelectionModel().select(item.getParams()[7].getValue().intValue());
					p_fill.getSelectionModel().select(item.getParams()[6].getValue().intValue());
					p_style.getSelectionModel().select(item.getParams()[5].getValue().intValue());
				}
			}
		});
		paramSX.valueProperty().addListener((obs, oldValue, newValue) -> {
			FreeItem item = itemList.getSelectionModel().getSelectedItem();
			if(item != null) item.getParams()[0].set(paramSX.getValue());
			uic.ReDraw();
		});
		paramSY.valueProperty().addListener((obs, oldValue, newValue) -> {
			FreeItem item = itemList.getSelectionModel().getSelectedItem();
			if(item != null) item.getParams()[1].set(paramSY.getValue());
			uic.ReDraw();
		});
		paramS0.valueProperty().addListener((obs, oldValue, newValue) -> {
			FreeItem item = itemList.getSelectionModel().getSelectedItem();
			if(item != null) item.getParams()[2].set(paramS0.getValue());
			uic.ReDraw();
		});
		paramS1.valueProperty().addListener((obs, oldValue, newValue) -> {
			FreeItem item = itemList.getSelectionModel().getSelectedItem();
			if(item != null) item.getParams()[3].set(paramS1.getValue());
			uic.ReDraw();
		});
		paramSR.valueProperty().addListener((obs, oldValue, newValue) -> {
			FreeItem item = itemList.getSelectionModel().getSelectedItem();
			if(item != null) item.getParams()[4].set(paramSR.getValue());
			uic.ReDraw();
		});
		p_color.setOnAction((ActionEvent) ->{
			FreeItem item = itemList.getSelectionModel().getSelectedItem();
			if(item != null) item.setColor(p_color.getValue());
			uic.ReDraw();
		});
		p_text.setOnAction((ActionEvent) ->{
			FreeItem item = itemList.getSelectionModel().getSelectedItem();
			if(item != null) item.setText(p_text.getText());
			itemList.setItems(null);
			itemList.setItems(freeItems);
			itemList.getSelectionModel().select(item);
			uic.ReDraw();
		});
		p_direction.valueProperty().addListener((obs, oldValue, newValue) -> {
			FreeItem item = itemList.getSelectionModel().getSelectedItem();
			if(item != null) item.getParams()[7].set(p_direction.getSelectionModel().getSelectedIndex());
			uic.ReDraw();
		});
		p_fill.valueProperty().addListener((obs, oldValue, newValue) -> {
			FreeItem item = itemList.getSelectionModel().getSelectedItem();
			if(item != null) item.getParams()[6].set(p_fill.getSelectionModel().getSelectedIndex());
			uic.ReDraw();
		});
		p_style.valueProperty().addListener((obs, oldValue, newValue) -> {
			FreeItem item = itemList.getSelectionModel().getSelectedItem();
			if(item != null) item.getParams()[5].set(p_style.getSelectionModel().getSelectedIndex());
			uic.ReDraw();
		});
		selectFont.setOnAction((ActionEvent) ->{
			FreeItem item = itemList.getSelectionModel().getSelectedItem();
			if(item != null) item.setFontName(uic.selectFontFamily(item.getFontName()));
			uic.ReDraw();
		});
	}
	public void selectItem(int index){//リストの中からアイテムを選択する。
		itemList.getSelectionModel().select(index);
	}
}

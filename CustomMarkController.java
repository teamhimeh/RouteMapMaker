package RouteMapMaker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ResourceBundle;

import RouteMapMaker.URElements.ArrayCommands;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class CustomMarkController implements Initializable{
	final int prevSize = 40;
	private Stage stage;//このstageを保持する。
	private ObservableList<StopMark> customMarks = FXCollections.observableArrayList();//カスタムマークの集合
	private GraphicsContext gc;
	private ObservableList<String> LayerListOb = FXCollections.observableArrayList();
	private ObservableList<String> paramDrawOb = FXCollections.observableArrayList();//fill(0)かStroke(1)かのパラメーターにセット
	private URElements urManager = new URElements();//undoとredoを管理する。
	
	@FXML Canvas markCanvas;
	@FXML Pane prevPane;
	@FXML Rectangle bgRect;
	@FXML ColorPicker prevbgSetter;
	@FXML ColorPicker paramColor;
	@FXML ChoiceBox<String> paramDraw;
	@FXML TextField paramText;
	@FXML Label paramL1;
	@FXML Label paramL2;
	@FXML Label paramL3;
	@FXML Label paramL4;
	@FXML Label paramL5;
	@FXML Label paramL6;
	@FXML Label paramL7;
	@FXML Label paramL8;
	@FXML Label paramLT;
	@FXML Spinner<Integer> paramS1;
	@FXML Spinner<Integer> paramS2;
	@FXML Spinner<Integer> paramS3;
	@FXML Spinner<Integer> paramS4;
	@FXML Spinner<Integer> paramS5;
	@FXML Spinner<Integer> paramS6;
	@FXML Spinner<Integer> paramS7;
	@FXML Spinner<Integer> paramS8;
	@FXML ChoiceBox<String> paramST;
	@FXML Button addOval;
	@FXML Button addRect;
	@FXML Button addArc;
	@FXML Button addText;
	@FXML Button addImage;
	@FXML Button selectFont;
	@FXML ListView<StopMark> MarkList;
	@FXML ListView<String> LayerList;
	@FXML Button MarkAdd;
	@FXML Button MarkCopy;
	@FXML Button MarkDelete;
	@FXML Button LayerDelete;
	@FXML Button Layer_UP;
	@FXML Button Layer_DOWN;
	@FXML MenuItem Undo;
	@FXML MenuItem Redo;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		gc = markCanvas.getGraphicsContext2D();
		MarkList.setItems(customMarks);
		StopMarkCell cellFactory = new StopMarkCell();
		MarkList.setCellFactory(cellFactory);
		MarkList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			draw();//まずは描画。
			if(MarkList.getSelectionModel().getSelectedIndex() != -1){
				setLayerList(customMarks.get(MarkList.getSelectionModel().getSelectedIndex()));
				if(customMarks.get(MarkList.getSelectionModel().getSelectedIndex()).getLayers().size() != 0){
					LayerList.getSelectionModel().select(0);
				}
			}
		});
		MarkAdd.setOnAction((ActionEvent) ->{//空のマークを追加する。
			System.out.println("add called.");
			StopMark newMark = new StopMark();
			urManager.push(customMarks, URElements.ArrayCommands.ADD, customMarks.size(), newMark);
			customMarks.add(newMark);
			MarkList.getSelectionModel().selectLast();
		});
		MarkCopy.setOnAction((ActionEvent) ->{//マークをコピー
			int index = MarkList.getSelectionModel().getSelectedIndex();
			if(index != -1){
				StopMark cloneMark = customMarks.get(index).clone();
				urManager.push(customMarks, URElements.ArrayCommands.ADD, customMarks.size(), cloneMark);
				customMarks.add(cloneMark);
			}
		});
		MarkDelete.setOnAction((ActionEvent) ->{
			int index = MarkList.getSelectionModel().getSelectedIndex();
			if(index != -1){
				urManager.push(customMarks, URElements.ArrayCommands.REMOVE, index, customMarks.get(index));
				customMarks.remove(index);
			}
		});
		prevbgSetter.setValue(Color.BLACK);//初期値は黒。
		prevbgSetter.setOnAction((ActionEvent) ->{
			bgRect.setFill(prevbgSetter.getValue());
		});
		paramColor.setOnAction((ActionEvent) ->{
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				urManager.push(customMarks.get(indexM).getLayers().get(indexL).getColorProperty(),
						customMarks.get(indexM).getLayers().get(indexL).getColor(), paramColor.getValue());
				customMarks.get(indexM).getLayers().get(indexL).setColor(paramColor.getValue());
			}
			draw();
		});
		paramDrawOb.add("fill");
		paramDrawOb.add("stroke");
		paramDraw.setItems(paramDrawOb);
		paramDraw.valueProperty().addListener((obs, oldValue, newValue) -> {
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				MarkLayer l = customMarks.get(indexM).getLayers().get(indexL);
				if(paramDraw.getSelectionModel().getSelectedIndex() == 0){
					if(l.getPaint() == MarkLayer.STROKE) urManager.push(l.getPaintProperty(), MarkLayer.STROKE, MarkLayer.FILL);
					l.setPaint(MarkLayer.FILL);
				}
				if(paramDraw.getSelectionModel().getSelectedIndex() == 1){
					if(l.getPaint() == MarkLayer.FILL) urManager.push(l.getPaintProperty(), MarkLayer.FILL, MarkLayer.STROKE);
					l.setPaint(MarkLayer.STROKE);
				}
				//レイヤーリスト更新
				setLayerList(MarkList.getSelectionModel().getSelectedItem());
				LayerList.getSelectionModel().select(indexL);
				draw();
			}
		});
		//スピナーについての設定は配列で一気に処理する
		Spinner[] paramSs = {paramS1,paramS2,paramS3,paramS4,paramS5,paramS6,paramS7,paramS8};
		for(int i = 0; i < 8; i++){
			paramSs[i].setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE));
		}
		setParamsReactions();//action定義が頭悪い方法でしかできないので隔離
		paramST.valueProperty().addListener((obs, oldValue, newValue) -> {
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			//コレに関しては他のレイヤーからの切替時にselectIndex-1が送られる不具合がある。とりあえずif文条件で応急処置
			int selectedIndex = paramST.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1 && selectedIndex != -1){
				MarkLayer l = customMarks.get(indexM).getLayers().get(indexL);
				if(l.getType() == MarkLayer.ARC){
					if((int)l.getParam(7) != paramST.getSelectionModel().getSelectedIndex())
						urManager.push(l.getParamProperty().get(7), l.getParam(7), (double)selectedIndex);
					l.setParam(7, (double) paramST.getSelectionModel().getSelectedIndex());
				}
				if(l.getType() == MarkLayer.TEXT){
					if((int)l.getParam(4) != paramST.getSelectionModel().getSelectedIndex())
						urManager.push(l.getParamProperty().get(4), l.getParam(4), (double)selectedIndex);
					l.setParam(4, (double) paramST.getSelectionModel().getSelectedIndex());
				}
				draw();
			}
		});
		selectFont.setOnAction((ActionEvent) ->{
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				MarkLayer l = customMarks.get(indexM).getLayers().get(indexL);
				String newFont = null;
				String current = l.getFontName();
				selectFontController euc = null;
				FXMLLoader editLoader = null;
				Stage editStage = new Stage();
				editStage.initModality(Modality.APPLICATION_MODAL);
				VBox ap = null;
				try {
					editLoader = new FXMLLoader(getClass().getResource("selectFontController.fxml"));
					ap= (VBox)editLoader.load();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				euc = (selectFontController)editLoader.getController();
				euc.setObject(editStage, current);
				Scene sc = new Scene(ap, 400, 300);
				editStage.setScene(sc);
				editStage.setTitle("フォントの選択");
				editStage.showAndWait();
				if(euc.shouldSave()){
					newFont = euc.getFontName();
				}else{
					newFont = current;
				}
				if(! current.equals(newFont))urManager.push(l.getFontNameProperty(), current, newFont);
				l.setFontName(newFont);
			}
			draw();
		});
		paramText.setOnAction((ActionEvent) ->{
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				if(customMarks.get(indexM).getLayers().get(indexL).getText() != paramText.getText())
				urManager.push(customMarks.get(indexM).getLayers().get(indexL).getTextProperty(),
						customMarks.get(indexM).getLayers().get(indexL).getText(), paramText.getText());
				customMarks.get(indexM).getLayers().get(indexL).setText(paramText.getText());
				draw();
			}
		});
		addOval.setOnAction((ActionEvent) ->{
			int index = MarkList.getSelectionModel().getSelectedIndex();
			if(index != -1){
				MarkLayer newOval = new MarkLayer(MarkLayer.OVAL);
				//初期値投入
				newOval.addParam(0.0);//左上X
				newOval.addParam(0.0);//左上Y
				newOval.addParam(1.0);//横直径
				newOval.addParam(1.0);//縦直径
				newOval.addParam(0.05);//デフォの線の太さ2/40（あくまでも相対比なのでprevSizeが変わってもここは問題ない）
				newOval.setPaint(MarkLayer.FILL);
				newOval.setColor(Color.WHITE);
				urManager.push(customMarks.get(index).getLayers(), URElements.ArrayCommands.ADD,
						customMarks.get(index).getLayers().size(), newOval);
				customMarks.get(index).getLayers().add(newOval);
				setLayerList(customMarks.get(index));
				LayerList.getSelectionModel().selectLast();
				draw();
			}
		});
		addRect.setOnAction((ActionEvent) ->{
			int index = MarkList.getSelectionModel().getSelectedIndex();
			if(index != -1){
				MarkLayer newRect = new MarkLayer(MarkLayer.RECT);
				//初期値投入
				newRect.addParam(0.0);//左上X
				newRect.addParam(0.0);//左上Y
				newRect.addParam(1.0);//幅
				newRect.addParam(1.0);//高さ
				newRect.addParam(0.0);//円弧幅
				newRect.addParam(0.0);//円弧高さ
				newRect.addParam(0.05);//lineWidth
				newRect.setPaint(MarkLayer.FILL);
				newRect.setColor(Color.WHITE);
				urManager.push(customMarks.get(index).getLayers(), URElements.ArrayCommands.ADD,
						customMarks.get(index).getLayers().size(), newRect);
				customMarks.get(index).getLayers().add(newRect);
				setLayerList(customMarks.get(index));
				LayerList.getSelectionModel().selectLast();
				draw();
			}
		});
		addArc.setOnAction((ActionEvent) ->{
			int index = MarkList.getSelectionModel().getSelectedIndex();
			if(index != -1){
				MarkLayer newArc = new MarkLayer(MarkLayer.ARC);
				//初期値投入
				newArc.addParam(0.0);//X
				newArc.addParam(0.0);//Y
				newArc.addParam(1.0);//幅
				newArc.addParam(1.0);//高さ
				newArc.addParam(0.0);//始角
				newArc.addParam(120.0);//角の大きさ
				newArc.addParam(0.05);//lineWidth
				newArc.addParam(2.0);//closure
				newArc.setPaint(MarkLayer.FILL);
				newArc.setColor(Color.WHITE);
				urManager.push(customMarks.get(index).getLayers(), URElements.ArrayCommands.ADD,
						customMarks.get(index).getLayers().size(), newArc);
				customMarks.get(index).getLayers().add(newArc);
				setLayerList(customMarks.get(index));
				LayerList.getSelectionModel().selectLast();
				draw();
			}
		});
		addText.setOnAction((ActionEvent) ->{
			int index = MarkList.getSelectionModel().getSelectedIndex();
			if(index != -1){
				MarkLayer newText = new MarkLayer(MarkLayer.TEXT);
				//初期値投入
				newText.addParam(0.0);//X
				newText.addParam(1.0);//Y
				newText.addParam(1.0);//size
				newText.addParam(0.05);//lineWidth
				newText.addParam(0.0);//type
				newText.setPaint(MarkLayer.FILL);
				newText.setColor(Color.WHITE);
				newText.setText("※");
				newText.setFontName("system");
				urManager.push(customMarks.get(index).getLayers(), URElements.ArrayCommands.ADD,
						customMarks.get(index).getLayers().size(), newText);
				customMarks.get(index).getLayers().add(newText);
				setLayerList(customMarks.get(index));
				LayerList.getSelectionModel().selectLast();
				draw();
			}
		});
		addImage.setOnAction((ActionEvent) ->{
			int index = MarkList.getSelectionModel().getSelectedIndex();
			if(index != -1){
				MarkLayer newImage = new MarkLayer(MarkLayer.IMAGE);
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("画像ファイルを選択してください。");
				fileChooser.getExtensionFilters().add(new ExtensionFilter("Image Files(jpg,png,gif,bmp)", 
						"*.png", "*.jpg", "*.jpeg", "*.gif","*.bmp", "*.PNG", "*.JPG", "*.JPEG", "*.GIF","*.BMP"));
				File imageFile = fileChooser.showOpenDialog(null);
				if(imageFile != null){
					try {
						newImage.setImage(new Image(new BufferedInputStream(new FileInputStream(imageFile))));
						newImage.setText(imageFile.getName());
						if(newImage.getImage().isError()){//イメージのロード中にエラーが検出されたことを示す。
							newImage.getImage().getException().printStackTrace();
							Alert alert = new Alert(AlertType.ERROR,"画像の読み込みエラー",ButtonType.CLOSE);
							alert.getDialogPane().setContentText("画像の読み込みでエラーが発生しました。画像ファイルでない可能性があります。");
							alert.showAndWait();
						}else if(newImage.getImage().getHeight() == 0 || newImage.getImage().getWidth() == 0){
							Alert alert = new Alert(AlertType.ERROR,"画像の読み込みエラー",ButtonType.CLOSE);
							alert.getDialogPane().setContentText("読み込まれた画像のサイズが0です。画像ファイルでない可能性があります。");
							alert.showAndWait();
						}else{//エラーなし
							//初期値設定
							double h = newImage.getImage().getHeight();
							double w = newImage.getImage().getWidth();
							if(w < h){//縦長
								newImage.addParam((1-w/h)/2);//X
								newImage.addParam(0.0);//Y
								newImage.addParam(w/h);//幅
								newImage.addParam(1.0);//高さ
							}else{//横長
								newImage.addParam(0.0);//X
								newImage.addParam((1-h/w)/2);//Y
								newImage.addParam(1.0);//幅
								newImage.addParam(h/w);//高さ
							}
							urManager.push(customMarks.get(index).getLayers(), URElements.ArrayCommands.ADD,
									customMarks.get(index).getLayers().size(), newImage);
							customMarks.get(index).getLayers().add(newImage);
							setLayerList(customMarks.get(index));
							LayerList.getSelectionModel().selectLast();
							draw();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Alert alert = new Alert(AlertType.ERROR,"ファイルのエラー",ButtonType.CLOSE);
						alert.getDialogPane().setContentText("選択されたファイルを開くことができませんでした。");
						alert.showAndWait();
					}
				}
			}
		});
		LayerList.setItems(LayerListOb);
		LayerList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			System.out.println("LayerList selected "+indexL);
			if(indexM != -1 && indexL != -1){
				setParameters(customMarks.get(indexM).getLayers().get(indexL));
			}
		});
		LayerList.setOnMouseClicked((ActionEvent) ->{
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			System.out.println("LayerList selected on mouse"+indexL);
			if(indexM != -1 && indexL != -1){
				setParameters(customMarks.get(indexM).getLayers().get(indexL));
			}
		});
		LayerDelete.setOnAction((ActionEvent) ->{
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				urManager.push(customMarks.get(indexM).getLayers(), URElements.ArrayCommands.REMOVE,
						indexL, customMarks.get(indexM).getLayers().get(indexL));
				customMarks.get(indexM).getLayers().remove(indexL);
				setLayerList(customMarks.get(indexM));
				if(customMarks.get(indexM).getLayers().size() == 0){
					paramColor.setDisable(true);
					paramDraw.setDisable(true);
					setNumericParams(null, new String[0]);
				}else if(customMarks.get(indexM).getLayers().size() == indexL){
					LayerList.getSelectionModel().selectLast();
				}else{
					LayerList.getSelectionModel().select(indexL);
				}
			}
		});
		Layer_UP.setOnAction((ActionEvent) ->{
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL > 0){//indexLが0だとコレは意味を持たない
				urManager.push(customMarks.get(indexM).getLayers(), URElements.ArrayCommands.UP, indexL, null);
				MarkLayer l1 = customMarks.get(indexM).getLayers().get(indexL);
				MarkLayer l2 = customMarks.get(indexM).getLayers().get(indexL - 1);
				customMarks.get(indexM).getLayers().set(indexL - 1, l1);
				customMarks.get(indexM).getLayers().set(indexL, l2);
				setLayerList(customMarks.get(indexM));
				LayerList.getSelectionModel().select(indexL - 1);
				draw();
			}
		});
		Layer_DOWN.setOnAction((ActionEvent) ->{
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1 && indexL != customMarks.get(indexM).getLayers().size() - 1){
				//indexLが最後だとコレは意味を持たない
				urManager.push(customMarks.get(indexM).getLayers(), URElements.ArrayCommands.DOWN, indexL, null);
				MarkLayer l1 = customMarks.get(indexM).getLayers().get(indexL);
				MarkLayer l2 = customMarks.get(indexM).getLayers().get(indexL + 1);
				customMarks.get(indexM).getLayers().set(indexL + 1, l1);
				customMarks.get(indexM).getLayers().set(indexL, l2);
				setLayerList(customMarks.get(indexM));
				LayerList.getSelectionModel().select(indexL + 1);
				draw();
			}
		});
		Undo.setOnAction((ActionEvent)->{
			int layerListSelectedIndex = LayerList.getSelectionModel().getSelectedIndex();
			urManager.undo();
			if(MarkList.getSelectionModel().getSelectedItem() == null){
				MarkList.getSelectionModel().selectFirst();
			}else{
				setLayerList(MarkList.getSelectionModel().getSelectedItem());
				if(layerListSelectedIndex != -1 && layerListSelectedIndex < 
						MarkList.getSelectionModel().getSelectedItem().getLayers().size()){
					LayerList.getSelectionModel().select(layerListSelectedIndex);
				}else{
					LayerList.getSelectionModel().selectFirst();
				}
			}
			draw();
		});
		Redo.setOnAction((ActionEvent)->{
			int layerListSelectedIndex = LayerList.getSelectionModel().getSelectedIndex();
			urManager.redo();
			if(MarkList.getSelectionModel().getSelectedItem() == null){
				MarkList.getSelectionModel().selectFirst();
			}else{
				setLayerList(MarkList.getSelectionModel().getSelectedItem());
				if(layerListSelectedIndex != -1 && layerListSelectedIndex < 
						MarkList.getSelectionModel().getSelectedItem().getLayers().size()){
					LayerList.getSelectionModel().select(layerListSelectedIndex);
				}else{
					LayerList.getSelectionModel().selectFirst();
				}
			}
			draw();
		});
		urManager.getUndoableProperty().addListener((observable, oldValue, newValue) -> {
			Undo.setDisable(! urManager.getUndoableProperty().get());
		});
		urManager.getRedoableProperty().addListener((observable, oldValue, newValue) -> {
			Redo.setDisable(! urManager.getRedoableProperty().get());
		});
	}
	
	public void setObject(Stage stage,ObservableList<StopMark> mm){
		this.customMarks = mm;
		MarkList.setItems(customMarks);//これをしないとObservableListからListViewに変更通知が行きません。
		this.stage = stage;
	}
	
	public static void markDraw(GraphicsContext gc, StopMark mark, double size, double[] coordinate){//主に実際の路線図上での描画用
		double coor[] = new double[2];
		coor[0] = coordinate[0] - size / 2;
		coor[1] = coordinate[1] - size / 2;
		actMarkDraw(gc, mark, size, coor);
	}
	public static void markDraw(GraphicsContext gc, StopMark mark, double prevSize){//主にプレビュー画面での描画用
		gc.clearRect(0, 0, prevSize, prevSize);//はじめに全領域消去
		double[] coordinate = {0.0,0.0};
		actMarkDraw(gc, mark, prevSize, coordinate);
	}
	private static void actMarkDraw(GraphicsContext gc, StopMark mark, double size, double[] coor){//実際の描画処理。
		//背景処理、領域消去処理はやりません。
		for(int i = mark.getLayers().size() - 1; 0 <= i; i--){
			MarkLayer layer = mark.getLayers().get(i);//現在のレイヤー
			Double[] prevParams;//パラメーター収納に使う
			switch(layer.getType()){
			case MarkLayer.OVAL:
				prevParams = new Double[5];
				for(int k = 0; k < 5; k++){
					prevParams[k] = layer.getParam(k) * size;
				}
				if(layer.getPaint() == MarkLayer.FILL){
					gc.setFill(layer.getColor());
					gc.fillOval(prevParams[0]+coor[0], prevParams[1]+coor[1], prevParams[2], prevParams[3]);
				}
				if(layer.getPaint() == MarkLayer.STROKE){
					gc.setStroke(layer.getColor());
					gc.setLineWidth(prevParams[4]);
					gc.strokeOval(prevParams[0]+coor[0], prevParams[1]+coor[1], prevParams[2], prevParams[3]);
				}
				break;
			case MarkLayer.RECT:
				prevParams = new Double[7];
				for(int k = 0; k < 7; k++){//RECTは全てsize倍する
					prevParams[k] = layer.getParam(k) * size;
				}
				if(layer.getPaint() == MarkLayer.FILL){
					gc.setFill(layer.getColor());
					gc.fillRoundRect(prevParams[0]+coor[0], prevParams[1]+coor[1], prevParams[2], prevParams[3], prevParams[4],
							prevParams[5]);
				}
				if(layer.getPaint() == MarkLayer.STROKE){
					gc.setStroke(layer.getColor());
					gc.setLineWidth(prevParams[6]);
					gc.strokeRoundRect(prevParams[0]+coor[0], prevParams[1]+coor[1], prevParams[2], prevParams[3],
							prevParams[4],prevParams[5]);
				}
				break;
			case MarkLayer.ARC:
				prevParams = new Double[8];//ARCはsize倍するやつとしないやつがある。
				prevParams[0] = layer.getParam(0) * size;
				prevParams[1] = layer.getParam(1) * size;
				prevParams[2] = layer.getParam(2) * size;
				prevParams[3] = layer.getParam(3) * size;
				prevParams[4] = layer.getParam(4);
				prevParams[5] = layer.getParam(5);
				prevParams[6] = layer.getParam(6) * size;
				prevParams[7] = layer.getParam(7);
				ArcType a = ArcType.CHORD;
				if(prevParams[7].intValue() == 0) a = ArcType.CHORD;
				if(prevParams[7].intValue() == 1) a = ArcType.OPEN;
				if(prevParams[7].intValue() == 2) a = ArcType.ROUND;
				if(layer.getPaint() == MarkLayer.FILL){
					gc.setFill(layer.getColor());
					gc.fillArc(prevParams[0]+coor[0], prevParams[1]+coor[1], prevParams[2], prevParams[3],prevParams[4],
							prevParams[5], a);
				}
				if(layer.getPaint() == MarkLayer.STROKE){
					gc.setStroke(layer.getColor());
					gc.setLineWidth(prevParams[6]);
					gc.strokeArc(prevParams[0]+coor[0], prevParams[1]+coor[1], prevParams[2], prevParams[3],prevParams[4],
							prevParams[5], a);
				}
				break;
			case MarkLayer.TEXT:
				prevParams = new Double[5];//TEXTはsize倍するやつとしないやつがある。
				prevParams[0] = layer.getParam(0) * size;
				prevParams[1] = layer.getParam(1) * size;
				prevParams[2] = layer.getParam(2) * size;
				prevParams[3] = layer.getParam(3) * size;
				prevParams[4] = layer.getParam(4);
				Font font = Font.getDefault();
				if(prevParams[4].intValue() == 0) font = Font.font(layer.getFontName(), FontWeight.NORMAL, FontPosture.REGULAR,
						prevParams[2]);//NORMAL
				if(prevParams[4].intValue() == 1) font = Font.font(layer.getFontName(), FontWeight.BOLD, FontPosture.REGULAR,
						prevParams[2]);//BOLD
				if(prevParams[4].intValue() == 2) font = Font.font(layer.getFontName(), FontWeight.NORMAL, FontPosture.ITALIC,
						prevParams[2]);//ITALIC
				if(prevParams[4].intValue() == 3) font = Font.font(layer.getFontName(), FontWeight.BOLD, FontPosture.ITALIC,
						prevParams[2]);//BOLD_ITALIC
				if(layer.getPaint() == MarkLayer.FILL){
					gc.setFill(layer.getColor());
					gc.setFont(font);
					gc.fillText(layer.getText(), prevParams[0]+coor[0], prevParams[1]+coor[1]);
				}
				if(layer.getPaint() == MarkLayer.STROKE){
					gc.setStroke(layer.getColor());
					gc.setLineWidth(prevParams[3]);
					gc.setFont(font);
					gc.strokeText(layer.getText(), prevParams[0]+coor[0], prevParams[1]+coor[1]);
				}
				break;
			case MarkLayer.IMAGE:
				prevParams = new Double[4];
				for(int k = 0; k < 4; k++){//IMAGEは全てsize倍する
					prevParams[k] = layer.getParam(k) * size;
				}
				gc.drawImage(layer.getImage(), prevParams[0]+coor[0], prevParams[1]+coor[1], prevParams[2], prevParams[3]);
				break;
			}
		}
	}
	void draw(){//プレビューを描画するメソッド。背景処理はやりません。
		int markIndex = MarkList.getSelectionModel().getSelectedIndex();
		if(markIndex != -1){
			markDraw(gc, customMarks.get(markIndex), this.prevSize);
			//リストも更新・・・この処理は不具合を引き起こすのであとで対策
			/*
			customMarks.add(new StopMark());
			MarkList.getSelectionModel().selectFirst();
			customMarks.remove(customMarks.size() - 1);
			MarkList.getSelectionModel().select(markIndex);
			*/
		}
	}
	void setLayerList(StopMark s){//LayerListを更新する
		LayerListOb.clear();
		for(MarkLayer l: s.getLayers()){//各MarkLayerに対して
			String fs = null;
			if(l.getPaint() == MarkLayer.FILL) fs = "Fill";
			if(l.getPaint() == MarkLayer.STROKE) fs = "Stroke";
			if(l.getType() == MarkLayer.OVAL) LayerListOb.add("円・楕円["+fs+"]");
			if(l.getType() == MarkLayer.ARC) LayerListOb.add("円弧["+fs+"]");
			if(l.getType() == MarkLayer.RECT) LayerListOb.add("長方形["+fs+"]");
			if(l.getType() == MarkLayer.POLYGON) LayerListOb.add("多角形["+fs+"]");
			if(l.getType() == MarkLayer.POLYLINE) LayerListOb.add("ポリライン["+fs+"]");
			if(l.getType() == MarkLayer.TEXT) LayerListOb.add("文字列["+fs+"]");
			if(l.getType() == MarkLayer.IMAGE) LayerListOb.add("画像["+l.getText()+"]");
		}
	}
	void setParameters(MarkLayer l){//各種パラメーターを設定していく。
		if(l.getType() != MarkLayer.TEXT){
			paramText.setDisable(true);
			selectFont.setDisable(true);
		}
		//↑パラメータごとに設定した方が早いゾーン。↓図形ごとに設定するゾーン
		if(l.getType() == MarkLayer.OVAL){
			paramColor.setDisable(false);
			paramColor.setValue(l.getColor());
			paramDraw.setDisable(false);
			paramLT.setDisable(true);
			paramST.setDisable(true);
			if(l.getPaint() == MarkLayer.FILL) paramDraw.getSelectionModel().select(0);
			if(l.getPaint() == MarkLayer.STROKE) paramDraw.getSelectionModel().select(1);
			String[] texts = {"左上X","左上Y","直径X","直径Y","線の太さ"};
			setNumericParams(l,texts);
		}else if(l.getType() == MarkLayer.RECT){
			paramColor.setDisable(false);
			paramColor.setValue(l.getColor());
			paramDraw.setDisable(false);
			paramLT.setDisable(true);
			paramST.setDisable(true);
			if(l.getPaint() == MarkLayer.FILL) paramDraw.getSelectionModel().select(0);
			if(l.getPaint() == MarkLayer.STROKE) paramDraw.getSelectionModel().select(1);
			String[] texts = {"左上X","左上Y","幅","高さ","角円幅","角円高さ","線の太さ"};
			setNumericParams(l,texts);
		}else if(l.getType() == MarkLayer.ARC){
			paramColor.setDisable(false);
			paramColor.setValue(l.getColor());
			paramDraw.setDisable(false);
			if(l.getPaint() == MarkLayer.FILL) paramDraw.getSelectionModel().select(0);
			if(l.getPaint() == MarkLayer.STROKE) paramDraw.getSelectionModel().select(1);
			String[] texts = {"X","Y","幅","高さ","始角(°)","角大きさ","線の太さ"};
			setNumericParams(l,texts);
			paramLT.setDisable(false);
			paramLT.setText("閉じタイプ");
			paramST.setDisable(false);
			ObservableList<String> paramSTOb = FXCollections.observableArrayList("CHORD","OPEN","ROUND");
			paramST.setItems(paramSTOb);
			paramST.getSelectionModel().select((int)l.getParam(7));
		}else if(l.getType() == MarkLayer.TEXT){
			paramColor.setDisable(false);
			paramColor.setValue(l.getColor());
			paramDraw.setDisable(false);
			paramLT.setDisable(false);
			paramLT.setText("スタイル");
			paramST.setDisable(false);
			ObservableList<String> paramSTOb = FXCollections.observableArrayList("REGULAR","BOLD","ITALIC","BOLD_ITALIC");
			paramST.setItems(paramSTOb);
			paramST.getSelectionModel().select((int)l.getParam(4));
			selectFont.setDisable(false);
			paramText.setDisable(false);
			paramText.setText(l.getText());
			if(l.getPaint() == MarkLayer.FILL) paramDraw.getSelectionModel().select(0);
			if(l.getPaint() == MarkLayer.STROKE) paramDraw.getSelectionModel().select(1);
			String[] texts = {"X","Y","文字サイズ","線の太さ"};
			setNumericParams(l,texts);
		}else if(l.getType() == MarkLayer.IMAGE){
			paramColor.setDisable(true);
			paramDraw.setDisable(true);
			paramLT.setDisable(true);
			paramST.setDisable(true);
			String[] texts = {"左上X","左上Y","画像幅","画像高さ"};
			setNumericParams(l,texts);
		}
	}
	void setNumericParams(MarkLayer l, String[] texts){//下のパラメーターたちのdisableパラメタを一斉に切り替え、各種代入する
		Label[] paramLs = {paramL1,paramL2,paramL3,paramL4,paramL5,paramL6,paramL7,paramL8};
		Spinner[] paramSs = {paramS1,paramS2,paramS3,paramS4,paramS5,paramS6,paramS7,paramS8};
		int i = texts.length;//定義文字配列の長さが有効パラメーターの個数と一致する。
		for(int h = 0; h < i; h++){//iまでは全てfalse
			paramLs[h].setDisable(false);
			paramLs[h].setText(texts[h]);
			paramSs[h].setDisable(false);
			if(l.getParamsProportion()[h] == true)paramSs[h].getValueFactory().setValue((int)(l.getParam(h) * prevSize));
			if(l.getParamsProportion()[h] == false)paramSs[h].getValueFactory().setValue((int)l.getParam(h));
		}
		for(int h = i; h < 8; h++){
			paramLs[h].setDisable(true);
			paramSs[h].setDisable(true);
		}
	}
	void setParamsReactions(){//Spinnerのジェネリクス縛り等の関係で全部手書きという頭悪いことしかうまくいかないのでここに隔離します。
		paramS1.valueProperty().addListener((obs, oldValue, newValue) -> {
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				if(customMarks.get(indexM).getLayers().get(indexL).getParamsProportion()[0] == true){
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(0) * prevSize){
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).
							getParamProperty().get(0),oldValue.doubleValue() / prevSize, newValue.doubleValue() / prevSize);
					}
					customMarks.get(indexM).getLayers().get(indexL).setParam(0, (double) (paramS1.getValue()) / prevSize);
				}else{
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(0)) 
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).
							getParamProperty().get(0),oldValue.doubleValue(), newValue.doubleValue());
					customMarks.get(indexM).getLayers().get(indexL).setParam(0, (double) (paramS1.getValue()));
				}
				draw();
			}
		});
		paramS2.valueProperty().addListener((obs, oldValue, newValue) -> {
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				if(customMarks.get(indexM).getLayers().get(indexL).getParamsProportion()[1] == true){
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(1) * prevSize)
					urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(1),
							oldValue.doubleValue() / prevSize, newValue.doubleValue() / prevSize);
					customMarks.get(indexM).getLayers().get(indexL).setParam(1, (double) (paramS2.getValue()) / prevSize);
				}else{
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(1))
					urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(1),
							oldValue.doubleValue(), newValue.doubleValue());
					customMarks.get(indexM).getLayers().get(indexL).setParam(1, (double) (paramS2.getValue()));
				}
				draw();
			}
		});
		paramS3.valueProperty().addListener((obs, oldValue, newValue) -> {
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				if(customMarks.get(indexM).getLayers().get(indexL).getParamsProportion()[2] == true){
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(2) * prevSize)
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(2),
								oldValue.doubleValue() / prevSize, newValue.doubleValue() / prevSize);
					customMarks.get(indexM).getLayers().get(indexL).setParam(2, (double) (paramS3.getValue()) / prevSize);
				}else{
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(2))
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(2),
								oldValue.doubleValue(), newValue.doubleValue());
					customMarks.get(indexM).getLayers().get(indexL).setParam(2, (double) (paramS3.getValue()));
				}
				draw();
			}
		});
		paramS4.valueProperty().addListener((obs, oldValue, newValue) -> {
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				if(customMarks.get(indexM).getLayers().get(indexL).getParamsProportion()[3] == true){
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(3) * prevSize)
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(3),
								oldValue.doubleValue() / prevSize, newValue.doubleValue() / prevSize);
					customMarks.get(indexM).getLayers().get(indexL).setParam(3, (double) (paramS4.getValue()) / prevSize);
				}else{
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(3))
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(3),
								oldValue.doubleValue(), newValue.doubleValue());
					customMarks.get(indexM).getLayers().get(indexL).setParam(3, (double) (paramS4.getValue()));
				}
				draw();
			}
		});
		paramS5.valueProperty().addListener((obs, oldValue, newValue) -> {
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				if(customMarks.get(indexM).getLayers().get(indexL).getParamsProportion()[4] == true){
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(4) * prevSize)
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(4),
								oldValue.doubleValue() / prevSize, newValue.doubleValue() / prevSize);
					customMarks.get(indexM).getLayers().get(indexL).setParam(4, (double) (paramS5.getValue()) / prevSize);
				}else{
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(4))
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(4),
								oldValue.doubleValue(), newValue.doubleValue());
					customMarks.get(indexM).getLayers().get(indexL).setParam(4, (double) (paramS5.getValue()));
				}
				draw();
			}
		});
		paramS6.valueProperty().addListener((obs, oldValue, newValue) -> {
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				if(customMarks.get(indexM).getLayers().get(indexL).getParamsProportion()[5] == true){
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(5) * prevSize)
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(5),
								oldValue.doubleValue() / prevSize, newValue.doubleValue() / prevSize);
					customMarks.get(indexM).getLayers().get(indexL).setParam(5, (double) (paramS6.getValue()) / prevSize);
				}else{
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(5))
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(5),
								oldValue.doubleValue(), newValue.doubleValue());
					customMarks.get(indexM).getLayers().get(indexL).setParam(5, (double) (paramS6.getValue()));
				}
				draw();
			}
		});
		paramS7.valueProperty().addListener((obs, oldValue, newValue) -> {
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				if(customMarks.get(indexM).getLayers().get(indexL).getParamsProportion()[6] == true){
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(6) * prevSize)
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(6),
								oldValue.doubleValue() / prevSize, newValue.doubleValue() / prevSize);
					customMarks.get(indexM).getLayers().get(indexL).setParam(6, (double) (paramS7.getValue()) / prevSize);
				}else{
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(6))
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(6),
								oldValue.doubleValue(), newValue.doubleValue());
					customMarks.get(indexM).getLayers().get(indexL).setParam(6, (double) (paramS7.getValue()));
				}
				draw();
			}
		});
		paramS8.valueProperty().addListener((obs, oldValue, newValue) -> {
			int indexM = MarkList.getSelectionModel().getSelectedIndex();
			int indexL = LayerList.getSelectionModel().getSelectedIndex();
			if(indexM != -1 && indexL != -1){
				if(customMarks.get(indexM).getLayers().get(indexL).getParamsProportion()[7] == true){
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(7) * prevSize)
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(7),
								oldValue.doubleValue() / prevSize, newValue.doubleValue() / prevSize);
					customMarks.get(indexM).getLayers().get(indexL).setParam(7, (double) (paramS8.getValue()) / prevSize);
				}else{
					if(oldValue == customMarks.get(indexM).getLayers().get(indexL).getParam(7))
						urManager.push(customMarks.get(indexM).getLayers().get(indexL).getParamProperty().get(7),
								oldValue.doubleValue(), newValue.doubleValue());
					customMarks.get(indexM).getLayers().get(indexL).setParam(7, (double) (paramS8.getValue()));
				}
				draw();
			}
		});
	}
}

package RouteMapMaker;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class TransformController implements Initializable {

	private double[] canvasSize;//キャンバスのサイズを取得、設定
	private UIController uic;//Redraw用
	private Stage stage;
	private ObservableList<Line> lineList;
	private ObservableList<FreeItem> freeItems;
	private MainURManager urManager;
	private double[] originalSize = new double[2];
	private final int WITH_FI = -10;
	private final int NO_FI = -11;
	private final int CANCEL = -12;
	
	@FXML Spinner<Integer> trans_X;
	@FXML Spinner<Integer> trans_Y;
	@FXML Button trans_AP;
	@FXML Spinner<Integer> scale_Width;
	@FXML Spinner<Integer> scale_Height;
	@FXML CheckBox scale_fix;
	@FXML Spinner<Integer> scale_X;
	@FXML Spinner<Integer> scale_Y;
	@FXML Label scale_after;
	@FXML Button scale_AP;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		trans_X.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		trans_Y.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		trans_AP.setOnAction((ActionEvent) ->{
			int choice = confirm("平行移動");
			if(choice != CANCEL){
				//変更したパラメーターを格納してまとめてpush
				ObservableList<DoubleProperty> props = FXCollections.observableArrayList();
				ObservableList<Double> oldVals = FXCollections.observableArrayList();
				ObservableList<Double> newVals = FXCollections.observableArrayList();
				for(Line line: lineList){
					for(Station sta: line.getStations()){
						sta.setDrawn(false);
					}
				}
				try{
					for(Line line: lineList){
						for(Station sta: line.getStations()){
							double old;
							if(! sta.isDrawn()){
								//X
								props.add(sta.getPointProperty()[0]);
								old = sta.getPointProperty()[0].get();
								oldVals.add(sta.getPointProperty()[0].get());
								sta.getPointProperty()[0].set(old + Integer.parseInt(trans_X.getEditor().getText()));
								newVals.add(sta.getPointProperty()[0].get());
								//Y
								props.add(sta.getPointProperty()[1]);
								old = sta.getPointProperty()[1].get();
								oldVals.add(sta.getPointProperty()[1].get());
								sta.getPointProperty()[1].set(old + Integer.parseInt(trans_Y.getEditor().getText()));
								newVals.add(sta.getPointProperty()[1].get());
								sta.setDrawn(true);
							}
						}
					}
					canvasSize[0] = canvasSize[0] + Integer.parseInt(trans_X.getEditor().getText());
					canvasSize[1] = canvasSize[1] + Integer.parseInt(trans_Y.getEditor().getText());
					if(choice == WITH_FI){
						for(FreeItem item: freeItems){
							double old;
							//X
							props.add(item.getParams()[0]);
							old = item.getParams()[0].get();
							oldVals.add(item.getParams()[0].get());
							item.getParams()[0].set(old + Integer.parseInt(trans_X.getEditor().getText()));
							newVals.add(item.getParams()[0].get());
							//Y
							props.add(item.getParams()[1]);
							old = item.getParams()[1].get();
							oldVals.add(item.getParams()[1].get());
							item.getParams()[1].set(old + Integer.parseInt(trans_Y.getEditor().getText()));
							newVals.add(item.getParams()[1].get());
						}
					}
					urManager.push(props, oldVals, newVals, originalSize, canvasSize, uic);
					System.out.println("pushed.");
					uic.ReDraw();
					stage.close();
				}catch(NumberFormatException e){
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("パラメーターを確認してください。\nパラメーターには半角数字を入力してください。");
					alert.showAndWait();
				}
			}
		});
		scale_Width.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE));
		scale_Height.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE));
		scale_fix.setSelected(true);
		scale_X.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		scale_Y.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		scale_Width.valueProperty().addListener((obs, oldVal, newVal) -> {
			if(scale_fix.isSelected()) scale_Height.getValueFactory().setValue(scale_Width.getValue());
			calcSize();
		});
		scale_Height.valueProperty().addListener((obs, oldVal, newVal) -> {
			if(scale_fix.isSelected()) scale_Width.getValueFactory().setValue(scale_Height.getValue());
			calcSize();
		});
		scale_X.valueProperty().addListener((obs, oldVal, newVal) -> {
			calcSize();
		});
		scale_Y.valueProperty().addListener((obs, oldVal, newVal) -> {
			calcSize();
		});
		scale_AP.setOnAction((ActionEvent) ->{
			int choice = confirm("平行移動");
			if(choice != CANCEL){
				//変更したパラメーターを格納してまとめてpush
				ObservableList<DoubleProperty> props = FXCollections.observableArrayList();
				ObservableList<Double> oldVals = FXCollections.observableArrayList();
				ObservableList<Double> newVals = FXCollections.observableArrayList();
				for(Line line: lineList){
					for(Station sta: line.getStations()){
						sta.setDrawn(false);
					}
				}
				try{
					double scaleW = Double.parseDouble(scale_Width.getEditor().getText()) / 100;
					double scaleH = Double.parseDouble(scale_Height.getEditor().getText()) / 100;
					int scaleX = Integer.parseInt(scale_X.getEditor().getText());
					int scaleY = Integer.parseInt(scale_Y.getEditor().getText());
					for(Line l: lineList){
						for(Station sta: l.getStations()){
							if(! sta.isDrawn()){
								double old;
								//X
								old = sta.getPointProperty()[0].get();
								props.add(sta.getPointProperty()[0]);
								oldVals.add(old);
								sta.getPointProperty()[0].set((old - scaleX) * scaleW + scaleX);
								newVals.add(sta.getPointProperty()[0].get());
								//Y
								old = sta.getPointProperty()[1].get();
								props.add(sta.getPointProperty()[1]);
								oldVals.add(old);
								sta.getPointProperty()[1].set((old - scaleY) * scaleH + scaleY);
								newVals.add(sta.getPointProperty()[1].get());
								sta.setDrawn(true);
							}
						}
					}
					canvasSize[0] = (canvasSize[0] - scaleX) * scaleW + scaleX;
					canvasSize[1] = (canvasSize[1] - scaleY) * scaleH + scaleY;
					if(choice == WITH_FI){
						for(FreeItem item: freeItems){
							double old;
							//X
							props.add(item.getParams()[0]);
							old = item.getParams()[0].get();
							oldVals.add(item.getParams()[0].get());
							item.getParams()[0].set((old - scaleX) * scaleW + scaleX);
							newVals.add(item.getParams()[0].get());
							//Y
							props.add(item.getParams()[1]);
							old = item.getParams()[1].get();
							oldVals.add(item.getParams()[1].get());
							item.getParams()[1].set((old - scaleY) * scaleH + scaleY);
							newVals.add(item.getParams()[1].get());
						}
					}
					urManager.push(props, oldVals, newVals, originalSize, canvasSize, uic);
					uic.ReDraw();
					stage.close();
				}catch(NumberFormatException e){
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("パラメーターを確認してください。\n パラメーターには半角数字を入力してください。");
					alert.showAndWait();
				}
			}
		});
	}
	public void setObject(double[] canvasOriginal, Stage stage, UIController uic, ObservableList<Line> lineList, 
			ObservableList<FreeItem> freeItems, MainURManager urManager){
		this.canvasSize = canvasOriginal;
		this.uic = uic;
		this.stage = stage;
		this.lineList = lineList;
		this.freeItems = freeItems;
		this.urManager = urManager;
		originalSize[0] = canvasSize[0];
		originalSize[1] = canvasSize[1];
		scale_Width.getValueFactory().setValue(100);
		scale_Height.getValueFactory().setValue(100);
		scale_after.setText((int)originalSize[0] + " × " + (int)originalSize[1]);
	}
	private int confirm(String text){//FreeItemも一緒に移すかやらないかキャンセルかを問うダイアログを作る
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setContentText(text + " を行います。\n"
				+ "自由挿入アイテムの位置座標も一緒に変更しますか？");
		ButtonType buttonWithFI = new ButtonType("一緒に変更");
		ButtonType buttonNoFI = new ButtonType("駅座標だけ");
		ButtonType buttonCancel = new ButtonType("キャンセル",ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonWithFI,buttonNoFI,buttonCancel);
		Optional<ButtonType> result = alert.showAndWait();
		if(result.get() == buttonWithFI){
			return WITH_FI;
		}else if(result.get() == buttonNoFI){
			return NO_FI;
		}else{
			return CANCEL;
		}
	}
	private void calcSize(){//スケール変換後のサイズを計算し、表示する。
		double[] after = new double[2];
		try{
			double X = scale_X.getValue().doubleValue();
			double Y = scale_Y.getValue().doubleValue();
			double W = scale_Width.getValue().doubleValue() / 100;
			double H = scale_Height.getValue().doubleValue() / 100;
			after[0] = (originalSize[0] - X) * W + X;
			after[1] = (originalSize[1] - Y) * H + Y;
			scale_after.setText((int)after[0] + " × " + (int) after[1]);
		}catch(NumberFormatException e){
			scale_after.setText("");
		}
	}

}

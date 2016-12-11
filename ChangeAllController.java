package RouteMapMaker;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class ChangeAllController implements Initializable{

	ObservableList<Line> lineList;
	ObservableList<StopMark> B_markList = FXCollections.observableArrayList();
	ObservableList<DoubleArrayWrapper> lineDashes;
	UIController uic;
	
	@FXML ListView<Line> A_list;
	@FXML ComboBox<Integer> A_Location;
	@FXML Button A_Location_AP;
	@FXML ColorPicker A_Color;
	@FXML Button A_Color_AP;
	@FXML Spinner<Integer> A_Size;
	@FXML Button A_Size_AP;
	@FXML ComboBox<Integer> A_Style;
	@FXML Button A_Style_AP;
	@FXML Spinner<Integer> A_X;
	@FXML Spinner<Integer> A_Y;
	@FXML Button A_Zure_AP;
	@FXML ListView<Line> B_LineList;
	@FXML ListView<Train> B_TrainList;
	@FXML ColorPicker B_lineColor;
	@FXML Button B_lineColor_AP;
	@FXML Spinner<Integer> B_lineZure;
	@FXML Button B_lineZure_AP;
	@FXML Spinner<Integer> B_lineWidth;
	@FXML Button B_lineWidth_AP;
	@FXML ComboBox<DoubleArrayWrapper> B_linePattern;
	@FXML Button B_linePattern_AP;
	@FXML Spinner<Integer> B_lineA;
	@FXML Button B_lineA_AP;
	@FXML Spinner<Integer> B_lineB;
	@FXML Button B_lineB_AP;
	@FXML ColorPicker B_markColor;
	@FXML Button B_markColor_AP;
	@FXML Spinner<Integer> B_markSize;
	@FXML Button B_markSize_AP;
	@FXML ComboBox<StopMark> B_markType;
	@FXML Button B_markType_AP;
	@FXML ListView<Line> C_LineList;
	@FXML ListView<Train> C_TrainList;
	@FXML ListView<TrainStop> C_StopList;
	@FXML Spinner<Integer> C_X;
	@FXML Spinner<Integer> C_Y;
	@FXML Button C_loc_AP;
	@FXML ComboBox<StopMark> C_Mark;
	@FXML Button C_Mark_AP;
	@FXML ListView<Line> D_LineList;
	@FXML ListView<Station> D_StaList;
	@FXML ToggleButton D_TBstaOn;
	@FXML Button D_TBstaOn_AP;
	@FXML Spinner<Integer> D_staX;
	@FXML Spinner<Integer> D_staY;
	@FXML Button D_sta_AP;
	@FXML ComboBox<Integer> D_staMuki;
	@FXML Button D_staMuki_AP;
	@FXML Spinner<Integer> D_staSize;
	@FXML Button D_staSize_AP;
	@FXML ComboBox<Integer> D_staStyle;
	@FXML Button D_staStyle_AP;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		//タブA（路線プロパティ）
		A_list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		A_list.setCellFactory(new LineCell());
		ObservableList<Integer> locationList = FXCollections.observableArrayList(Line.BOTTOM,Line.TOP,Line.RIGHT,Line.LEFT);
		A_Location.setItems(locationList);
		A_Location.setCellFactory(new locationCell());
		A_Location.setButtonCell(new locationCell().call(null));
		A_Location.getSelectionModel().select(0);
		A_Location_AP.setOnAction((ActionEvent) ->{
			if(confirm("路線駅名表示位置")){
				if(A_list.getSelectionModel().getSelectedItems().size() > 0){
					for(Line l: A_list.getSelectionModel().getSelectedItems()){
						l.setNameLocation(A_Location.getSelectionModel().getSelectedItem().intValue());
					}
				}else{
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("変更する路線を選択してください。");
					alert.showAndWait();
				}
			}
		});
		A_Color_AP.setOnAction((ActionEvent) ->{
			if(confirm("路線駅名色")){
				if(A_list.getSelectionModel().getSelectedItems().size() > 0){
					for(Line l: A_list.getSelectionModel().getSelectedItems()){
						l.setNameColor(A_Color.getValue());
					}
				}else{
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("変更する路線を選択してください。");
					alert.showAndWait();
				}
			}
		});
		A_Size.setEditable(true);
		A_Size.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1,Integer.MAX_VALUE,10,1));
		A_Size_AP.setOnAction((ActionEvent) ->{
			if(confirm("路線駅名サイズ")){
				if(A_list.getSelectionModel().getSelectedItems().size() > 0){
					for(Line l: A_list.getSelectionModel().getSelectedItems()){
						l.setNameSize(A_Size.getValue());
					}
				}else{
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("変更する路線を選択してください。");
					alert.showAndWait();
				}
			}
		});
		ObservableList<Integer> A_styleList = FXCollections.observableArrayList(Line.REGULAR,Line.BOLD,Line.ITALIC,
				Line.ITALIC_BOLD);
		A_Style.setItems(A_styleList);
		A_Style.setCellFactory(new A_styleCell());
		A_Style.setButtonCell(new A_styleCell().call(null));
		A_Style.getSelectionModel().select(0);
		A_Style_AP.setOnAction((ActionEvent) ->{
			if(confirm("路線駅名表示位置")){
				if(A_list.getSelectionModel().getSelectedItems().size() > 0){
					for(Line l: A_list.getSelectionModel().getSelectedItems()){
						l.setNameStyle(A_Style.getSelectionModel().getSelectedItem().intValue());
					}
				}else{
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("変更する路線を選択してください。");
					alert.showAndWait();
				}
			}
		});
		A_X.setEditable(true);
		A_X.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0,1));
		A_Y.setEditable(true);
		A_Y.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0,1));
		A_Zure_AP.setOnAction((ActionEvent) ->{
			if(confirm("駅名位置補正")){
				if(A_list.getSelectionModel().getSelectedItems().size() > 0){
					for(Line l: A_list.getSelectionModel().getSelectedItems()){
						l.setNameX(A_X.getValue().intValue());
						l.setNameY(A_Y.getValue().intValue());
					}
				}else{
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("変更する路線を選択してください。");
					alert.showAndWait();
				}
			}
		});
		//タブB（運転経路プロパティ）
		B_LineList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		B_LineList.setCellFactory(new LineCell());
		B_TrainList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		B_TrainList.setCellFactory(new TrainCell());
		B_LineList.getSelectionModel().selectedItemProperty().addListener((ov, oldVal, newVal) ->{
			ObservableList<Line> selectedItems = B_LineList.getSelectionModel().getSelectedItems();
			if(selectedItems.size() == 1){//単一選択の時はその路線の駅をセット
				B_TrainList.setItems(selectedItems.get(0).getTrains());
			}else{//無選択or複数選択なら空欄
				B_TrainList.setItems(null);
			}
		});
		B_lineColor_AP.setOnAction((ActionEvent) ->{
			if(confirm("ラインカラー")){
				for(Train train: B_getEditList()){
					train.setLineColor(B_lineColor.getValue());
				}
			}
			uic.ReDraw();
		});
		B_lineZure.setEditable(true);
		B_lineZure.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0,1));
		B_lineZure_AP.setOnAction((ActionEvent) ->{
			if(confirm("ライン位置")){
				for(Train train: B_getEditList()){
					train.setLineDistance(B_lineZure.getValue().intValue());
				}
			}
			uic.ReDraw();
		});
		B_lineWidth.setEditable(true);
		B_lineWidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0,Integer.MAX_VALUE,10,1));
		B_lineWidth_AP.setOnAction((ActionEvent) ->{
			if(confirm("ライン太さ")){
				for(Train train: B_getEditList()){
					train.setLineWidth(B_lineWidth.getValue().intValue());
				}
			}
			uic.ReDraw();
		});
		B_linePattern.setCellFactory(new LineDashCell());
		B_linePattern.setButtonCell(new LineDashCell().call(null));
		B_linePattern_AP.setOnAction((ActionEvent) ->{
			if(confirm("線パターン")){
				for(Train train: B_getEditList()){
					train.setLineDash(B_linePattern.getSelectionModel().getSelectedItem());
				}
			}
			uic.ReDraw();
		});
		B_lineA.setEditable(true);
		B_lineA.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0,1));
		B_lineA_AP.setOnAction((ActionEvent) ->{
			if(confirm("ライン端補正A")){
				for(Train train: B_getEditList()){
					train.setEdgeA(B_lineA.getValue().intValue());
				}
			}
			uic.ReDraw();
		});
		B_lineB.setEditable(true);
		B_lineB.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0,1));
		B_lineB_AP.setOnAction((ActionEvent) ->{
			if(confirm("ライン端補正B")){
				for(Train train: B_getEditList()){
					train.setEdgeB(B_lineB.getValue().intValue());
				}
			}
			uic.ReDraw();
		});
		B_markColor_AP.setOnAction((ActionEvent) ->{
			if(confirm("マークカラー")){
				for(Train train: B_getEditList()){
					train.setMarkColor(B_markColor.getValue());
				}
			}
			uic.ReDraw();
		});
		B_markSize.setEditable(true);
		B_markSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0,Integer.MAX_VALUE,10,1));
		B_markSize_AP.setOnAction((ActionEvent) ->{
			if(confirm("マーク大きさ")){
				for(Train train: B_getEditList()){
					train.setMarkSize(B_markSize.getValue().intValue());
				}
			}
			uic.ReDraw();
		});
		B_markType.setCellFactory(new StopMarkCell());
		B_markType.setButtonCell(new StopMarkCell().call(null));
		B_markType_AP.setOnAction((ActionEvent) ->{
			if(confirm("マークタイプ")){
				for(Train train: B_getEditList()){
					train.setMark(B_markType.getValue());
				}
			}
			uic.ReDraw();
		});
		//タブC（停車駅プロパティ）
		C_LineList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		C_LineList.setCellFactory(new LineCell());
		C_TrainList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		C_TrainList.setCellFactory(new TrainCell());
		C_StopList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		C_StopList.setCellFactory(new StopCell());
		C_LineList.getSelectionModel().selectedItemProperty().addListener((ov, oldVal, newVal) ->{
			ObservableList<Line> selectedItems = C_LineList.getSelectionModel().getSelectedItems();
			if(selectedItems.size() == 1){//単一選択の時はその路線の駅をセット
				C_TrainList.setItems(selectedItems.get(0).getTrains());
			}else{//無選択or複数選択なら空欄
				C_TrainList.setItems(null);
			}
			C_StopList.setItems(null);//停車駅プロパティは空欄にする
		});
		C_TrainList.getSelectionModel().selectedItemProperty().addListener((ov, oldVal, newVal) ->{
			ObservableList<Train> selectedItems = C_TrainList.getSelectionModel().getSelectedItems();
			if(selectedItems.size() == 1){//単一選択の時はその路線の駅をセット
				C_StopList.setItems(selectedItems.get(0).getStops());
			}else{//無選択or複数選択なら空欄
				C_StopList.setItems(null);
			}
		});
		C_X.setEditable(true);
		C_X.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0,1));
		C_Y.setEditable(true);
		C_Y.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0,1));
		C_loc_AP.setOnAction((ActionEvent) ->{
			if(confirm("ライン位置補正")){
				if(C_StopList.getSelectionModel().getSelectedItems().size() > 0){
					for(TrainStop ts : C_StopList.getSelectionModel().getSelectedItems()){
						ts.setShiftX(C_X.getValue().intValue());
						ts.setShiftY(C_Y.getValue().intValue());
					}
				}else if(C_TrainList.getSelectionModel().getSelectedItems().size() > 0){
					for(Train train: C_TrainList.getSelectionModel().getSelectedItems()){
						for(TrainStop ts: train.getStops()){
							ts.setShiftX(C_X.getValue().intValue());
							ts.setShiftY(C_Y.getValue().intValue());
						}
					}
				}else if(C_LineList.getSelectionModel().getSelectedItems().size() > 0){
					for(Line l: C_LineList.getSelectionModel().getSelectedItems()){
						for(Train train: l.getTrains()){
							for(TrainStop ts: train.getStops()){
								ts.setShiftX(C_X.getValue().intValue());
								ts.setShiftY(C_Y.getValue().intValue());
							}
						}
					}
				}else{
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("変更する駅を選択してください。");
					alert.showAndWait();
				}
			}
			uic.ReDraw();
		});
		C_Mark.setCellFactory(new StopMarkCell());
		C_Mark.setButtonCell(new StopMarkCell().call(null));
		C_Mark_AP.setOnAction((ActionEvent) ->{
			if(confirm("停車駅マーク")){
				if(C_StopList.getSelectionModel().getSelectedItems().size() > 0){
					for(TrainStop ts : C_StopList.getSelectionModel().getSelectedItems()){
						if(C_Mark.getSelectionModel().getSelectedItem() != null)
							ts.setMark(C_Mark.getSelectionModel().getSelectedItem());
					}
				}else if(C_TrainList.getSelectionModel().getSelectedItems().size() > 0){
					for(Train train: C_TrainList.getSelectionModel().getSelectedItems()){
						for(TrainStop ts: train.getStops()){
							if(C_Mark.getSelectionModel().getSelectedItem() != null)
								ts.setMark(C_Mark.getSelectionModel().getSelectedItem());
						}
					}
				}else if(C_LineList.getSelectionModel().getSelectedItems().size() > 0){
					for(Line l: C_LineList.getSelectionModel().getSelectedItems()){
						for(Train train: l.getTrains()){
							for(TrainStop ts: train.getStops()){
								if(C_Mark.getSelectionModel().getSelectedItem() != null)
									ts.setMark(C_Mark.getSelectionModel().getSelectedItem());
							}
						}
					}
				}else{
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("変更する駅を選択してください。");
					alert.showAndWait();
				}
			}
			uic.ReDraw();
		});
		//タブD（駅プロパティ）
		D_LineList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		D_LineList.setCellFactory(new LineCell());
		D_StaList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		D_StaList.setCellFactory(new StationCell());
		D_LineList.getSelectionModel().selectedItemProperty().addListener((ov, oldVal, newVal) ->{
			ObservableList<Line> selectedItems = D_LineList.getSelectionModel().getSelectedItems();
			if(selectedItems.size() == 1){//単一選択の時はその路線の駅をセット
				D_StaList.setItems(selectedItems.get(0).getStations());
			}else{//無選択or複数選択なら空欄
				D_StaList.setItems(null);
			}
		});
		D_TBstaOn_AP.setOnAction((ActionEvent) ->{
			if(confirm(null)){
				if(D_LineList.getSelectionModel().getSelectedItems().size() > 1){//複数の路線が選択されている。
					for(Line line: D_LineList.getSelectionModel().getSelectedItems()){
						for(Station sta: line.getStations()){
							sta.setShiftBase(D_TBstaOn.isSelected());
						}
					}
				}else{
					if(D_StaList.getSelectionModel().getSelectedItems().size() == 0){
						Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
						alert.getDialogPane().setContentText("変更する駅を選択してください。");
						alert.showAndWait();
					}else{
						for(Station sta: D_StaList.getSelectionModel().getSelectedItems()){
							sta.setShiftBase(D_TBstaOn.isSelected());
						}
					}
				}
			}
			uic.ReDraw();
		});
		D_staX.setEditable(true);
		D_staX.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0,1));
		D_staY.setEditable(true);
		D_staY.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,Integer.MAX_VALUE,0,1));
		D_sta_AP.setOnAction((ActionEvent) ->{
			if(confirm("駅別の名前位置補正")){
				if(D_LineList.getSelectionModel().getSelectedItems().size() > 1){//複数の路線が選択されている。
					for(Line line: D_LineList.getSelectionModel().getSelectedItems()){
						for(Station sta: line.getStations()){
							sta.setNameX(D_staX.getValue());
							sta.setNameY(D_staY.getValue());
						}
					}
				}else{
					if(D_StaList.getSelectionModel().getSelectedItems().size() == 0){
						Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
						alert.getDialogPane().setContentText("変更する駅を選択してください。");
						alert.showAndWait();
					}else{
						for(Station sta: D_StaList.getSelectionModel().getSelectedItems()){
							sta.setNameX(D_staX.getValue());
							sta.setNameY(D_staY.getValue());
						}
					}
				}
			}
			uic.ReDraw();
		});
		ObservableList<Integer> staMukiList = FXCollections.observableArrayList(Station.TEXT_TATE_BOTTOM,Station.TEXT_TATE_TOP,
				Station.TEXT_YOKO_RIGHT,Station.TEXT_YOKO_LEFT,Station.TEXT_UNSET);
		D_staMuki.setItems(staMukiList);
		D_staMuki.setCellFactory(new staMukiCell());
		D_staMuki.setButtonCell(new staMukiCell().call(null));
		D_staMuki.getSelectionModel().select(0);
		D_staMuki_AP.setOnAction((ActionEvent) ->{
			if(confirm("駅名表示位置")){
				if(D_LineList.getSelectionModel().getSelectedItems().size() > 1){//複数の路線が選択されている。
					for(Line line: D_LineList.getSelectionModel().getSelectedItems()){
						for(Station sta: line.getStations()){
							if(D_staMuki.getSelectionModel().getSelectedItem() != null)
								sta.setMuki(D_staMuki.getSelectionModel().getSelectedItem().intValue());
						}
					}
				}else{
					if(D_StaList.getSelectionModel().getSelectedItems().size() == 0){
						Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
						alert.getDialogPane().setContentText("変更する駅を選択してください。");
						alert.showAndWait();
					}else{
						for(Station sta: D_StaList.getSelectionModel().getSelectedItems()){
							if(D_staMuki.getSelectionModel().getSelectedItem() != null)
								sta.setMuki(D_staMuki.getSelectionModel().getSelectedItem().intValue());
						}
					}
				}
			}
			uic.ReDraw();
		});
		D_staSize.setEditable(true);
		D_staSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1,Integer.MAX_VALUE,10,1));
		D_staSize_AP.setOnAction((ActionEvent) ->{
			if(confirm("駅名の大きさ")){
				if(D_LineList.getSelectionModel().getSelectedItems().size() > 1){//複数の路線が選択されている。
					for(Line line: D_LineList.getSelectionModel().getSelectedItems()){
						for(Station sta: line.getStations()){
							sta.setNameSize(D_staSize.getValue().intValue());
						}
					}
				}else{
					if(D_StaList.getSelectionModel().getSelectedItems().size() == 0){
						Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
						alert.getDialogPane().setContentText("変更する駅を選択してください。");
						alert.showAndWait();
					}else{
						for(Station sta: D_StaList.getSelectionModel().getSelectedItems()){
							sta.setNameSize(D_staSize.getValue().intValue());
						}
					}
				}
			}
			uic.ReDraw();
		});
		ObservableList<Integer> staStyleList = FXCollections.observableArrayList(Station.REGULAR, Station.BOLD,
				Station.ITALIC, Station.BOLD_ITALIC, Station.STYLE_UNSET);
		D_staStyle.setItems(staStyleList);
		D_staStyle.setCellFactory(new staStyleCell());
		D_staStyle.setButtonCell(new staStyleCell().call(null));
		D_staStyle.getSelectionModel().select(0);
		D_staStyle_AP.setOnAction((ActionEvent) ->{
			if(confirm("駅名スタイル")){
				if(D_LineList.getSelectionModel().getSelectedItems().size() > 1){//複数の路線が選択されている。
					for(Line line: D_LineList.getSelectionModel().getSelectedItems()){
						for(Station sta: line.getStations()){
							if(D_staStyle.getSelectionModel().getSelectedItem() != null)
								sta.setNameStyle(D_staStyle.getSelectionModel().getSelectedItem().intValue());
						}
					}
				}else{
					if(D_StaList.getSelectionModel().getSelectedItems().size() == 0){
						Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
						alert.getDialogPane().setContentText("変更する駅を選択してください。");
						alert.showAndWait();
					}else{
						for(Station sta: D_StaList.getSelectionModel().getSelectedItems()){
							if(D_staStyle.getSelectionModel().getSelectedItem() != null)
								sta.setNameStyle(D_staStyle.getSelectionModel().getSelectedItem().intValue());
						}
					}
				}
			}
			uic.ReDraw();
		});
	}
	boolean confirm(String text){//trueならOK
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setHeaderText("一斉変更の確認");
		if(text == null){
			alert.setContentText("このパラメーターを選択された全てのアイテムに対して変更します。よろしいですか？");
		}else{
			alert.setContentText(text+" を選択された全てのアイテムに対して変更します。よろしいですか？");
		}
		Optional<ButtonType> result = alert.showAndWait();
		if(result.get() == ButtonType.OK){
			return true;
		}else{
			return false;
		}
	}
	ObservableList<Train> B_getEditList(){//編集する対象を返す
		ObservableList<Train> list = FXCollections.observableArrayList();
		if(B_LineList.getSelectionModel().getSelectedItems().size() > 1){//複数の路線が選択されている。
			for(Line line: B_LineList.getSelectionModel().getSelectedItems()){
				list.addAll(line.getTrains());
			}
		}else{
			if(B_TrainList.getSelectionModel().getSelectedItems().size() == 0){
				Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("変更する駅を選択してください。");
				alert.showAndWait();
			}else{
				list = B_TrainList.getSelectionModel().getSelectedItems();
			}
		}
		return list;
	}
	public void setObject(ObservableList<Line> lineList, ObservableList<StopMark> markList, 
			ObservableList<DoubleArrayWrapper> lineDashes, UIController uic){
		this.lineList = lineList;
		B_markList.add(StopMark.CIRCLE);
		B_markList.add(StopMark.NO_DRAW);
		B_markList.addAll(markList);
		this.lineDashes = lineDashes;
		this.uic = uic;
		A_list.setItems(lineList);
		B_LineList.setItems(lineList);
		B_linePattern.setItems(lineDashes);
		B_linePattern.getSelectionModel().selectFirst();
		B_markType.setItems(B_markList);
		B_markType.getSelectionModel().selectFirst();
		C_LineList.setItems(lineList);
		ObservableList<StopMark> mlC = FXCollections.observableArrayList(StopMark.OBEY_LINE,StopMark.NO_DRAW,StopMark.CIRCLE);
		mlC.addAll(markList);
		C_Mark.setItems(mlC);
		C_Mark.getSelectionModel().selectFirst();
		D_LineList.setItems(lineList);
	}
	//以下、セルファクトリ用の内部クラス
	class LineCell extends ListCell<Line> implements Callback<ListView<Line>, ListCell<Line>>{
		@Override
		public ListCell<Line> call(ListView<Line> param) {
			// TODO Auto-generated method stub
			return new LineCell(){
				@Override
				protected void updateItem(Line item, boolean empty){
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
						setGraphic(null);
					} else {
						Color textColor = Color.BLACK;//色は共通規格
						if(item.getNameLocation() == Line.BOTTOM) textColor = Color.DARKBLUE;
						if(item.getNameLocation() == Line.TOP) textColor = Color.DARKRED;
						if(item.getNameLocation() == Line.RIGHT) textColor = Color.DARKGREEN;
						if(item.getNameLocation() == Line.LEFT) textColor = Color.DARKSALMON;
						setTextFill(textColor);
						setText(item.getName());
					}
				}
			};
		}
	}
	class TrainCell extends ListCell<Train> implements Callback<ListView<Train>, ListCell<Train>>{
		@Override
		public ListCell<Train> call(ListView<Train> param) {
			// TODO Auto-generated method stub
			return new TrainCell(){
				@Override
				protected void updateItem(Train item, boolean empty){
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
						setGraphic(null);
					} else {
						setText(item.getName());
					}
				}
			};
		}
	}
	class StopCell extends ListCell<TrainStop> implements Callback<ListView<TrainStop>, ListCell<TrainStop>>{
		@Override
		public ListCell<TrainStop> call(ListView<TrainStop> param) {
			// TODO Auto-generated method stub
			return new StopCell(){
				@Override
				protected void updateItem(TrainStop item, boolean empty){
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
						setGraphic(null);
					} else {
						setText(item.getSta().getName());
					}
				}
			};
		}
	}
	class StationCell extends ListCell<Station> implements Callback<ListView<Station>, ListCell<Station>>{
		@Override
		public ListCell<Station> call(ListView<Station> param) {
			// TODO Auto-generated method stub
			return new StationCell(){
				@Override
				protected void updateItem(Station item, boolean empty){
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
						setGraphic(null);
					} else {
						Color textColor = Color.BLACK;//色は共通規格
						if(item.getMuki() == Station.TEXT_TATE_BOTTOM) textColor = Color.DARKBLUE;
						if(item.getMuki() == Station.TEXT_TATE_TOP) textColor = Color.DARKRED;
						if(item.getMuki() == Station.TEXT_YOKO_RIGHT) textColor = Color.DARKGREEN;
						if(item.getMuki() == Station.TEXT_YOKO_LEFT) textColor = Color.DARKSALMON;
						setTextFill(textColor);
						if(item.getNameSize() == -1) setTextFill(Color.GRAY);//非表示設定ならグレー
						setText(item.getName());
					}
				}
			};
		}
	}
	class staMukiCell extends ListCell<Integer> implements Callback<ListView<Integer>, ListCell<Integer>>{
		@Override
		public ListCell<Integer> call(ListView<Integer> param) {
			// TODO Auto-generated method stub
			return new staMukiCell(){
				@Override
				protected void updateItem(Integer item, boolean empty){
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
						setGraphic(null);
					} else {
						if(item.intValue() == Station.TEXT_TATE_BOTTOM){
							setTextFill(Color.DARKBLUE);
							setText("縦 - 下付き");
						}
						if(item.intValue() == Station.TEXT_TATE_TOP){
							setTextFill(Color.DARKRED);
							setText("縦 - 上付き");
						}
						if(item.intValue() == Station.TEXT_YOKO_LEFT){
							setTextFill(Color.DARKSALMON);
							setText("横 - 左付き");
						}
						if(item.intValue() == Station.TEXT_YOKO_RIGHT){
							setTextFill(Color.DARKGREEN);
							setText("横 - 右付き");
						}
						if(item.intValue() == Station.TEXT_UNSET){
							setTextFill(Color.BLACK);
							setText("路線準拠");
						}
					}
				}
			};
		}
	}
	class locationCell extends ListCell<Integer> implements Callback<ListView<Integer>, ListCell<Integer>>{
		@Override
		public ListCell<Integer> call(ListView<Integer> param) {
			// TODO Auto-generated method stub
			return new locationCell(){
				@Override
				protected void updateItem(Integer item, boolean empty){
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
						setGraphic(null);
					} else {
						if(item.intValue() == Line.BOTTOM){
							setTextFill(Color.DARKBLUE);
							setText("縦 - 下付き");
						}
						if(item.intValue() == Line.TOP){
							setTextFill(Color.DARKRED);
							setText("縦 - 上付き");
						}
						if(item.intValue() == Line.LEFT){
							setTextFill(Color.DARKSALMON);
							setText("横 - 左付き");
						}
						if(item.intValue() == Line.RIGHT){
							setTextFill(Color.DARKGREEN);
							setText("横 - 右付き");
						}
					}
				}
			};
		}
	}
	class staStyleCell extends ListCell<Integer> implements Callback<ListView<Integer>, ListCell<Integer>>{
		@Override
		public ListCell<Integer> call(ListView<Integer> param) {
			// TODO Auto-generated method stub
			return new staStyleCell(){
				@Override
				protected void updateItem(Integer item, boolean empty){
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
						setGraphic(null);
					} else {
						if(item.intValue() == Station.REGULAR) setText("REGULAR");
						if(item.intValue() == Station.BOLD) setText("BOLD");
						if(item.intValue() == Station.ITALIC) setText("ITALIC");
						if(item.intValue() == Station.BOLD_ITALIC) setText("BOLD_ITALIC");
						if(item.intValue() == Station.STYLE_UNSET) setText("路線準拠");
					}
				}
			};
		}
	}
	class A_styleCell extends ListCell<Integer> implements Callback<ListView<Integer>, ListCell<Integer>>{
		@Override
		public ListCell<Integer> call(ListView<Integer> param) {
			// TODO Auto-generated method stub
			return new A_styleCell(){
				@Override
				protected void updateItem(Integer item, boolean empty){
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
						setGraphic(null);
					} else {
						if(item.intValue() == Line.REGULAR) setText("REGULAR");
						if(item.intValue() == Line.BOLD) setText("BOLD");
						if(item.intValue() == Line.ITALIC) setText("ITALIC");
						if(item.intValue() == Line.ITALIC_BOLD) setText("BOLD_ITALIC");
					}
				}
			};
		}
	}
}

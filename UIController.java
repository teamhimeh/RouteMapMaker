package RouteMapMaker;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UIController implements Initializable{
	
	final double EPSILON = 0.00001;//doubleでの比較用。double変数は==で比較しちゃだめです！
	private ObservableList<String> rnList = FXCollections.observableArrayList();
	private ObservableList<String> tStaListOb = FXCollections.observableArrayList();
	private ObservableList<String> snList = FXCollections.observableArrayList();
	private ObservableList<String> trList = FXCollections.observableArrayList();
	private ObservableList<StopMark> markList = FXCollections.observableArrayList();//駅ごと
	private ObservableList<StopMark> trainMarkList = FXCollections.observableArrayList();//経路ごと
	private ObservableList<Line> lineList;
	private Line line;
	private Station movingSt;
	private ObservableList<MvSta> movingStList = FXCollections.observableArrayList();
	private boolean firstSet = false;//移動前の駅が固定座標だったか否か
	private FreeItem movingItem = null;
	private GraphicsContext gc;
	private double y_largest = 0;
	private double x_largest = 0;
	private ToggleGroup esGroup;//どちらの編集モードかのToggleGroup
	private final double pointRadius = 3;//駅の点の半径
	final double canvasMargin = 200;
	private final double version = 8;//セーブファイルのバージョン。セーブファイルに完全な互換性がなくなった時に変更する。
	private final double ReleaseVersion = 11;//リリースバージョン。ユーザーへの案内用
	private File dataFile;
	private Stage mainStage;//この画面のstage。MODALにするのに使ったり
	private ColorWrapper bgColor = new ColorWrapper(Color.WHITESMOKE);//路線図の背景カラー。デフォルトはWHITESMOKE
	private double zoom = 1.0;//canvas上での表示倍率。mapDrawのみに適用する。
	protected double[] canvasOriginal = new double[2];//mapDrawで1倍の時のcanvasのサイズを記録しておく。
	private StringProperty stationFontFamily = new SimpleStringProperty("system");//駅名に使用するフォントファミリ名
	private ObservableList<StopMark> customMarks = FXCollections.observableArrayList();//カスタム停車駅マークを保持するクラス。
	private ObservableList<FreeItem> freeItems = FXCollections.observableArrayList();//自由挿入テキスト、画像を保持するクラス。
	private Configuration config = new Configuration();
	private Stage configStage;
	private boolean configWindowOpened = false;//環境設定ウィンドウが既に開かれているかどうか
	private FreeItemsController fic;
	private Stage fiStage;
	private boolean fiWindowOpened = false;//freeItemウィンドウが既に開かれているかどうか
	private MainURManager urManager = MainURManager.urManager;
	private ObservableList<DoubleArrayWrapper> lineDashes = FXCollections.observableArrayList();//ライン点線パターンを記憶。
	private Stage changeAllStage;
	private boolean changeAllWindowOpened = false;
	private boolean shortCutKeyPressed = false;//コマンドorCtrlキーが押されてるか否か
	private boolean isLoading = false; //読み込み処理でUIのlistenerが反応するため，それの処理
	
	@FXML AnchorPane leftPane;
	@FXML AnchorPane rightPane;
	@FXML Button RouteDelete;
	@FXML Button RouteAdd;
	@FXML Button RouteLoad;
	@FXML Button staRemoveRestr;
	@FXML Button staDeConnect;
	@FXML Button StationDelete;
	@FXML Button StationAdd;
	@FXML Button stationFont;
	@FXML Button tStaEdit;
	@FXML Button TrainAdd;
	@FXML Button TrainDelete;
	@FXML Button TrainCopy;
	@FXML Button TT_UP;
	@FXML Button TT_DOWN;
	@FXML Button RRT_UP;
	@FXML Button RRT_DOWN;
	@FXML Canvas canvas;
	@FXML CheckBox staCurveConnection;
	@FXML ColorPicker re_bg_CP;
	@FXML ColorPicker re_line_CP;
	@FXML ColorPicker re_mark_CP;
	@FXML ColorPicker RouteColor;
	@FXML ComboBox<StopMark> re_mark_CB;
	@FXML ComboBox<StopMark> re_staMark_CB;
	@FXML ComboBox<DoubleArrayWrapper> re_linePattern_CB;
	@FXML ComboBox<String> re_staPMuki_CB;
	@FXML ComboBox<String> re_staPStyle_CB;
	@FXML ComboBox<String> RouteStyle;
	@FXML ComboBox<String> staStyle;
	@FXML Label currentFont;
	@FXML Label mouseLocation;
	@FXML ListView<String> RouteTable;
	@FXML ListView<String> StationList;
	@FXML ListView<String> TrainTable;
	@FXML ListView<String> tStaList;
	@FXML ListView<String> R_RouteTable;
	@FXML MenuBar menubar;
	@FXML MenuItem mb_new;
	@FXML MenuItem mb_open;
	@FXML MenuItem mb_save;
	@FXML MenuItem mb_saveAs;
	@FXML MenuItem mb_exportImage;
	@FXML MenuItem mb_about;
	@FXML MenuItem mb_goWiki;
	@FXML MenuItem mb_checkUpdate;
	@FXML MenuItem mb_config;
	@FXML MenuItem mb_changeAll;
	@FXML MenuItem mb_editCustomMark;
	@FXML MenuItem mb_setCustomMark;
	@FXML MenuItem mb_freeItem;
	@FXML MenuItem mb_lineDashes;
	@FXML MenuItem mb_transform;
	@FXML MenuItem mb_undo;
	@FXML MenuItem mb_redo;
	@FXML MenuItem mb_R;
	@FXML MenuItem mb_T;
	@FXML ToggleButton leftEditButton;
	@FXML ToggleButton rightEditButton;
	@FXML ToggleButton re_staPShift_TB;
	@FXML RadioButton tateTop;
	@FXML RadioButton tateBottom;
	@FXML RadioButton yokoLeft;
	@FXML RadioButton yokoRight;
	@FXML RadioButton staTateBottom;
	@FXML RadioButton staTateTop;
	@FXML RadioButton staYokoRight;
	@FXML RadioButton staYokoLeft;
	@FXML RadioButton staObeyLine;
	@FXML Rectangle draggedRect;
	@FXML ScrollPane canvasPane;
	@FXML Slider ZoomSlider;
	@FXML Spinner<Integer> re_line_SP;
	@FXML Spinner<Integer> re_lineC_SP;
	@FXML Spinner<Integer> re_mark_SP;
	@FXML Spinner<Integer> re_lineSA_SP;
	@FXML Spinner<Integer> re_lineSB_SP;
	@FXML Spinner<Integer> re_staPSize_SP;
	@FXML Spinner<Integer> RouteSize;
	@FXML Spinner<Integer> R_nameX;
	@FXML Spinner<Integer> R_nameY;
	@FXML Spinner<Integer> re_staPX_SP;
	@FXML Spinner<Integer> re_staPY_SP;
	@FXML Spinner<Integer> re_staLAX_SP;
	@FXML Spinner<Integer> re_staLAY_SP;
	@FXML Spinner<Integer> staSize;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		lineList = FXCollections.observableArrayList();
		RouteTable.setItems(rnList);
		RouteTable.setEditable(true);
		RouteTable.setCellFactory(TextFieldListCell.forListView());
		lineList.add(new Line("路線1"));
		newLinePointSet(lineList.get(0));
		rnList.add(lineList.get(0).getName());
		StationList.setCellFactory(TextFieldListCell.forListView());
		gc = canvas.getGraphicsContext2D();
		gc.save();
		(new Thread(){
			@Override
			public void run(){
				checkUpdate(true);
			}
		}).start();
		config.read();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> config.save()));
		//起動時ダイアログの表示
		if(! config.getNoAlert()){
			Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
			alert.getDialogPane().setHeaderText("路線図メーカー使用上の注意・お願い");
			Text text = new Text("本ソフトウェアの使用にあたり以下の3つをお願いしています。\n"
					+ "1.不具合によるデータの破損などに十分注意してください。\n"
					+ "2.随時アップデートを配信しますのでアップデートを確認し、インストールしてください。\n"
					+ "3.不具合を発見した時は開発者へバグ報告をしてください。（Twitter @himeshi_hob にお願いします。）\n\n"
					+ "開発者の情報、ライセンスなどはHelp→Aboutを参照してください。");
			CheckBox box = new CheckBox("次からこのダイアログを表示しない");
			VBox vbox = new VBox(10.0,text,box);
			alert.getDialogPane().setContent(vbox);
			alert.showAndWait();
			config.setNoAlert(box.isSelected());
		}
		selectSomething(true);
		lineDraw();
		line = lineList.get(0);
		RouteAdd.setOnAction((ActionEvent) ->{
			createNewLine(null);
			lineDraw();
		});
		RouteDelete.setOnAction((ActionEvent) ->{
			int index = RouteTable.getSelectionModel().getSelectedIndex();
			if(index != -1){
				urManager.push(lineList, URElements.ArrayCommands.REMOVE, index, lineList.get(index));
				lineList.remove(index);
				rnList.clear();
				for(int i=0; i < lineList.size(); i++){
					rnList.add(lineList.get(i).getName());
				}
				lineDraw();
			}
		});
		RouteLoad.setOnAction((ActionEvent) ->{
			// 駅名が書かれたファイルを選択
			FileChooser fc = new FileChooser();
			FileChooser.ExtensionFilter txt = new FileChooser.ExtensionFilter("テキストファイル（*.txt）", "*.txt");
			fc.setTitle("ファイルを開く");
			fc.getExtensionFilters().add(txt);
			File selectedFile = fc.showOpenDialog(null);
			ArrayList<String> staNames = new ArrayList<String>();
			try{
				if(fc.getSelectedExtensionFilter() == txt){
					BufferedReader br = new BufferedReader(new FileReader(selectedFile));
					String line = br.readLine();
					while(line != null) {
						staNames.add(line);
						line = br.readLine();
					}
					br.close();
					createNewLine(staNames);
					lineDraw();
				}
			}catch(IOException e){
				Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("エラーが発生しました。ファイルを読み込めません。");
				alert.showAndWait();
			}catch(Exception e){
				e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("エラーが発生しました。\n"
						+ "以下のエラーメッセージを@himeshi_hobにお知らせください。\n" + e.getLocalizedMessage());
				alert.showAndWait();
			}
		});
		//駅名文字列の向きに関するトグルボタンの設定（路線単位）
		ToggleGroup mukiGroup = new ToggleGroup();
		tateTop.setToggleGroup(mukiGroup);
		tateBottom.setToggleGroup(mukiGroup);
		yokoRight.setToggleGroup(mukiGroup);
		yokoLeft.setToggleGroup(mukiGroup);
		tateBottom.setSelected(true);
		mukiGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle old_toggle,
				Toggle new_toggle) ->{
					int RouteIndex = RouteTable.getSelectionModel().getSelectedIndex();
					int newT = -1;
					int oldT = -1;
					if(old_toggle == tateBottom) oldT = Line.BOTTOM;
					if(old_toggle == tateTop) oldT = Line.TOP;
					if(old_toggle == yokoLeft) oldT = Line.LEFT;
					if(old_toggle == yokoRight) oldT = Line.RIGHT;
					if(new_toggle == tateBottom) newT = Line.BOTTOM;
					if(new_toggle == tateTop) newT = Line.TOP;
					if(new_toggle == yokoLeft) newT = Line.LEFT;
					if(new_toggle == yokoRight) newT = Line.RIGHT;
					if(RouteIndex == -1){
						Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
						alert.getDialogPane().setContentText("路線を選択してください。");
						alert.showAndWait();
					}else{
						if(lineList.get(RouteIndex).getNameLocation() == oldT)
							urManager.push(lineList.get(RouteIndex).getNameLocationProperty(), oldT, newT);
						lineList.get(RouteIndex).setNameLocation(newT);
					}
					lineDraw();
				});
		RouteTable.getSelectionModel().selectedItemProperty().addListener( (ObservableValue<? extends String> ov, 
				String old_val, String new_val) -> {
					//路線が選択された時の処理
					if(RouteTable.getSelectionModel().getSelectedIndex() != -1){//index-1は何も選択されてないことを示すので処理しない。
						int index = RouteTable.getSelectionModel().getSelectedIndex();
						line = lineList.get(index);
						StationList.setItems(snList);
						snList.clear();
						for(int i=0; i < line.getStations().size(); i++){
							snList.add(line.getStations().get(i).getName());
						}
						StationList.getSelectionModel().selectLast();
						StationList.setEditable(true);
						//トグルの選択と駅名表示位置設定
						if(lineList.get(index).getNameLocation() == Line.BOTTOM) mukiGroup.selectToggle(tateBottom);
						if(lineList.get(index).getNameLocation() == Line.TOP) mukiGroup.selectToggle(tateTop);
						if(lineList.get(index).getNameLocation() == Line.RIGHT) mukiGroup.selectToggle(yokoRight);
						if(lineList.get(index).getNameLocation() == Line.LEFT) mukiGroup.selectToggle(yokoLeft);
						RouteSize.getValueFactory().setValue(lineList.get(index).getNameSize());//サイズ設定
						RouteStyle.getSelectionModel().select(lineList.get(index).getNameStyle());//style設定
						RouteColor.setValue(lineList.get(index).getNameColor());//色設定
					}
				});
		RouteTable.setOnEditCommit(new EventHandler<ListView.EditEvent<String>>(){
			@Override
			public void handle(ListView.EditEvent<String> t){
				if(! t.getNewValue().equals("")){
					if(! t.getNewValue().equals(lineList.get(t.getIndex()).getName())){
						urManager.push(lineList.get(t.getIndex()).getNameProperty(), lineList.get(t.getIndex()).getName(),
						t.getNewValue());
						lineList.get(t.getIndex()).setName(t.getNewValue());
					}
				}
				rnList.clear();
				for(int i=0; i < lineList.size(); i++){
					rnList.add(lineList.get(i).getName());
				}
				RouteTable.getSelectionModel().select(t.getIndex());
			}
		});
		RouteColor.setOnAction((ActionEvent) -> {
			int index = RouteTable.getSelectionModel().getSelectedIndex();
			if(index != -1){
				urManager.push(lineList.get(index).getNameColorProperty(), lineList.get(index).getNameColor(),
						RouteColor.getValue());
				lineList.get(index).setNameColor(RouteColor.getValue());
				lineDraw();
			}
		});
		RouteSize.setEditable(true);
		RouteSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,Integer.MAX_VALUE,15,1));
		RouteSize.valueProperty().addListener((obs, oldVal, newVal) -> {
			int index = RouteTable.getSelectionModel().getSelectedIndex();
			if(index != -1){
				if(oldVal.intValue() == lineList.get(index).getNameSize())
					urManager.push(lineList.get(index).getNameSizeProperty(), oldVal, newVal);
				lineList.get(index).setNameSize(RouteSize.getValue());
				lineDraw();
			}
		});
		ObservableList<String> RouteStyle_Options = FXCollections.observableArrayList("Regular", "Italic", "Bold", "BoldItalic");
		RouteStyle.setItems(RouteStyle_Options);
		RouteStyle.valueProperty().addListener((obs, oldVal, newVal) -> {
			int index = RouteTable.getSelectionModel().getSelectedIndex();
			if(index != -1){
				if(oldVal != null){
					if((oldVal.equals("Regular") && lineList.get(index).getNameStyle() == Line.REGULAR) ||
							(oldVal.equals("Italic") && lineList.get(index).getNameStyle() == Line.ITALIC) ||
							(oldVal.equals("Bold") && lineList.get(index).getNameStyle() == Line.BOLD) ||
							(oldVal.equals("BoldItalic") && lineList.get(index).getNameStyle() == Line.ITALIC_BOLD))
						urManager.push(lineList.get(index).getNameStyleProperty(), lineList.get(index).getNameStyle(), 
								RouteStyle.getSelectionModel().getSelectedIndex());
				}
				lineList.get(index).setNameStyle(RouteStyle.getSelectionModel().getSelectedIndex());
				lineDraw();
			}
		});
		stationFont.setOnAction((ActionEvent) ->{//フォントを設定。これは全路線共通です。
			String oldVal = stationFontFamily.get();
			stationFontFamily.set(selectFontFamily(stationFontFamily.get()));
			if(! stationFontFamily.get().equals(oldVal)) urManager.push(stationFontFamily, oldVal, stationFontFamily.get());
			currentFont.setText(stationFontFamily.get());
			currentFont.setFont(Font.font(stationFontFamily.get()));
			lineDraw();
		});
		StationAdd.setOnAction((ActionEvent) ->{
			int index = StationList.getSelectionModel().getSelectedIndex();
			if(index == 0){
				Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("駅は2番目以降に挿入してください。");
				alert.showAndWait();
			}else{
				int staNum = 0;
				while(true){
					String d = staNum + "駅";
					if(findStaByName(d)!=null){
						staNum++;
					}else{
						break;
					}
				}
				Station newSta = new Station(staNum + "駅");
				urManager.push(line.getStations(), URElements.ArrayCommands.ADD, index, newSta);
				line.getStations().add(index, newSta);
				//固定座標ではないが参照座標を登録する。
				double[] p = detectCoordinate(index, RouteTable.getSelectionModel().getSelectedIndex());
				line.getStations().get(index).setInterPoint(p[0], p[1]);
				snList.clear();
				for(int i=0; i < line.getStations().size(); i++){
					snList.add(line.getStations().get(i).getName());
				}
				StationList.getSelectionModel().select(index + 1);
				lineDraw();
			}
		});
		StationDelete.setOnAction((ActionEvent) ->{
			int index = StationList.getSelectionModel().getSelectedIndex();
			if(index == 0 || index == line.getStations().size() - 1){
				//削除は受け付けない
				Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("始点と終点は削除できません。");
				alert.showAndWait();
			}else if(index != -1){
				//選択されたlineで削除対象駅が路線に追加されているかを検査する
				ArrayList<Integer[]> remove_Candidates = new ArrayList<Integer[]>();//{経路番号,停車場番号}
				ObservableList<TrainStop> removedStops = FXCollections.observableArrayList();
				Station removeCandidate = line.getStations().get(index);
				for(int i = 0; i < line.getTrains().size(); i++){
					for(int h = 0; h < line.getTrains().get(i).getStops().size(); h++){
						if(line.getTrains().get(i).getStops().get(h).getSta() == removeCandidate){
							Integer[] id = {i,h};
							remove_Candidates.add(id);
						}
					}
				}
				if(remove_Candidates.size() > 0){
					StringBuilder names = new StringBuilder();
					for(Integer[] id: remove_Candidates){
						names.append(line.getTrains().get(id[0]).getName()+" ");//空白で区切る
					}
					Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
					alert.setContentText("運転経路"+names.toString()+"に削除対象駅が含まれています。削除してよろしいですか？");
					Optional<ButtonType> result = alert.showAndWait();
					if(result.get() == ButtonType.OK){
						for(Integer[] id: remove_Candidates){
							TrainStop removedTrainStop = line.getTrains().get(id[0]).getStops().remove(id[1].intValue());
							removedStops.add(removedTrainStop);
						}
						
					}
				}
				Station removedSta = line.getStations().remove(index);
				urManager.push(line.getStations(), index, removedSta, line.getTrains(), remove_Candidates, removedStops);
				snList.clear();
				for(int i=0; i < line.getStations().size(); i++){
					snList.add(line.getStations().get(i).getName());
				}
				StationList.getSelectionModel().select(index);
				lineDraw();
			}
		});
		StationList.setOnEditCommit(new EventHandler<ListView.EditEvent<String>>(){
			@Override
			public void handle(ListView.EditEvent<String> t){
				int indexR = RouteTable.getSelectionModel().getSelectedIndex();
				int indexS = StationList.getSelectionModel().getSelectedIndex();
				String prev = lineList.get(indexR).getStations().get(indexS).getName();//変更前の駅名
				StationList.getItems().set(t.getIndex(), t.getNewValue());
				String str = StationList.getSelectionModel().getSelectedItem();//変更しようとしてる駅名
				//途中駅でも接続することにしました。
				if(str.equals("")){
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("駅名は空にはできません。\n"
							+ "中継点を設定するときは駅名大きさパラメーターを-1にしてください。");
					alert.showAndWait();
				}else{
					if(! str.equals(prev)){//もし変更前と変更後が同名なら無視
						//変更候補名と同名の駅があるか調べる
						Station sameSta = null;//同名の駅があればこの変数に入れる。nullならば無いということに。
						int[] indexOfSameSta = new int[2];//座標固定する時にindexが必要になる。
						search:for(int i = 0; i < lineList.size(); i++){
							for(int j = 0; j < lineList.get(i).getStations().size(); j++){
								if(lineList.get(i).getStations().get(j).getName().equals(str)){
									sameSta = lineList.get(i).getStations().get(j);
									indexOfSameSta[0] = i;
									indexOfSameSta[1] = j;
									break search;
								}
							}
						}
						if(sameSta == null){//同名の駅は存在しない。駅名を書き換えるだけ。
							lineList.get(indexR).getStations().get(indexS).setName(str);
							urManager.push(lineList.get(indexR).getStations().get(indexS).getNameProperty(), prev, str);
						}else{//同名の駅が存在する。駅オブジェクト自体を置き換える手続きへ。
							Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
							alert.setContentText("マップ内に同じ駅名の駅があります。その駅と統合してよろしいですか？");
							Optional<ButtonType> result = alert.showAndWait();
							if(result.get() == ButtonType.OK){//置き換え処理へ
								//まず座標非固定点の場合は固定する。
								boolean fixed = sameSta.isSet();
								if(! sameSta.isSet()){
									double[] p = detectCoordinate(indexOfSameSta[1],indexOfSameSta[0]);
									sameSta.setPoint(p[0], p[1]);
								}
								Station replacedSta = lineList.get(indexR).getStations().get(indexS);//置き換える前のオブジェクト
								//置き換えるべき全ての路線の駅について走査する。
								ArrayList<Integer[]> replaceIndex = new ArrayList<Integer[]>();//Undoのためにインデックスを記憶
								for(int i = 0; i < lineList.size(); i++){
									for(int j = 0; j < lineList.get(i).getStations().size(); j++){
										if(lineList.get(i).getStations().get(j) == replacedSta){
											lineList.get(i).getStations().set(j, sameSta);
											Integer[] id = {i,j};
											replaceIndex.add(id);
										}
									}
								}
								urManager.push(lineList, replaceIndex, replacedSta, sameSta, fixed);
							}
						}
					}
				}
				snList.clear();
				for(int i=0; i < line.getStations().size(); i++){
					snList.add(line.getStations().get(i).getName());
				}
				StationList.getSelectionModel().select(indexS);
				lineDraw();
			}
		});
		ToggleGroup staMukiGroup = new ToggleGroup();
		staTateBottom.setToggleGroup(staMukiGroup);
		staTateTop.setToggleGroup(staMukiGroup);
		staYokoRight.setToggleGroup(staMukiGroup);
		staYokoLeft.setToggleGroup(staMukiGroup);
		staObeyLine.setToggleGroup(staMukiGroup);
		staObeyLine.setSelected(true);
		staMukiGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle old_toggle,
				Toggle new_toggle) ->{
					int indexR = RouteTable.getSelectionModel().getSelectedIndex();
					int indexS = StationList.getSelectionModel().getSelectedIndex();
					if(indexR != -1 && indexS != -1){
						//undoRedoのpush
						int oldT = 0;
						int newT = 0;
						if(old_toggle == staTateTop) oldT = Station.TEXT_TATE_TOP;
						if(old_toggle == staTateBottom) oldT = Station.TEXT_TATE_BOTTOM;
						if(old_toggle == staYokoLeft) oldT = Station.TEXT_YOKO_LEFT;
						if(old_toggle == staYokoRight) oldT = Station.TEXT_YOKO_RIGHT;
						if(old_toggle == staObeyLine) oldT = Station.TEXT_UNSET;
						if(new_toggle == staTateTop) newT = Station.TEXT_TATE_TOP;
						if(new_toggle == staTateBottom) newT = Station.TEXT_TATE_BOTTOM;
						if(new_toggle == staYokoLeft) newT = Station.TEXT_YOKO_LEFT;
						if(new_toggle == staYokoRight) newT = Station.TEXT_YOKO_RIGHT;
						if(new_toggle == staObeyLine) newT = Station.TEXT_UNSET;
						if(oldT == lineList.get(indexR).getStations().get(indexS).getMuki())
							urManager.push(lineList.get(indexR).getStations().get(indexS).getMukiProperty(), oldT, newT);
						//変更処理
						lineList.get(indexR).getStations().get(indexS).setMuki(newT);
					}
					lineDraw();
				});
		staSize.setEditable(true);
		staSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1,Integer.MAX_VALUE,0,1));
		staSize.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexR = RouteTable.getSelectionModel().getSelectedIndex();
			int indexS = StationList.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexS != -1){
				if(lineList.get(indexR).getStations().get(indexS).getNameSize() == oldVal.intValue())
					urManager.push(lineList.get(indexR).getStations().get(indexS).getNameSizeProperty(), oldVal, newVal);
				lineList.get(indexR).getStations().get(indexS).setNameSize(staSize.getValue());
				lineDraw();
			}
		});
		ObservableList<String> staStyle_Options = FXCollections.observableArrayList("Regular", "Italic", "Bold", 
				"BoldItalic", "路線準拠");
		staStyle.setItems(staStyle_Options);
		staStyle.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexR = RouteTable.getSelectionModel().getSelectedIndex();
			int indexS = StationList.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexS != -1){
				if(lineList.get(indexR).getStations().get(indexS).getNameStyle() == staStyle_Options.indexOf(oldVal))
					urManager.push(lineList.get(indexR).getStations().get(indexS).getNameStyleProperty(),
							staStyle_Options.indexOf(oldVal), staStyle_Options.indexOf(newVal));
				lineList.get(indexR).getStations().get(indexS).setNameStyle(staStyle.getSelectionModel().getSelectedIndex());
				lineDraw();
			}
		});
		staCurveConnection.setOnAction((ActionEvent)->{
			int indexR = RouteTable.getSelectionModel().getSelectedIndex();
			int indexS = StationList.getSelectionModel().getSelectedIndex();
			Station sta = lineList.get(indexR).getStations().get(indexS);
			ObservableList<Integer> ci = lineList.get(indexR).getCurveIdxs();
			if(staCurveConnection.isSelected()) {
				//idxを追加する
				ci.add(indexS);
				//urManager.push(line.getStations(), URElements.ArrayCommands.ADD, ci.size()-1, Integer(indexS));
			} else {
				//idxを削除する
				int idx = ci.indexOf(indexS);
				ci.remove(idx);
				//urManager.push(line.getStations(), URElements.ArrayCommands.REMOVE, idx, indexS);
			}
		});
		staRemoveRestr.setOnAction((ActionEvent)->{
			int indexR = RouteTable.getSelectionModel().getSelectedIndex();
			int indexS = StationList.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexS != -1){
				if(indexS == 0 || indexS == lineList.get(indexR).getStations().size() - 1){
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("始点または終点の座標固定を解除することはできません");
					alert.showAndWait();
				}else if(detectConnectedLine(lineList.get(indexR).getStations().get(indexS)).size() > 1){
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("複数路線に所属する駅の座標固定を解除することはできません。\n"
							+ "「駅の接続解除」ボタンで駅の接続を解除できます。");
					alert.showAndWait();
				}else{
					lineList.get(indexR).getStations().get(indexS).erasePoint();
					urManager.push(lineList.get(indexR).getStations().get(indexS).getPointSetProperty(), false);
					lineDraw();
				}
			}
		});
		staDeConnect.setOnAction((ActionEvent)->{
			int indexR = RouteTable.getSelectionModel().getSelectedIndex();
			int indexS = StationList.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexS != -1){
				if(detectConnectedLine(lineList.get(indexR).getStations().get(indexS)).size() < 2){
					//この場合は接続を解除する意味がないのでなにもしない。
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("指定された駅は他の路線と接続していません。");
					alert.showAndWait();
				}else{
					Station oldSta = lineList.get(indexR).getStations().get(indexS);
					Station newSta = new Station("新-" + oldSta.getName());
					//以下初期設定。clone使いたいけどshiftCoorでトラブりそうなのでやめる
					newSta.setPoint(oldSta.getPoint()[0] + 50, oldSta.getPoint()[1] + 50);
					newSta.setConnection(oldSta.getConnection());
					newSta.setMuki(oldSta.getMuki());
					newSta.setNameSize(oldSta.getNameSize());
					newSta.setNameStyle(oldSta.getNameStyle());
					//描画位置設定は引き継がないことにする
					lineList.get(indexR).getStations().set(indexS, newSta);
					lineDraw();
					snList.clear();
					for(int i=0; i < lineList.get(indexR).getStations().size(); i++){
						snList.add(lineList.get(indexR).getStations().get(i).getName());
					}
					//交点駅が登録されている運転経路も新駅にチェンジ
					ArrayList<Integer[]> setList = new ArrayList<Integer[]>();
					ObservableList<TrainStop> stopValue = FXCollections.observableArrayList();
					for(int k = 0; k < lineList.get(indexR).getTrains().size(); k++){
						Train train = lineList.get(indexR).getTrains().get(k);
						for(int i = 0; i < train.getStops().size(); i++){
							if(train.getStops().get(i).getSta() == oldSta){
								TrainStop newStop = new TrainStop(newSta);
								Integer[] id = {k,i};
								setList.add(id);
								stopValue.add(train.getStops().get(i));
								stopValue.add(newStop);
								train.getStops().set(i, newStop);
							}
						}
					}
					urManager.push(lineList.get(indexR).getStations(), oldSta, newSta, indexS, lineList.get(indexR).getTrains(),
							setList, stopValue);
					//運転経路編集ウィンドウで変更を反映させる
					int indexRR = R_RouteTable.getSelectionModel().getSelectedIndex();
					int indexT = TrainTable.getSelectionModel().getSelectedIndex();
					if(indexRR != -1 && indexT != -1){
						tStaListOb.clear();
						for(TrainStop ts: lineList.get(indexRR).getTrains().get(indexT).getStops()){
							tStaListOb.add(ts.getSta().getName());
						}
					}
				}
			}
		});
		StationList.getSelectionModel().selectedItemProperty().addListener( (ObservableValue<? extends String> ov, 
				String old_val, String new_val) -> {
					int indexR = RouteTable.getSelectionModel().getSelectedIndex();
					int indexS = StationList.getSelectionModel().getSelectedIndex();
					if(indexR != -1 && indexS != -1){
						Station s = lineList.get(indexR).getStations().get(indexS);
						if(s.getMuki() == Station.TEXT_TATE_TOP) staMukiGroup.selectToggle(staTateTop);
						if(s.getMuki() == Station.TEXT_TATE_BOTTOM) staMukiGroup.selectToggle(staTateBottom);
						if(s.getMuki() == Station.TEXT_YOKO_LEFT) staMukiGroup.selectToggle(staYokoLeft);
						if(s.getMuki() == Station.TEXT_YOKO_RIGHT) staMukiGroup.selectToggle(staYokoRight);
						if(s.getMuki() == Station.TEXT_UNSET) staMukiGroup.selectToggle(staObeyLine);
						staSize.getValueFactory().setValue(s.getNameSize());
						staStyle.getSelectionModel().select(s.getNameStyle());
						staCurveConnection.setDisable(!lineList.get(indexR).isCurvable(indexS));
						staCurveConnection.setSelected(lineList.get(indexR).getCurveConnection(indexS));
					}
				});
		double[] startCoor = new double[2];//ドラッグスタート時の座標を記録する。
		draggedRect.setVisible(false);
		canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>(){//canvas上でマウスが押された時
			@Override
			public void handle(MouseEvent e){
				startCoor[0] = e.getX();
				startCoor[1] = e.getY();
				if(esGroup.getSelectedToggle() == rightEditButton){
					movingSt = searchStation(e.getX(), e.getY());
					if(movingSt == null){
						draggedRect.setVisible(true);
						draggedRect.setX(startCoor[0]);
						draggedRect.setY(startCoor[1]);
						draggedRect.setWidth(0);
						draggedRect.setHeight(0);
						movingStList.clear();
					}else{
						firstSet = movingSt.isSet();
						startCoor[0] = movingSt.getPointUS()[0];//駅移動時の開始座標は開始時のマウス座標ではなく駅座標にする。
						startCoor[1] = movingSt.getPointUS()[1];
						boolean contain = false;
						for(MvSta ms: movingStList){
							if(ms.sta == movingSt) contain = true;
						}
						if(contain && shortCutKeyPressed){//movingStListから選択されたものを削除する
							//ConcurrentModificationExceptionを回避するためにIteratorを使う
							Iterator<MvSta> iter = movingStList.iterator();
							while(iter.hasNext()){
								MvSta ms = iter.next();
								if(ms.sta == movingSt) iter.remove();
							}
						}
						if(! contain){
							if(! shortCutKeyPressed) movingStList.clear();
							movingStList.add(new MvSta(movingSt));
						}
						for(MvSta ms: movingStList){//start座標の更新
							ms.start[0] = ms.sta.getPointUS()[0];
							ms.start[1] = ms.sta.getPointUS()[1];
						}
						draggedRect.setVisible(false);
					}
					lineDraw();
				}else{//leftEditbuttonが選択されている状態
					
				}
			}
		});
		canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>(){//canvas上でマウスがドラッグされた時
			@Override
			public void handle(MouseEvent e){
				if(esGroup.getSelectedToggle() == rightEditButton){
					if(movingSt != null){//特定の駅が選択されている時
						//movingSt.setPoint(e.getX(), e.getY());
						for(MvSta ms: movingStList){
							ms.sta.setPoint(ms.start[0] + e.getX() - startCoor[0], ms.start[1] + e.getY() - startCoor[1]);
						}
						//領域の自動拡大
						if(canvas.getWidth() - e.getX() < canvasMargin) canvas.setWidth(e.getX() + canvasMargin);
						if(canvas.getHeight() - e.getY() < canvasMargin) canvas.setHeight(e.getY() + canvasMargin);
						lineDraw();
					}else{//特定の駅が選択されているわけではないとき
						if(e.getX() - startCoor[0] <= 0){//符号の反転が必要
							draggedRect.setX(e.getX());
							draggedRect.setWidth(startCoor[0] - e.getX());
						}else{//反転必要なし
							draggedRect.setWidth(e.getX() - startCoor[0]);
						}
						if(e.getY() - startCoor[1] <= 0){
							draggedRect.setY(e.getY());
							draggedRect.setHeight(startCoor[1] - e.getY());
						}else{
							draggedRect.setHeight(e.getY() - startCoor[1]);
						}
					}
				}else{//leftEditbuttonが選択されている状態
					
				}
			}
		});
		canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>(){//canvas上でマウスが離された時
			@Override
			public void handle(MouseEvent e){
				if(esGroup.getSelectedToggle() == rightEditButton){
					if(movingSt != null){//特定の駅が選択されている時
						double[] gridedPos = getGridedPoint(e.getX(), e.getY());
						double mouseX = gridedPos[0];
						double mouseY = gridedPos[1];
						for(MvSta ms: movingStList){
							ms.sta.setPoint(ms.start[0] + mouseX - startCoor[0], ms.start[1] + mouseY - startCoor[1]);
						}
						//マウスが全く動いてないかつ全てがもともと座標固定駅だった場合はpushしてはならない
						boolean shouldBePushed = false;
						for(MvSta ms: movingStList){
							//完全にイコールにするとすごく小さな値で差がついてしまう
							if(! ms.isSet || Math.abs(ms.start[0] - ms.sta.getPoint()[0]) > 0.5  || 
									Math.abs(ms.start[1] - ms.sta.getPoint()[1]) > 0.5){
								shouldBePushed = true;
								//System.out.println(ms.start[0]+","+ms.sta.getPoint()[0]+","+ms.start[1]+","+ms.sta.getPoint()[1]);
								break;
							}
						}
						if(shouldBePushed){
							urManager.push(movingStList);
							System.out.println("mouseReleased - pushed!");
						}
						//canvasのサイズを調整する。
						x_largest = 0;
						y_largest = 0;
						for(int i = 0; i < lineList.size(); i++){
							for(int j = 0; j < lineList.get(i).getStations().size(); j++){
								if(lineList.get(i).getStations().get(j).isSet()){
									double[] p = lineList.get(i).getStations().get(j).getPoint();
									if(p[0] > x_largest) x_largest = p[0]; 
									if(p[1] > y_largest) y_largest = p[1]; 
								}
							}
						}
						canvasOriginal[0] = x_largest + canvasMargin;
						canvasOriginal[1] = y_largest + canvasMargin;
						canvas.setWidth(x_largest + canvasMargin);
						canvas.setHeight(y_largest + canvasMargin);
						lineDraw();
					}else{//特定の駅が選択されているわけではないとき
						draggedRect.setVisible(false);
						movingStList = searchStation(draggedRect.getX(), draggedRect.getY(),
								draggedRect.getWidth(), draggedRect.getHeight());
						lineDraw();
					}
				}else{
					
				}
				
			}
		});
		canvas.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>(){//canvas上でマウスが押された時
			@Override
			public void handle(MouseEvent e){
				mouseLocation.setText("X: "+(int)e.getX()+", Y: "+(int)e.getY());
			}
		});
		canvas.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>(){//canvas上でマウスが押された時
			@Override
			public void handle(MouseEvent e){
				mouseLocation.setText("X: "+(int)e.getX()+", Y: "+(int)e.getY());
			}
		});
		canvas.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>(){//canvas上でマウスが押された時
			@Override
			public void handle(MouseEvent e){
				mouseLocation.setText("");
			}
		});
		final EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>(){
			@Override
			public void handle(KeyEvent event) {
				// TODO Auto-generated method stub
				if(event.isShiftDown()) shortCutKeyPressed = true;
				if(! event.isShiftDown()) shortCutKeyPressed = false;
			}
		};
		canvasPane.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
		canvasPane.addEventHandler(KeyEvent.KEY_RELEASED, keyEventHandler);
		//メニューバー項目の動作を定義。
		menubar.setUseSystemMenuBar(config.getMenubarMode());
		mb_new.setOnAction((ActionEvent) ->{
			dataFile = null;
			lineList.clear();
			customMarks.clear();
			freeItems.clear();
			rnList.clear();
			lineList.add(new Line("路線1"));
			newLinePointSet(lineList.get(0));
			rnList.add(lineList.get(0).getName());
			RouteTable.getSelectionModel().select(0);
			urManager.clear();
			lineDraw();
		});
		mb_open.setOnAction((ActionEvent) ->{
			FileChooser fc = new FileChooser();
			FileChooser.ExtensionFilter erm = new FileChooser.ExtensionFilter("路線図メーカーテキスト形式（*.erm）", "*.erm");
			FileChooser.ExtensionFilter rmm = new FileChooser.ExtensionFilter("路線図メーカー形式（*.rmm）", "*.rmm");
			fc.setTitle("ファイルを開く");
			fc.getExtensionFilters().add(rmm);
			fc.getExtensionFilters().add(erm);
			File selectedFile = fc.showOpenDialog(null);
			try{
				if(fc.getSelectedExtensionFilter() == erm){
					urManager.clear();
					dataFile = selectedFile;
					readERMFile(dataFile);
				}
				if(fc.getSelectedExtensionFilter() == rmm){
					urManager.clear();
					dataFile = selectedFile;
					readRMMFile(dataFile);
				}
			}catch(IOException e){
				Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("エラーが発生しました。ファイルを読み込めません。");
				alert.showAndWait();
				dataFile = null;
			}catch(Exception e){
				e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("エラーが発生しました。データファイルに不備があります。\n"
						+ "以下のエラーメッセージを@himeshi_hobにお知らせください。\n" + e.getLocalizedMessage());
				alert.showAndWait();
				dataFile = null;
			}
		});
		mb_save.setOnAction((ActionEvent) ->{
			if(dataFile == null){
				FileChooser fc = new FileChooser();
				fc.setTitle("ファイルの保存");
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("路線図メーカー形式（*.rmm）", "*.rmm"));
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("路線図メーカーテキスト形式（*.erm）", "*.erm"));
				dataFile = fc.showSaveDialog(null);
			}
			if(dataFile != null){
				try{
					saveRMMFile(dataFile);
					Alert alert = new Alert(AlertType.INFORMATION, "保存しました。\n\n※このダイアログはenterキーで閉じます", ButtonType.OK);
					alert.show();
				}catch(IOException e){
					Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("保存中にエラーが発生しました。");
					alert.showAndWait();
					dataFile = null;
				}
			}
		});
		mb_saveAs.setOnAction((ActionEvent) ->{
			FileChooser fc = new FileChooser();
			fc.setTitle("ファイルの保存");
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("路線図メーカー形式（*.rmm）", "*.rmm"));
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("路線図メーカーテキスト形式（*.erm）", "*.erm"));
			dataFile = fc.showSaveDialog(null);
			if(dataFile != null){
				try{
					saveRMMFile(dataFile);
					Alert alert = new Alert(AlertType.INFORMATION, "保存しました。\n\n※このダイアログはenterキーで閉じます", ButtonType.OK);
					alert.show();
				}catch(IOException e){
					Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("保存中にエラーが発生しました。");
					alert.showAndWait();
					dataFile = null;
				}
			}
		});
		mb_exportImage.setOnAction((ActionEvent) ->{
			exportImage();
		});
		mb_about.setOnAction((ActionEvent) ->{
			Alert alert = new Alert(AlertType.NONE,"",ButtonType.CLOSE);
			alert.getDialogPane().setHeaderText("バージョン情報");
			alert.getDialogPane().setContentText("version "+ ReleaseVersion +"　Release：2019年12月9日\n"
					+ "使い方の参照、不具合報告等はwikiで行うことができます。\n"
					+ "不具合を発見された際はwikiもしくはTwitterでの報告にご協力をお願いします。\n\n"
					+ "お問い合わせ：@himeshi_hob（Twitter）\n2017-2019 ひめし \nCreativeCommons 表示-非営利4.0国際ライセンスに従います。"
					+ "ライセンスについては付属のReadMeを参照してください。");
			alert.show();
		});
		mb_goWiki.setOnAction((ActionEvent) ->{
			Desktop desktop = Desktop.getDesktop();
			URI uu;
			try {
				uu = new URI("http://wikiwiki.jp/routemapmake/");
				desktop.browse(uu);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		mb_checkUpdate.setOnAction((ActionEvent) ->{
			boolean b = checkUpdate(false);
			if(! b){
				Alert alert = new Alert(AlertType.INFORMATION,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("このバージョンは最新版です。");
				alert.show();
			}
			
		});
		mb_config.setOnAction((ActionEvent) ->{
			if(! configWindowOpened){
				ConfigUIController configEuc = null;
				FXMLLoader editLoader = null;
				configStage = new Stage();
				VBox ap = null;
				try {
					editLoader = new FXMLLoader(getClass().getResource("ConfigUIController.fxml"));
					ap= (VBox)editLoader.load();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				configEuc = (ConfigUIController)editLoader.getController();
				configEuc.setObject(configStage, config, this);
				Scene sc = new Scene(ap);
				configStage.setScene(sc);
				configStage.setTitle("環境設定");
				configWindowOpened = true;
				configStage.showAndWait();
				configWindowOpened = false;
			}else{
				configStage.toFront();
			}
		});
		mb_changeAll.setOnAction((ActionEvent) ->{
			if(! changeAllWindowOpened){
				ChangeAllController caEuc = null;
				FXMLLoader editLoader = null;
				changeAllStage = new Stage();
				VBox ap = null;
				try {
					editLoader = new FXMLLoader(getClass().getResource("ChangeAllController.fxml"));
					ap= (VBox)editLoader.load();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				caEuc = editLoader.getController();
				caEuc.setObject(lineList, customMarks, lineDashes, this);
				Scene sc = new Scene(ap);
				changeAllStage.setScene(sc);
				changeAllStage.setTitle("パラメーター 一括変更");
				changeAllWindowOpened = true;
				changeAllStage.showAndWait();
				changeAllWindowOpened = false;
			}else{
				changeAllStage.toFront();
			}
		});
		mb_editCustomMark.setOnAction((ActionEvent) ->{
			CustomMarkController euc = null;
			FXMLLoader editLoader = null;
			Stage editStage = new Stage();
			editStage.initModality(Modality.APPLICATION_MODAL);
			VBox ap = null;
			try {
				editLoader = new FXMLLoader(getClass().getResource("CustomMarkController.fxml"));
				ap= (VBox)editLoader.load();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			euc = (CustomMarkController)editLoader.getController();
			euc.setObject(editStage, customMarks);
			Scene sc = new Scene(ap);
			editStage.setScene(sc);
			editStage.setTitle("カスタム停車マークの編集");
			editStage.showAndWait();
			setMarkList();//マークリストを更新
		});
		mb_setCustomMark.setOnAction((ActionEvent) ->{
			SetMarkController euc = null;
			FXMLLoader editLoader = null;
			Stage editStage = new Stage();
			editStage.initModality(Modality.APPLICATION_MODAL);
			VBox ap = null;
			try {
				editLoader = new FXMLLoader(getClass().getResource("SetMarkController.fxml"));
				ap= (VBox)editLoader.load();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			euc = (SetMarkController)editLoader.getController();
			euc.setObject(lineList, customMarks);
			Scene sc = new Scene(ap);
			editStage.setScene(sc);
			editStage.setTitle("停車マークの一括設定");
			editStage.showAndWait();
			//運転経路編集モードなら再描画
			if(esGroup.getSelectedToggle() == leftEditButton) mapDraw();
		});
		fic = new FreeItemsController(freeItems, this);//コントローラーの初期化
		mb_freeItem.setOnAction((ActionEvent e) ->{
			//ショートカットキーを使って起動するとウィンドウを閉じてももう一度開く問題がある。
			if(fiWindowOpened){
				fiStage.toFront();
			}else{
				FXMLLoader editLoader = null;
				fiStage = new Stage();
				fiStage.initModality(Modality.NONE);
				VBox ap = null;
				try {
					editLoader = new FXMLLoader(getClass().getResource("FreeItemsController.fxml"));
					editLoader.setController(fic);
					ap= (VBox)editLoader.load();
					fic.initialize(location, resources);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Scene sc = new Scene(ap);
				fiStage.setScene(sc);
				fiStage.setTitle("テキスト・画像の挿入");
				fiWindowOpened = true;
				fiStage.showAndWait();
				fiWindowOpened = false;
			}
		});
		mb_lineDashes.setOnAction((ActionEvent) ->{
			LineDashesController euc = null;
			FXMLLoader editLoader = null;
			Stage editStage = new Stage();
			editStage.initModality(Modality.APPLICATION_MODAL);
			VBox ap = null;
			try {
				editLoader = new FXMLLoader(getClass().getResource("LineDashesController.fxml"));
				ap= (VBox)editLoader.load();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			euc = editLoader.getController();
			euc.setObject(lineDashes);
			Scene sc = new Scene(ap);
			editStage.setScene(sc);
			editStage.setTitle("破線パターンの編集");
			editStage.showAndWait();
			//運転経路編集モードなら再描画
			if(esGroup.getSelectedToggle() == leftEditButton) mapDraw();
		});
		mb_transform.setOnAction((ActionEvent) ->{
			TransformController euc = null;
			FXMLLoader editLoader = null;
			Stage editStage = new Stage();
			editStage.initModality(Modality.APPLICATION_MODAL);
			VBox ap = null;
			try {
				editLoader = new FXMLLoader(getClass().getResource("TransformController.fxml"));
				ap= (VBox)editLoader.load();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			euc = editLoader.getController();
			euc.setObject(canvasOriginal, editStage, this, lineList, freeItems, urManager);
			Scene sc = new Scene(ap);
			editStage.setScene(sc);
			editStage.setTitle("座標変換");
			editStage.showAndWait();
		});
		mb_undo.setOnAction((ActionEvent) ->{
			urManager.undo();
			resetParams();
			ReDraw();
		});
		mb_redo.setOnAction((ActionEvent) ->{
			urManager.redo();
			resetParams();
			ReDraw();
		});
		mb_R.setOnAction((ActionEvent) ->{
			rightEditButton.setSelected(true);
		});
		mb_T.setOnAction((ActionEvent) ->{
			leftEditButton.setSelected(true);
		});
		urManager.getUndoableProperty().addListener((observable, oldValue, newValue) -> {
			mb_undo.setDisable(! urManager.getUndoableProperty().get());
		});
		urManager.getRedoableProperty().addListener((observable, oldValue, newValue) -> {
			mb_redo.setDisable(! urManager.getRedoableProperty().get());
		});
		//以下、運転経路項目
		TrainTable.setItems(trList);
		TrainTable.setCellFactory(TextFieldListCell.forListView());
		TrainTable.setEditable(true);
		//メニューバー横にある２つのトグルボタンについて。編集領域を切り替える。
		rightPane.setDisable(false);//初期状態は右側だけ編集可能
		leftPane.setDisable(true);
		esGroup = new ToggleGroup();
		rightEditButton.setToggleGroup(esGroup);
		rightEditButton.setSelected(true);
		leftEditButton.setToggleGroup(esGroup);
		esGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle old_toggle,
				Toggle new_toggle) ->{
					if(esGroup.getSelectedToggle() == rightEditButton){
						rightPane.setDisable(false);
						leftPane.setDisable(true);
						ZoomSlider.setDisable(true);
						selectSomething(true);
						lineDraw();
					}else if(esGroup.getSelectedToggle() == leftEditButton){
						rightPane.setDisable(true);
						leftPane.setDisable(false);
						ZoomSlider.setDisable(false);
						zoom = 1;
						ZoomSlider.setValue(0);
						selectSomething(false);
						mapDraw();
					}
				});
		TrainAdd.setOnAction((ActionEvent) ->{
			int index = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(index != -1){
				Train newTrain = new Train("系統" + (lineList.get(index).getTrains().size() + 1));
				urManager.push(lineList.get(index).getTrains(), URElements.ArrayCommands.ADD,
						lineList.get(index).getTrains().size(), newTrain);
				lineList.get(index).getTrains().add(newTrain);
				int indexT = lineList.get(index).getTrains().size() - 1;
				if(lineList.get(index).getTrains().size() > 1){//先例があればそれに従って自動補完を行う
					lineList.get(index).getTrains().get(indexT).setLineDistance(lineList.get(index).getTrains().get(indexT-1)
							.getLineDistance() + lineList.get(index).getTrains().get(indexT-1).getLineWidth());
					lineList.get(index).getTrains().get(indexT).setLineWidth(lineList.get(index).getTrains().get(indexT-1)
							.getLineWidth());
					lineList.get(index).getTrains().get(indexT).setEdgeA(lineList.get(index).getTrains().get(indexT-1)
							.getEdgeA());
					lineList.get(index).getTrains().get(indexT).setEdgeB(lineList.get(index).getTrains().get(indexT-1)
							.getEdgeB());
					lineList.get(index).getTrains().get(indexT).setMarkColor(lineList.get(index).getTrains().get(indexT-1)
							.getMarkColor());
					lineList.get(index).getTrains().get(indexT).setMarkSize(lineList.get(index).getTrains().get(indexT-1)
							.getMarkSize());
				}
				editTrainStops(lineList.get(index), lineList.get(index).getTrains().get(indexT));
			}
			trList.clear();
			for(int i = 0; i < lineList.get(index).getTrains().size(); i++){
				trList.add(lineList.get(index).getTrains().get(i).getName());
			}
		});
		TrainDelete.setOnAction((ActionEvent) ->{
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT != -1){
				urManager.push(lineList.get(indexR).getTrains(), URElements.ArrayCommands.REMOVE, indexT, 
						lineList.get(indexR).getTrains().get(indexT));
				lineList.get(indexR).getTrains().remove(indexT);
			}
			trList.clear();
			for(int i = 0; i < lineList.get(indexR).getTrains().size(); i++){
				trList.add(lineList.get(indexR).getTrains().get(i).getName());
			}
		});
		TrainCopy.setOnAction((ActionEvent) ->{
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT != -1){
				Train copyTrain = lineList.get(indexR).getTrains().get(indexT).clone();
				urManager.push(lineList.get(indexR).getTrains(), URElements.ArrayCommands.ADD, indexT + 1, copyTrain);
				lineList.get(indexR).getTrains().add(indexT + 1, copyTrain);
				lineList.get(indexR).getTrains().get(indexT + 1).setName
				(lineList.get(indexR).getTrains().get(indexT + 1).getName() + " のコピー");
				trList.clear();
				for(int i = 0; i < lineList.get(indexR).getTrains().size(); i++){
					trList.add(lineList.get(indexR).getTrains().get(i).getName());
				}
				TrainTable.getSelectionModel().select(indexT + 1);
			}
		});
		TrainTable.setOnEditCommit(new EventHandler<ListView.EditEvent<String>>(){
			@Override
			public void handle(ListView.EditEvent<String> t){
				int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
				int indexT = t.getIndex();
				TrainTable.getItems().set(t.getIndex(), t.getNewValue());
				if(t.getNewValue().equals("")){//空白の系統名はダメです
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("系統名は空白にできません。何かしら名前をつけてください。");
					alert.showAndWait();
				}else{
					if(! lineList.get(indexR).getTrains().get(indexT).getName().equals(t.getNewValue())){
						urManager.push(lineList.get(indexR).getTrains().get(indexT).getNameProperty(),
								lineList.get(indexR).getTrains().get(indexT).getName(), t.getNewValue());
						lineList.get(indexR).getTrains().get(indexT).setName(t.getNewValue());
					}
				}
				trList.clear();
				for(int i = 0; i < lineList.get(indexR).getTrains().size(); i++){
					trList.add(lineList.get(indexR).getTrains().get(i).getName());
				}
			}
		});
		TT_UP.setOnAction((ActionEvent) ->{
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT >= 1){//indexが-1と0の時は意味を持たない
				//オブジェクト入れ替え
				urManager.push(lineList.get(indexR).getTrains(), URElements.ArrayCommands.UP, indexT, null);
				Train tt1 = lineList.get(indexR).getTrains().get(indexT);
				Train tt2 = lineList.get(indexR).getTrains().get(indexT - 1);
				lineList.get(indexR).getTrains().set(indexT, tt2);
				lineList.get(indexR).getTrains().set(indexT - 1, tt1);
				trList.clear();
				for(int i = 0; i < lineList.get(indexR).getTrains().size(); i++){
					trList.add(lineList.get(indexR).getTrains().get(i).getName());
				}
				TrainTable.getSelectionModel().select(indexT - 1);
				mapDraw();
			}
		});
		TT_DOWN.setOnAction((ActionEvent) ->{
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexT != -1 && indexT != lineList.get(indexR).getTrains().size() - 1){//indexが-1と最後尾の時は意味を持たない
				//オブジェクト入れ替え
				urManager.push(lineList.get(indexR).getTrains(), URElements.ArrayCommands.DOWN, indexT, null);
				Train tt1 = lineList.get(indexR).getTrains().get(indexT);
				Train tt2 = lineList.get(indexR).getTrains().get(indexT + 1);
				lineList.get(indexR).getTrains().set(indexT, tt2);
				lineList.get(indexR).getTrains().set(indexT + 1, tt1);
				trList.clear();
				for(int i = 0; i < lineList.get(indexR).getTrains().size(); i++){
					trList.add(lineList.get(indexR).getTrains().get(i).getName());
				}
				TrainTable.getSelectionModel().select(indexT + 1);
				mapDraw();
			}
		});
		
		tStaList.setItems(tStaListOb);
		TrainTable.getSelectionModel().selectedItemProperty().addListener((ChangeListener) (observable, oldValue, newValue) -> {
			// TODO Auto-generated method stub
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT != -1){
				tStaListOb.clear();
				for(int i = 0; i < lineList.get(indexR).getTrains().get(indexT).getStops().size(); i++){
					tStaListOb.add(lineList.get(indexR).getTrains().get(indexT).getStops().get(i).getSta().getName());
				}
				re_line_CP.setValue(lineList.get(indexR).getTrains().get(indexT).getLineColor());
				re_lineC_SP.getValueFactory().setValue(lineList.get(indexR).getTrains().get(indexT).getLineDistance());
				re_line_SP.getValueFactory().setValue(lineList.get(indexR).getTrains().get(indexT).getLineWidth());
				re_lineSA_SP.getValueFactory().setValue(lineList.get(indexR).getTrains().get(indexT).getEdgeA());
				re_lineSB_SP.getValueFactory().setValue(lineList.get(indexR).getTrains().get(indexT).getEdgeB());
				re_mark_CP.setValue(lineList.get(indexR).getTrains().get(indexT).getMarkColor());
				re_mark_SP.getValueFactory().setValue(lineList.get(indexR).getTrains().get(indexT).getMarkSize());
				re_mark_CB.setValue(lineList.get(indexR).getTrains().get(indexT).getMark());
				re_linePattern_CB.setValue(lineList.get(indexR).getTrains().get(indexT).getLineDash());
				selectSomething(false);
			}
		});
		tStaEdit.setOnAction((ActionEvent) ->{
			//ウィンドウがポップアップして編集する。
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT != -1){
				editTrainStops(lineList.get(indexR), lineList.get(indexR).getTrains().get(indexT));
			}
		});
		//色パレットの初期設定
		re_bg_CP.setOnAction((ActionEvent) ->{
			if(! bgColor.get().equals(re_bg_CP.getValue())) urManager.push(bgColor, bgColor.get(), re_bg_CP.getValue());
			bgColor.set(re_bg_CP.getValue());
			mapDraw();
		});
		re_line_CP.setOnAction((ActionEvent) ->{
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT != - 1){
				urManager.push(lineList.get(indexR).getTrains().get(indexT).getLineColorProperty(), 
						lineList.get(indexR).getTrains().get(indexT).getLineColor(), re_line_CP.getValue());
				lineList.get(indexR).getTrains().get(indexT).setLineColor(re_line_CP.getValue());
			}
			mapDraw();
		});
		re_mark_CP.setOnAction((ActionEvent) ->{
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT != - 1){
				urManager.push(lineList.get(indexR).getTrains().get(indexT).getMarkColorProperty(),
						lineList.get(indexR).getTrains().get(indexT).getMarkColor(), re_mark_CP.getValue());
				lineList.get(indexR).getTrains().get(indexT).setMarkColor(re_mark_CP.getValue());
			}
			mapDraw();
		});
		re_line_SP.setEditable(true);
		re_line_SP.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,Integer.MAX_VALUE,10,1));
		re_lineC_SP.setEditable(true);
		re_lineC_SP.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1));
		re_lineSA_SP.setEditable(true);
		re_lineSA_SP.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1));
		re_lineSB_SP.setEditable(true);
		re_lineSB_SP.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1));
		re_mark_SP.setEditable(true);
		re_mark_SP.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,Integer.MAX_VALUE,8,1));
		re_line_SP.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT != - 1){
				if(oldVal.intValue() == lineList.get(indexR).getTrains().get(indexT).getLineWidth())
					urManager.push(lineList.get(indexR).getTrains().get(indexT).getLineWidthProperty(), oldVal, newVal);
				lineList.get(indexR).getTrains().get(indexT).setLineWidth(re_line_SP.getValue());
				mapDraw();
			}
		});
		re_lineC_SP.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT != - 1){
				if(oldVal.intValue() == lineList.get(indexR).getTrains().get(indexT).getLineDistance())
					urManager.push(lineList.get(indexR).getTrains().get(indexT).getLineDistanceProperty(), oldVal, newVal);
				lineList.get(indexR).getTrains().get(indexT).setLineDistance(re_lineC_SP.getValue());
				mapDraw();
			}
		});
		re_lineSA_SP.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT != - 1){
				if(lineList.get(indexR).getTrains().get(indexT).getEdgeA() == oldVal.intValue())
					urManager.push(lineList.get(indexR).getTrains().get(indexT).getEdgeAProperty(), oldVal, newVal);
				lineList.get(indexR).getTrains().get(indexT).setEdgeA(re_lineSA_SP.getValue());
				mapDraw();
			}
		});
		re_lineSB_SP.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT != - 1){
				if(oldVal.intValue() == lineList.get(indexR).getTrains().get(indexT).getEdgeB())
					urManager.push(lineList.get(indexR).getTrains().get(indexT).getEdgeBProperty(), oldVal, newVal);
				lineList.get(indexR).getTrains().get(indexT).setEdgeB(re_lineSB_SP.getValue());
				mapDraw();
			}
		});
		re_mark_SP.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexT != - 1){
				if(oldVal.intValue() == lineList.get(indexR).getTrains().get(indexT).getMarkSize())
					urManager.push(lineList.get(indexR).getTrains().get(indexT).getMarkSizeProperty(), oldVal, newVal);
				lineList.get(indexR).getTrains().get(indexT).setMarkSize(re_mark_SP.getValue());
				mapDraw();
			}
		});
		trainMarkList.clear();
		for(int i = 0; i < StopMark.DefaultMarks.length; i++){
			trainMarkList.add(StopMark.DefaultMarks[i]);
		}
		re_mark_CB.setItems(trainMarkList);
		StopMarkCell tcellFactory = new StopMarkCell();
		re_mark_CB.setCellFactory(tcellFactory);
		re_mark_CB.setButtonCell(tcellFactory.call(null));
		re_mark_CB.getSelectionModel().select(1);//CIRCLEをデフォルトにする。
		re_mark_CB.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexK != -1 && indexR != -1){
				if(re_mark_CB.getValue() != null){//setMarkList()でこのComboboxの中身が変えられた時にvalueがnullの状態でリスナーがコールされる
					if(lineList.get(indexR).getTrains().get(indexK).getMark() == oldVal)
						urManager.push(lineList.get(indexR).getTrains().get(indexK), oldVal, newVal);
					lineList.get(indexR).getTrains().get(indexK).setMark(re_mark_CB.getValue());
					mapDraw();
				}
			}
		});
		initializeLineDashes();
		re_linePattern_CB.setItems(lineDashes);
		LineDashCell ldCell = new LineDashCell();
		re_linePattern_CB.setCellFactory(ldCell);
		re_linePattern_CB.setButtonCell(ldCell.call(null));
		re_linePattern_CB.getSelectionModel().select(0);
		re_linePattern_CB.valueProperty().addListener((obs, oldVal, newVal) -> {
			if(isLoading) {
				return;
			}
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexK != -1 && indexR != -1){
				if(oldVal == lineList.get(indexR).getTrains().get(indexK).getLineDash())
					urManager.push(lineList.get(indexR).getTrains().get(indexK), oldVal, newVal);
				lineList.get(indexR).getTrains().get(indexK).setLineDash(newVal);
			}
			mapDraw();
		});
		R_RouteTable.setItems(rnList);
		R_RouteTable.getSelectionModel().selectedItemProperty().addListener((ChangeListener) (observable, oldValue, newValue) -> {
			int index = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(index != -1){
				trList.clear();
				for(int i = 0; i < lineList.get(index).getTrains().size(); i++){
					trList.add(lineList.get(index).getTrains().get(i).getName());
				}
				R_nameX.getValueFactory().setValue(lineList.get(index).getNameZure()[0]);
				R_nameY.getValueFactory().setValue(lineList.get(index).getNameZure()[1]);
				selectSomething(false);
			}
		});
		RRT_UP.setOnAction((ActionEvent) ->{
			int index = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(index > 0){//0と-1は意味を持たない
				Line l1 = lineList.get(index);
				Line l2 = lineList.get(index - 1);
				lineList.set(index, l2);
				lineList.set(index - 1, l1);
				urManager.push(lineList, URElements.ArrayCommands.UP, index, null);
				rnList.clear();
				for(int i=0; i < lineList.size(); i++){
					rnList.add(lineList.get(i).getName());
				}
				R_RouteTable.getSelectionModel().select(index - 1);
				mapDraw();
			}
		});
		RRT_DOWN.setOnAction((ActionEvent) ->{
			int index = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(index != -1 && index != lineList.size() - 1){//0とラストは意味を持たない
				Line l1 = lineList.get(index);
				Line l2 = lineList.get(index + 1);
				lineList.set(index, l2);
				lineList.set(index + 1, l1);
				urManager.push(lineList, URElements.ArrayCommands.DOWN, index, null);
				rnList.clear();
				for(int i=0; i < lineList.size(); i++){
					rnList.add(lineList.get(i).getName());
				}
				R_RouteTable.getSelectionModel().select(index + 1);
				mapDraw();
			}
		});
		R_nameX.setEditable(true);
		R_nameX.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1));
		R_nameX.valueProperty().addListener((obs, oldVal, newVal) -> {
			int index = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(index != -1){
				if(oldVal == lineList.get(index).getNameZure()[0])
					urManager.push(lineList.get(index).getNameXProperty(), oldVal, newVal);
				lineList.get(index).setNameX(R_nameX.getValue());
				mapDraw();
			}
		});
		R_nameY.setEditable(true);
		R_nameY.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1));
		R_nameY.valueProperty().addListener((obs, oldVal, newVal) -> {
			int index = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(index != -1){
				if(oldVal == lineList.get(index).getNameZure()[1])
					urManager.push(lineList.get(index).getNameYProperty(), oldVal, newVal);
				lineList.get(index).setNameY(R_nameY.getValue());
				mapDraw();
			}
		});
		tStaList.getSelectionModel().selectedItemProperty().addListener((ChangeListener) (observable, oldValue, newValue) -> {
			int indexS = tStaList.getSelectionModel().getSelectedIndex();
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexR != -1 && indexK != -1 && indexS != -1){
				if(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getMuki() == Station.TEXT_TATE_BOTTOM) 
					re_staPMuki_CB.getSelectionModel().select("縦-下付き");
				if(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getMuki() == Station.TEXT_TATE_TOP) 
					re_staPMuki_CB.getSelectionModel().select("縦-上付き");
				if(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getMuki() == Station.TEXT_YOKO_LEFT) 
					re_staPMuki_CB.getSelectionModel().select("横-左付き");
				if(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getMuki() == Station.TEXT_YOKO_RIGHT) 
					re_staPMuki_CB.getSelectionModel().select("横-右付き");
				if(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getMuki() == Station.TEXT_UNSET) 
					re_staPMuki_CB.getSelectionModel().select("路線準拠");
				re_staPSize_SP.getValueFactory().setValue(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getNameSize());
				re_staPStyle_CB.getSelectionModel().select(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getNameStyle());
				re_staPShift_TB.setSelected(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().shiftBasedOnStation());
				re_staPX_SP.getValueFactory().setValue(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getNameZure()[0]);
				re_staPY_SP.getValueFactory().setValue(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getNameZure()[1]);
				re_staLAX_SP.getValueFactory().setValue(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getShift()[0]);
				re_staLAY_SP.getValueFactory().setValue(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getShift()[1]);
				re_staMark_CB.setValue(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getMark());
			}
		});
		ObservableList<String> re_staPMuki_CB_Options = FXCollections.observableArrayList("縦-下付き","縦-上付き",
				"横-右付き","横-左付き", "路線準拠");
		re_staPMuki_CB.setItems(re_staPMuki_CB_Options);
		re_staPMuki_CB.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexS = tStaList.getSelectionModel().getSelectedIndex();
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexS != -1 && indexK != -1 && indexR != -1){
				int oldT = -1;
				int newT = -1;
				if(oldVal != null){
					if(oldVal.equals("縦-下付き")) oldT = Station.TEXT_TATE_BOTTOM;
					if(oldVal.equals("縦-上付き")) oldT = Station.TEXT_TATE_TOP;
					if(oldVal.equals("横-左付き")) oldT = Station.TEXT_YOKO_LEFT;
					if(oldVal.equals("横-右付き")) oldT = Station.TEXT_YOKO_RIGHT;
					if(oldVal.equals("路線準拠")) oldT = Station.TEXT_UNSET;
				}
				if(newVal.equals("縦-下付き")) newT = Station.TEXT_TATE_BOTTOM;
				if(newVal.equals("縦-上付き")) newT = Station.TEXT_TATE_TOP;
				if(newVal.equals("横-左付き")) newT = Station.TEXT_YOKO_LEFT;
				if(newVal.equals("横-右付き")) newT = Station.TEXT_YOKO_RIGHT;
				if(newVal.equals("路線準拠")) newT = Station.TEXT_UNSET;
				if(oldT == lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getMuki())
					urManager.push(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getMukiProperty(),
							oldT, newT);
				lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().setMuki(newT);
			}
			mapDraw();
		});
		re_staPSize_SP.setEditable(true);
		re_staPSize_SP.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE, 0, 1));
		re_staPSize_SP.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexS = tStaList.getSelectionModel().getSelectedIndex();
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexS != -1 && indexK != -1){
				if(oldVal.intValue() == lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getNameSize())
					urManager.push(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getNameSizeProperty(),
							oldVal, newVal);
				lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().setNameSize(re_staPSize_SP.getValue());
				mapDraw();
			}
		});
		re_staPStyle_CB.setItems(staStyle_Options);
		re_staPStyle_CB.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexS = tStaList.getSelectionModel().getSelectedIndex();
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexS != -1 && indexK != -1){
				int oldT = -1;
				int newT = re_staPStyle_CB.getSelectionModel().getSelectedIndex();
				//"Regular", "Italic", "Bold", "BoldItalic", "路線準拠"
				if(oldVal != null){
					if(oldVal.equals("Regular")) oldT = Station.REGULAR;
					if(oldVal.equals("Italic")) oldT = Station.ITALIC;
					if(oldVal.equals("Bold")) oldT = Station.BOLD;
					if(oldVal.equals("BoldItalic")) oldT = Station.BOLD_ITALIC;
					if(oldVal.equals("路線準拠")) oldT = Station.STYLE_UNSET;
				}
				if(oldT == lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getNameStyle()
						&& newT != -1)
					urManager.push(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().getNameStyleProperty(),
							oldT, newT);
				lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta().
				setNameStyle(re_staPStyle_CB.getSelectionModel().getSelectedIndex());
				mapDraw();
			}
		});
		ToggleGroup re_staPShift_TB_TG = new ToggleGroup();
		re_staPShift_TB.setToggleGroup(re_staPShift_TB_TG);
		re_staPShift_TB_TG.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
			int indexS = tStaList.getSelectionModel().getSelectedIndex();
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexS != -1 && indexK != -1){
				Station sta = lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta();
				if(sta.shiftBasedOnStation() != re_staPShift_TB.isSelected())
					urManager.push(sta.getShiftOnStationProperty(), re_staPShift_TB.isSelected());
				sta.setShiftBase(re_staPShift_TB.isSelected());
				mapDraw();
			}
		});
		re_staPX_SP.setEditable(true);
		re_staPX_SP.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1));
		re_staPX_SP.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexS = tStaList.getSelectionModel().getSelectedIndex();
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexS != -1 && indexK != -1){
				Station sta = lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta();
				if(oldVal == sta.getNameZure()[0])
					urManager.push(sta.getNameXProperty(), oldVal, newVal);
				sta.setNameX(re_staPX_SP.getValue());
				mapDraw();
			}
		});
		re_staPY_SP.setEditable(true);
		re_staPY_SP.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1));
		re_staPY_SP.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexS = tStaList.getSelectionModel().getSelectedIndex();
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexS != -1 && indexK != -1){
				Station sta = lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getSta();
				if(oldVal == sta.getNameZure()[1])
					urManager.push(sta.getNameYProperty(), oldVal, newVal);
				sta.setNameY(re_staPY_SP.getValue());
				mapDraw();
			}
		});
		re_staLAX_SP.setEditable(true);
		re_staLAX_SP.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1));
		re_staLAX_SP.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexS = tStaList.getSelectionModel().getSelectedIndex();
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexS != -1 && indexK != -1 && indexR != -1){
				TrainStop stop = lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS);
				if(oldVal == stop.getShift()[0])
					urManager.push(stop.getShiftXProperty(), oldVal, newVal);
				stop.setShiftX(re_staLAX_SP.getValue());
				mapDraw();
			}
		});
		re_staLAY_SP.setEditable(true);
		re_staLAY_SP.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1));
		re_staLAY_SP.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexS = tStaList.getSelectionModel().getSelectedIndex();
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexS != -1 && indexK != -1 && indexR != -1){
				TrainStop stop = lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS);
				if(oldVal == stop.getShift()[1])
					urManager.push(stop.getShiftYProperty(), oldVal, newVal);
				stop.setShiftY(re_staLAY_SP.getValue());
				mapDraw();
			}
		});
		/*
		 * re_staMark_CBについて
		 * StopMark.DefaultMarksを先にインポートしてからオリジナル定義のやつを読み込んでいく。
		 */
		//markListの初期化。先にDefaultMarksを追加する。
		markList.clear();
		markList.add(StopMark.OBEY_LINE);//駅ごとの設定なのでOBEY_LINEを入れておく。
		for(int i = 0; i < StopMark.DefaultMarks.length; i++){
			markList.add(StopMark.DefaultMarks[i]);
		}
		re_staMark_CB.setItems(markList);
		StopMarkCell cellFactory = new StopMarkCell();
		re_staMark_CB.setCellFactory(cellFactory);
		re_staMark_CB.setButtonCell(cellFactory.call(null));
		re_staMark_CB.getSelectionModel().select(0);//路線準拠をデフォルトにする。
		re_staMark_CB.valueProperty().addListener((obs, oldVal, newVal) -> {
			int indexS = tStaList.getSelectionModel().getSelectedIndex();
			int indexK = TrainTable.getSelectionModel().getSelectedIndex();
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexS != -1 && indexK != -1 && indexR != -1){
				if(re_staMark_CB.getValue() != null){//setMarkList()でこのComboboxの中身が変えられた時にvalueがnullの状態でリスナーがコールされる
					if(oldVal == lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).getMark())
						urManager.push(lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS), oldVal, newVal);
					lineList.get(indexR).getTrains().get(indexK).getStops().get(indexS).setMark(re_staMark_CB.getValue());
					mapDraw();
				}
			}
		});
		
		ZoomSlider.setMin(-3);
		ZoomSlider.setMax(3);
		ZoomSlider.setValue(0);
		ZoomSlider.setShowTickLabels(true);
		ZoomSlider.setShowTickMarks(true);
		ZoomSlider.setMajorTickUnit(1);
		ZoomSlider.setMinorTickCount(2);
		ZoomSlider.setSnapToTicks(true);
		ZoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
			double d = ZoomSlider.getValue();
			zoom = Math.pow(2, d);
			mapDraw();
		});
	}
	
	private Object Integer(int indexS) {
		// TODO Auto-generated method stub
		return null;
	}

	// 路線を作成し，作成されたLineを返す
	Line createNewLine(ArrayList<String> staNames) {
		Line newLine = new Line("路線" + (lineList.size()+1));
		if(lineList.size() > 0){//既に路線があった場合は入力補助として駅名色、駅名大きさ、駅名スタイルを引き継ぐ
			final Line lastLine = lineList.get(lineList.size()-1);
			newLine.setTategaki(lastLine.isTategaki());
			newLine.setNameColor(lastLine.getNameColor());
			newLine.setNameSize(lastLine.getNameSize());
			newLine.setNameStyle(lastLine.getNameStyle());
		}
		
		// staNamesが設定されている場合は駅を設定する
		if(staNames!=null) {
			// 空白などの無効な文字列を除く
			final List<String> validStaNames = staNames.stream().filter(p -> p!=null && !p.isEmpty())
					.collect(Collectors.toList());
			if(validStaNames.size() < 2) {
				// 駅名の数が足りない．
				return null;
			}
			// 駅リストを用意する
			List<Station> newLineStations = staNames.stream().map(n -> new Station(n))
					.collect(Collectors.toList());
			// 重複駅名について問い合わせる
			// dup_process = 1は問い合わせダイアログを設けるまでの応急処置
			int dup_process = 1; // 0:問い合わせ 1:すべて統合　2:すべて不統合
			for(int i=0; i<newLineStations.size(); i++) {
				final Station s = newLineStations.get(i);
				final Station dup = findStaByName(s.getName());
				if(dup==null || dup_process==2) {
					// 重複なし or すべて不統合 → そのまま
					continue;
				}
				else if(dup_process==1) {
					// すべて統合 → 置き換え
					newLineStations.set(i, dup);
				}
				else {
					// 問い合わせ
				}
			}
			newLine.setStations(FXCollections.observableList(newLineStations));
		}
		
		urManager.push(lineList, URElements.ArrayCommands.ADD, lineList.size(), newLine);
		lineList.add(newLine);
		newLinePointSet(newLine);
		rnList.add(newLine.getName());
		RouteTable.getSelectionModel().select(rnList.size() - 1);
		return newLine;
	}
	
	Station findStaByName(String Cname){//候補の駅名がOKかどうか調べる。trueだとアウト。
		for (Line l : lineList) {
			for (Station s : l.getStations()) {
				if(Cname.equals(s.getName())) {
					return s;
				}
			}
		}
		return null;
	}
	
	double[] getGridedPoint(double org_x, double org_y) {
		double[] pos = {org_x, org_y};
		if(!config.getR_grid()) {
			// グリッド非表示．グリッド補正の必要なし
			return pos;
		}
		
		int interval = config.getR_gridInterval();
		if(config.isGridTriangle()) {
			// 三角形グリッド．XとY個別の固定はサポートしない．
			if(config.getR_bindToGridX()) {
				//まずy座標を確定させる
				double y_interval = interval * Math.sqrt(3) / 2;
				int idx = (int) (Math.round(org_y / y_interval));
				pos[1] = idx * y_interval;
				//つづいてx座標を計算する．idxが偶数か奇数かで半interval分ずれる
				double offset = (idx%2==1 ? interval/2.0 : 0);
				pos[0] = Math.round((org_x - offset) / interval) * interval + offset;
			}
		} else {
			//四角形グリッド
			if(config.getR_bindToGridX()){//グリッドにバインドする設定だった場合は座標の補正を行う。
				pos[0] = Math.round(org_x / interval) * interval;
			}
			if(config.getR_bindToGridY()){
				pos[1] = Math.round(org_y / interval) * interval;
			}
		}
		return pos;
	}
	
	void newLinePointSet(Line l){//新しく追加された路線のとりあえずの描画位置を決める。
		final double start = 50;
		final double x_interval = 200;
		final double y_interval = 50;
		l.getStations().get(0).setPoint(start, y_largest + y_interval);//スタート地点
		l.getStations().get(l.getStations().size() - 1).setPoint(start + x_interval, y_largest + y_interval);
		y_largest = y_largest + y_interval;
		canvasOriginal[0] = x_largest + canvasMargin * 2;//最初だけ余分に取っておいたほうがいいっぽい
		canvasOriginal[1] = y_largest + canvasMargin * 2;
	}
	
	void selectSomething(boolean b){//編集画面で何も選択されていない状態を避けるメソッド。trueを渡せば路線編集モード、falseで系統編集モード
		if(b){//路線編集モード
			int indexR = RouteTable.getSelectionModel().getSelectedIndex();
			if(indexR == -1){
				if(lineList.size() != 0){
					RouteTable.getSelectionModel().select(0);
					indexR = RouteTable.getSelectionModel().getSelectedIndex();
					StationList.setItems(snList);
					snList.clear();
					for(int i=0; i < lineList.get(indexR).getStations().size(); i++){
						snList.add(lineList.get(indexR).getStations().get(i).getName());
					}
				}
			}
			int indexS = StationList.getSelectionModel().getSelectedIndex();
			if(indexS == -1 && lineList.size() != 0){
				if(lineList.get(indexR).getStations().size() != 0){
					StationList.getSelectionModel().select(0);
					indexS = StationList.getSelectionModel().getSelectedIndex();
				}
			}
			StationList.setEditable(true);
		}else{//系統編集モード
			int indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
			if(indexR == -1){
				if(lineList.size() != 0){
					R_RouteTable.getSelectionModel().select(0);
					indexR = R_RouteTable.getSelectionModel().getSelectedIndex();
					trList.clear();
					for(int i = 0; i < lineList.get(indexR).getTrains().size(); i++){
						trList.add(lineList.get(indexR).getTrains().get(i).getName());
					}
				}
			}
			int indexT = TrainTable.getSelectionModel().getSelectedIndex();
			if(indexT == -1 && lineList.size() != 0){
				if(lineList.get(indexR).getTrains().size() != 0){
					TrainTable.getSelectionModel().select(0);
					indexT = TrainTable.getSelectionModel().getSelectedIndex();
					tStaListOb.clear();
					for(int i = 0; i < lineList.get(indexR).getTrains().get(indexT).getStops().size(); i++){
						tStaListOb.add(lineList.get(indexR).getTrains().get(indexT).getStops().get(i).getSta().getName());
					}
				}
			}
			int indexS = tStaList.getSelectionModel().getSelectedIndex();
			if(indexS == -1 && lineList.size() != 0 && lineList.get(indexR).getTrains().size() != 0){
				if(lineList.get(indexR).getTrains().get(indexT).getStops().size() != 0){
					tStaList.getSelectionModel().select(0);
					indexS = tStaList.getSelectionModel().getSelectedIndex();
				}
			}
		}
	}
	void resetParams(){//パラメーターを更新する。
		int indexLR = RouteTable.getSelectionModel().getSelectedIndex();
		int sizeLR = rnList.size();
		int indexLS = StationList.getSelectionModel().getSelectedIndex();
		int sizeLS = snList.size();
		int indexRR = R_RouteTable.getSelectionModel().getSelectedIndex();
		int indexRT = TrainTable.getSelectionModel().getSelectedIndex();
		int sizeRT = trList.size();
		int indexRS = tStaList.getSelectionModel().getSelectedIndex();
		int sizeRS = tStaListOb.size();
		//駅名フォント設定とbgColorは駅を選択し直しても更新されないので個別に更新
		re_bg_CP.setValue(bgColor.get());
		currentFont.setText(stationFontFamily.get());
		//まずは左の枠
		rnList.clear();
		for(Line l: lineList){
			rnList.add(l.getName());
		}
		if(rnList.size() == sizeLR && indexLR != -1){
			RouteTable.getSelectionModel().select(indexLR);
			if(snList.size() == sizeLS && indexLS != -1){
				StationList.getSelectionModel().select(indexLS);
			}else{
				selectSomething(true);
			}
		}else{
			selectSomething(true);
		}
		//つづいて右枠
		if(rnList.size() == sizeLR && indexRR != -1){
			R_RouteTable.getSelectionModel().select(indexLR);
			if(trList.size() == sizeRT && indexRT != -1){
				TrainTable.getSelectionModel().select(indexRT);
				if(tStaListOb.size() == sizeRS && indexRS != -1){
					tStaList.getSelectionModel().select(indexRS);
				}else{
					selectSomething(false);
				}
			}else{
				selectSomething(false);
			}
		}else{
			selectSomething(false);
		}
	}
	void lineDraw(){
		gc.restore();
		gc.setTransform(1, 0, 0, 1, 0, 0);
		canvas.setWidth(canvasOriginal[0]);
		canvas.setHeight(canvasOriginal[1]);
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());//はじめに全領域消去
		//まずグリッドを描画する。
		drawGrid();
		//各路線ごとに描画。
		double[] startP = new double[2];//スタート座標
		double[] endP = new double[2];//エンド座標
		double radius = pointRadius;//点の半径
		gc.setFill(Color.BLACK);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(2);
		for(Line line: lineList){
			//まずは始点での処理。
			startP = line.getStations().get(0).getPoint();
			int stopIndex = 0;
			for(int i2 = 1; i2 < line.getStations().size(); i2++){
				// 座標非固定点はスキップ
				if(!line.getStations().get(i2).isSet()) {
					continue;
				}
				if(line.getCurveConnection(i2)) {
					// ベジエ曲線での接続
					
				} else {
					// 直線での接続
					endP = line.getStations().get(i2).getPoint();
					gc.strokeLine(startP[0], startP[1], endP[0], endP[1]);
					for(int i3 = stopIndex + 1; i3 <= i2; i3++){
						double[] p = new double[2];
						p[0] = startP[0] + (endP[0] - startP[0]) * (i3 - stopIndex) / (i2 - stopIndex);
						p[1] = startP[1] + (endP[1] - startP[1]) * (i3 - stopIndex) / (i2 - stopIndex);
						if(i3 != i2){
							line.getStations().get(i3).setInterPoint(p[0], p[1]);//中間座標の登録
						}
					}
					stopIndex = i2;
					startP = endP;
				}
			}
		}
		//駅の点の描画
		for(Line l: lineList){
			for(Station sta: l.getStations()){
				boolean contain = false;
				for(MvSta ms: movingStList){
					if(ms.sta == sta) contain = true;
				}
				if(contain) { // 選択中
					gc.setFill(Color.RED);
				} else if(sta.isSet()) { //座標固定されている
					gc.setFill(config.getFixedColor());
				} else { //座標固定されていない
					gc.setFill(config.getNonFixedColor());
				}
				double[] p = sta.getPointUS();
				gc.fillOval(p[0] - radius, p[1] - radius, radius * 2, radius * 2);
			}
		}
		gc.setFill(Color.BLACK);
		textDraw(true);
	}
	
	void drawGrid() {
		// グリッド表示OFF→return
		if(!config.getR_grid()) {
			return;
		}
		final int interval = config.getR_gridInterval();
		gc.setStroke(Color.LAVENDER);
		gc.setLineWidth(1);
		if(config.isGridTriangle()) {
			// 三角形グリッド
			// 水平線
			double y_interval = interval * Math.sqrt(3) / 2;
			double h = canvas.getHeight();
			for(int i = 0; i * y_interval < h; i++){//横線
				gc.strokeLine(0, i * y_interval, canvas.getWidth(), i * y_interval);
			}
			int start_idx = (int) (Math.ceil(h/interval/Math.sqrt(3)));
			// 斜め 傾き負線
			for(double x = -1 * start_idx * interval; x < canvas.getWidth(); x += interval) {
				gc.strokeLine(x, 0, x + h/Math.sqrt(3), h);
			}
			// 斜め 傾き正線
			for(double x = 0; x < canvas.getWidth() + h/Math.sqrt(3); x += interval) {
				gc.strokeLine(x - h/Math.sqrt(3), h, x , 0);
			}
		} else {
			// 四角形グリッド
			for(int i = 0; i < canvas.getHeight();){//横線
				gc.strokeLine(0, i, canvas.getWidth(), i);
				i = i + interval;
			}
			for(int i = 0; i < canvas.getWidth();){//縦線
				gc.strokeLine(i, 0, i, canvas.getHeight());
				i = i + interval;
			}
		}
	}
	
	void textDraw(boolean mode){//駅名描画メソッド。modeがtrueなら路線編集モード。falseなら運転経路編集モード。
		gc.setFill(Color.BLACK);
		gc.setFont(Font.getDefault());
		//先にdrawnフラグを全てfalseにする
		for(Line l: lineList){
			for(Station sta: l.getStations()){
				sta.setDrawn(false);
			}
		}
		for(int i = 0; i < lineList.size(); i++){
			for(int h = 0; h < lineList.get(i).getStations().size(); h++){
				int size;
				int style;
				int muki;
				int[] shift = new int[2];
				//先にサイズを特定しよう。
				if(lineList.get(i).getStations().get(h).getNameSize() == 0){
					size = lineList.get(i).getNameSize();
				}else{
					size = lineList.get(i).getStations().get(h).getNameSize();
				}
				//次にスタイル
				if(lineList.get(i).getStations().get(h).getNameStyle() == Station.STYLE_UNSET){
					style = lineList.get(i).getNameStyle();
				}else{
					style = lineList.get(i).getStations().get(h).getNameStyle();
				}
				//駅名位置補正を決める
				if(mode == true){//路線編集モードならshiftしない。
					shift[0] = 0;
					shift[1] = 0;
				}else{
					if(lineList.get(i).getStations().get(h).shiftBasedOnStation()){//駅の設定準拠
						shift = lineList.get(i).getStations().get(h).getNameZure();
					}else{//路線の設定準拠
						shift = lineList.get(i).getNameZure();
					}
				}
				//駅ごとに個別設定されている場合はそちらを優先
				if(size != -1 && lineList.get(i).getStations().get(h).isDrawn() == false){//サイズが-1の場合は描画しない。
					if(lineList.get(i).getStations().get(h).getMuki() == Station.TEXT_UNSET){
						if(lineList.get(i).getNameLocation() == Line.BOTTOM){
							drawTate(i,h,size,style,Line.BOTTOM,shift);
						}else if(lineList.get(i).getNameLocation() == Line.TOP){
							drawTate(i,h,size,style,Line.TOP,shift);
						}else if(lineList.get(i).getNameLocation() == Line.LEFT){
							drawYoko(i,h,size,style,Line.LEFT,shift);
						}else{
							drawYoko(i,h,size,style,Line.RIGHT,shift);
						}
					}
					if(lineList.get(i).getStations().get(h).getMuki() == Station.TEXT_TATE_BOTTOM){
						drawTate(i,h,size,style,Line.BOTTOM,shift);
					}else if(lineList.get(i).getStations().get(h).getMuki() == Station.TEXT_TATE_TOP){
						drawTate(i,h,size,style,Line.TOP,shift);
					}else if(lineList.get(i).getStations().get(h).getMuki() == Station.TEXT_YOKO_LEFT){
						drawYoko(i,h,size,style,Line.LEFT,shift);
					}else if(lineList.get(i).getStations().get(h).getMuki() == Station.TEXT_YOKO_RIGHT){
						drawYoko(i,h,size,style,Line.RIGHT,shift);
					}
					lineList.get(i).getStations().get(h).setDrawn(true);
				}
			}
		}
	}
	
	void drawYoko(int ln, int st, int size, int style, int location, int[] shift){
		double sideIntv = 5;
		double[] p;
		if(lineList.get(ln).getStations().get(st).isSet()){
			p = lineList.get(ln).getStations().get(st).getPoint();
		}else{
			p = lineList.get(ln).getStations().get(st).getInterPoint();
		}
		//文字スタイルの設定
		if(style == Line.REGULAR) gc.setFont(Font.font(stationFontFamily.get(), FontWeight.NORMAL, FontPosture.REGULAR, size));
		if(style == Line.ITALIC) gc.setFont(Font.font(stationFontFamily.get(), FontWeight.NORMAL, FontPosture.ITALIC, size));
		if(style == Line.BOLD) gc.setFont(Font.font(stationFontFamily.get(), FontWeight.BOLD, FontPosture.REGULAR, size));
		if(style == Line.ITALIC_BOLD) gc.setFont(Font.font(stationFontFamily.get(), FontWeight.BOLD, FontPosture.ITALIC, size));
		gc.setFill(lineList.get(ln).getNameColor());//色の設定
		gc.setTextBaseline(VPos.BASELINE);
		if(location == Line.LEFT){//右付き、左付きの設定
			gc.setTextAlign(TextAlignment.RIGHT);
			sideIntv = -5;
		}else{
			gc.setTextAlign(TextAlignment.LEFT);
			sideIntv = 5;
		}
		gc.fillText(lineList.get(ln).getStations().get(st).getName(), 
				p[0] + sideIntv + shift[0], p[1] + size / 2 + shift[1]);//X座標は要検証
	}
	void drawTate(int ln, int st, int size, int style, int location, int[] shift){
		double[] p;
		if(lineList.get(ln).getStations().get(st).isSet()){
			p = lineList.get(ln).getStations().get(st).getPoint();
		}else{
			p = lineList.get(ln).getStations().get(st).getInterPoint();
		}
		StringBuilder tate = new StringBuilder();
		for(int i = 0; i < lineList.get(ln).getStations().get(st).getName().length(); i++){
			tate.append(lineList.get(ln).getStations().get(st).getName().charAt(i));
			if(i != lineList.get(ln).getStations().get(st).getName().length() - 1){
				tate.append("\n");//最終文字以外は改行文字を追加する。
			}
		}
		//文字スタイルの設定
		if(style == Line.REGULAR) gc.setFont(Font.font(stationFontFamily.get(), FontWeight.NORMAL, FontPosture.REGULAR, size));
		if(style == Line.ITALIC) gc.setFont(Font.font(stationFontFamily.get(), FontWeight.NORMAL, FontPosture.ITALIC, size));
		if(style == Line.BOLD) gc.setFont(Font.font(stationFontFamily.get(), FontWeight.BOLD, FontPosture.REGULAR, size));
		if(style == Line.ITALIC_BOLD) gc.setFont(Font.font(stationFontFamily.get(), FontWeight.BOLD, FontPosture.ITALIC, size));
		gc.setFill(lineList.get(ln).getNameColor());//色の設定
		gc.setTextAlign(TextAlignment.LEFT);
		if(location == Line.TOP){//右付き、左付きの設定
			gc.setTextBaseline(VPos.BOTTOM);
		}else{
			gc.setTextBaseline(VPos.TOP);
		}
		gc.fillText(tate.toString(), p[0] - size / 2 + shift[0], p[1] + shift[1]);//Y座標の設定は要検証
	}
	
	// 線分aと線分bの（延長）交点を求める．aとbが平行で交点がない場合はa[1]を用いる．
	// a, bはそれぞれ線分の両端座標
	double[] calcIntersection(double[][] a, double[][]b) {
		double[] intr = new double[2];
		if(Math.abs(a[1][0] - a[0][0]) < EPSILON){//aが縦線。y=Constの形
			intr[0] = a[1][0];//x座標は決まりました。
			if(Math.abs(b[1][0] - b[0][0]) < EPSILON){//zhBも縦線
				intr[1] = a[1][1];
			}else{
				double tangent = (b[1][1] - b[0][1]) / (b[1][0] - b[0][0]);
				double intercept = b[0][1] - b[0][0] * tangent;
				intr[1] = tangent * intr[0] + intercept;
			}
		}else if(Math.abs(b[1][0] - b[0][0]) < EPSILON){//bが縦線。
			intr[0] = b[0][0];//x座標は決まりました。
			double tangent = (a[1][1] - a[0][1]) / (a[1][0] - a[0][0]);
			double intercept = a[0][1] - a[0][0] * tangent;
			intr[1] = tangent * intr[0] + intercept;
		}else{//両方共縦線じゃない
			double tangentA = (a[1][1] - a[0][1]) / (a[1][0] - a[0][0]);
			double tangentB = (b[1][1] - b[0][1]) / (b[1][0] - b[0][0]);
			double interceptA = a[1][1] - a[1][0] * tangentA;
			double interceptB = b[0][1] - b[0][0] * tangentB;
			if(Math.abs(tangentA - tangentB) < EPSILON){//２つの直線が一直線上にある。解が無数に存在してしまう場合
				intr = a[1].clone();//計算する意味がないのでそのまんまshiftされた値を使うだけ
			}else{
				intr[0] = (interceptB - interceptA) / (tangentB - tangentA) * -1;
				intr[1] = tangentA * intr[0] + interceptA;
			}
		}
		return intr;
	}
	
	void mapDraw(){//leftEdit状態の時はこちらが描画される。
		gc.restore();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());//はじめに全領域消去
		gc.setLineCap(StrokeLineCap.ROUND);//先っちょは丸くする。
		gc.setTransform(zoom, 0, 0, zoom, 0, 0);
		canvas.setWidth(canvasOriginal[0] * zoom);
		canvas.setHeight(canvasOriginal[1] * zoom);
		gc.setFill(bgColor.get());
		gc.fillRect(0, 0, canvasOriginal[0], canvasOriginal[1]);
		for(int k = lineList.size() - 1; 0 <= k; k--){//路線ごとに処理
			for(int i = lineList.get(k).getTrains().size() - 1; 0 <= i; i--){//系統ごとに処理。降順に処理していく。
				Train train = lineList.get(k).getTrains().get(i);
				if(train.getStops().size() >= 2){//0駅もしくは1駅しか登録されてない系統は無視
					//線の描画処理
					gc.setStroke(train.getLineColor());
					gc.setLineWidth(train.getLineWidth());
					gc.setLineDashes(train.getLineDash().get());
					gc.setFill(train.getMarkColor());
					int zure = train.getLineDistance();
					int mark_Size = train.getMarkSize();
					List<String> lineStationNames = lineList.get(k).getStations().stream()
							.map(sta -> sta.getName()).collect(Collectors.toList()); // 路線の駅名リスト
					int lastIndex = train.getStops().size() - 1;
					//路線における系統の始点の番号
					int startPoint = lineStationNames.indexOf(train.getStops().get(0).getSta().getName());
					//路線における系統の終点の番号
					int endPoint = lineStationNames.lastIndexOf(train.getStops().get(lastIndex).getSta().getName());
					int stopCount = 1;//駅ごとのライン補正は情報がTrainStopにあるので何番目のTrainStopなのかカウント
					double[] start = new double[2];
					double[] end = new double[2];
					//まずはedgeAを考えましょう。
					double[][] so = shiftPoint(lineList.get(k).getStations().get(startPoint).getPointUS(),
							lineList.get(k).getStations().get(startPoint + 1).getPointUS(), zure);
					double edgeALength = lineList.get(k).getTrains().get(i).getEdgeA();
					int[] staShift = lineList.get(k).getTrains().get(i).getStops().get(0).getShift();//スタートなのでindex0
					//so[0]にstartPointでの駅毎位置補正を加える。こうすることでShiftCoorに反映される。
					so[0][0] = so[0][0] + staShift[0];
					so[0][1] = so[0][1] + staShift[1];
					//edgeAを考慮する。
					start[0] = so[0][0] - (so[1][0] - so[0][0]) * edgeALength /
							Math.sqrt(Math.pow(so[1][0] - so[0][0], 2) + Math.pow(so[1][1] - so[0][1], 2));
					start[1] = so[0][1] - (so[1][1] - so[0][1]) * edgeALength /
							Math.sqrt(Math.pow(so[1][0] - so[0][0], 2) + Math.pow(so[1][1] - so[0][1], 2));
					double[][] ll = null;//edgeBの処理の関係でここで宣言しておきます。
					//lineList.get(k).getStations().get(startPoint).setShiftCoor(start.clone());
					for(int h = startPoint; h < endPoint; h++){//各駅ごとに繰り返し描画していく。
						if(lineList.get(k).getStations().get(h+1).isSet() == true && h != endPoint - 1){
							//この場合は歪みを防ぐため特殊な処理が必要。連立方程式を用意してその解を採用する。
							double[][] zhA = shiftPoint(lineList.get(k).getStations().get(h).getPointUS(),
									lineList.get(k).getStations().get(h+1).getPoint(), zure);
							double[][] zhB = shiftPoint(lineList.get(k).getStations().get(h+1).getPoint(),
									lineList.get(k).getStations().get(h+2).getPointUS(), zure);
							end = calcIntersection(zhA, zhB);
							//駅毎位置補正を加える。
							if(lineList.get(k).getStations().get(h+1) == train.getStops().get(stopCount).getSta()){
								staShift = train.getStops().get(stopCount).getShift();
								end[0] = end[0] + staShift[0];
								end[1] = end[1] + staShift[1];
								stopCount ++;//最後にstopcountを一つ上げる。
							}
						}else if(h == endPoint - 1){//最後のひと区間の時の処理。
							ll = shiftPoint(lineList.get(k).getStations().get(h).getPointUS(),
									lineList.get(k).getStations().get(h+1).getPointUS(), zure);
							//駅毎位置補正を加える。
							//ll[1]に補正を加える事でShiftCoorに反映される。
							staShift = lineList.get(k).getTrains().get(i).getStops().get(stopCount).getShift();
							ll[1][0] = ll[1][0] + staShift[0];
							ll[1][1] = ll[1][1] + staShift[1];
							/*
							if(lineList.get(k).getTrains().get(i).getStops().get(stopCount) == lineList.get(k).getTrains().
									get(i).getStops().get(lineList.get(k).getTrains().get(i).getStops().size() - 1)){
								//ll[1]に補正を加える事でShiftCoorに反映される。
								staShift = lineList.get(k).getTrains().get(i).getStops().get(stopCount).getShift();
								ll[1][0] = ll[1][0] + staShift[0];
								ll[1][1] = ll[1][1] + staShift[1];
							}else{
								throw new IllegalArgumentException("lineList"+k+", train"+i+"でstopCountが正常に計算されていません！");
							}
							*/
							double edgeBLength = lineList.get(k).getTrains().get(i).getEdgeB();
							end[0] = ll[1][0] + (ll[1][0] - ll[0][0]) * edgeBLength /
									Math.sqrt(Math.pow(ll[1][0] - ll[0][0], 2) + Math.pow(ll[1][1] - ll[0][1], 2));
							end[1] = ll[1][1] + (ll[1][1] - ll[0][1]) * edgeBLength /
									Math.sqrt(Math.pow(ll[1][0] - ll[0][0], 2) + Math.pow(ll[1][1] - ll[0][1], 2));
						}else{
							end = shiftPoint(lineList.get(k).getStations().get(h).getPointUS(),
									lineList.get(k).getStations().get(h+1).getPointUS(), zure)[1];
							//駅毎位置補正を加える。
							if(lineList.get(k).getStations().get(h+1) == lineList.get(k).getTrains().get(i).getStops().
									get(stopCount).getSta()){
								staShift = lineList.get(k).getTrains().get(i).getStops().get(stopCount).getShift();
								end[0] = end[0] + staShift[0];
								end[1] = end[1] + staShift[1];
								stopCount ++;//最後にstopcountを一つ上げる。
							}
						}
						gc.strokeLine(start[0], start[1], end[0], end[1]);
						lineList.get(k).getStations().get(h+1).setShiftCoor(end.clone());
						start[0] = end[0];
						start[1] = end[1];
					}
					lineList.get(k).getStations().get(startPoint).setShiftCoor(so[0]);
					lineList.get(k).getStations().get(endPoint).setShiftCoor(ll[1]);
					gc.setLineDashes(null);//破線設定の後処理
					//上書きの問題があってやはりmarkは線を書き終わってからにしよう。
					for(int h = 0; h < lineList.get(k).getTrains().get(i).getStops().size(); h++){
						//どのmarkを使うのか決める。
						StopMark mm = null;
						if(lineList.get(k).getTrains().get(i).getStops().get(h).getMark() == StopMark.OBEY_LINE){
							mm = lineList.get(k).getTrains().get(i).getMark();
						}else{
							mm = lineList.get(k).getTrains().get(i).getStops().get(h).getMark();
						}
						//以下、それぞれのマークの処理
						if(mm == StopMark.CIRCLE){
							gc.setFill(lineList.get(k).getTrains().get(i).getMarkColor());
							gc.fillOval(lineList.get(k).getTrains().get(i).getStops().get(h).getSta().getShiftCoor()[0] - mark_Size / 2,
									lineList.get(k).getTrains().get(i).getStops().get(h).getSta().getShiftCoor()[1] - mark_Size / 2, 
									mark_Size, mark_Size);
						}else if(mm == StopMark.NO_DRAW){
							//NO_DRAWなのでなにもしない。
						}else{//カスタムマーク
							CustomMarkController.markDraw(gc, mm, mark_Size,
									lineList.get(k).getTrains().get(i).getStops().get(h).getSta().getShiftCoor());
						}
					}
				}
			}
		}
		textDraw(false);//駅名はlineにもとづいて描画することになりました。
		//以下、自由挿入アイテムを描画する
		gc.setTextAlign(TextAlignment.LEFT);//駅名描画でいじったので直す
		gc.setTextBaseline(VPos.BASELINE);
		for(int c = freeItems.size() - 1; 0 <= c; c--){//下から順番に。
			FreeItem item = freeItems.get(c);
			double[] params = new double[5];
			for(int k = 0; k < 5; k++){
				params[k] = item.getParams()[k].getValue();
			}
			//アフィン変換で回転。そのまま回転だと原点中心になっちゃうので行列計算。
			//freeItemでは回転をリストアしないと前の回転がどんどん溜まっていく。zoomの再設定も必要。
			gc.restore();
			gc.setTransform(zoom, 0, 0, zoom, 0, 0);
			gc.transform(Math.cos(Math.toRadians(params[4])),Math.sin(Math.toRadians(params[4])),
					-1 * Math.sin(Math.toRadians(params[4])),Math.cos(Math.toRadians(params[4])),
					params[0] - params[0] * Math.cos(Math.toRadians(params[4])) + params[1] * Math.sin(Math.toRadians(params[4])),
					params[1] - params[0] * Math.sin(Math.toRadians(params[4])) - params[1] * Math.cos(Math.toRadians(params[4])));
			if(item.getType() == FreeItem.IMAGE) gc.drawImage(item.getImage(), params[0], params[1], params[2], params[3]);
			if(item.getType() == FreeItem.TEXT){
				//テキストの描画処理はここに実装
				//文字スタイルの設定
				if(item.getParams()[5].getValue() == 0) gc.setFont(Font.font(item.getFontName(), FontWeight.NORMAL, 
						FontPosture.REGULAR, item.getParams()[2].getValue()));
				if(item.getParams()[5].getValue() == 2) gc.setFont(Font.font(item.getFontName(), FontWeight.NORMAL, 
						FontPosture.ITALIC, item.getParams()[2].getValue()));
				if(item.getParams()[5].getValue() == 1) gc.setFont(Font.font(item.getFontName(), FontWeight.BOLD, 
						FontPosture.REGULAR, item.getParams()[2].getValue()));
				if(item.getParams()[5].getValue() == 3) gc.setFont(Font.font(item.getFontName(), FontWeight.BOLD, 
						FontPosture.ITALIC, item.getParams()[2].getValue()));
				String writes = null;//実際に出力するString
				if(item.getParams()[7].getValue() == 0) writes = item.getText();
				if(item.getParams()[7].getValue() == 1){//縦書きの場合
					StringBuilder tate = new StringBuilder();
					for(int i = 0; i < item.getText().length(); i++){
						tate.append(item.getText().charAt(i));
						if(i != item.getText().length() - 1){
							tate.append("\n");//最終文字以外は改行文字を追加する。
						}
					}
					writes = tate.toString();
				}
				if(item.getParams()[6].getValue() == 0){//fill
					gc.setFill(item.getColor());
					gc.fillText(writes, item.getParams()[0].getValue(), item.getParams()[1].getValue());
				}
				if(item.getParams()[6].getValue() == 1){//stroke
					gc.setStroke(item.getColor());
					gc.setLineWidth(item.getParams()[3].getValue());
					gc.strokeText(writes, item.getParams()[0].getValue(), item.getParams()[1].getValue());
				}
			}
		}
	}
	protected void ReDraw(){//画面を描画し直す。主に外部インスタンスから呼び出す用。
		if(esGroup.getSelectedToggle() == rightEditButton){
			lineDraw();
		}else if(esGroup.getSelectedToggle() == leftEditButton){
			mapDraw();
		}
	}
	boolean stationConnect(int stIndex, int lnIndex, boolean auto){//キャンセルされたかを返す。
		//路線indexと駅indexをもらって他に同じ駅名があるかどうかを調べる。
		boolean canceled = false;
		String gst = lineList.get(lnIndex).getStations().get(stIndex).getName();//サーチする駅名
		search: for(int i=0; i < lineList.size(); i++){
			for(int j = 0; j < lineList.get(i).getStations().size(); j++){
				if(i != lnIndex || j != stIndex){//自分自身に関しては探索しない。同じ路線内でも接続してしまうよ。
					//名前が一致した場合かつオブジェクトが異なる場合
					if(gst.equals(lineList.get(i).getStations().get(j).getName()) && 
							lineList.get(lnIndex).getStations().get(stIndex) != lineList.get(i).getStations().get(j)){
						boolean dodo = auto;//connect処理を実際にやるかやらないか
						if(! dodo){
							Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
							alert.setContentText("マップ内に同じ駅名の駅があります。その駅と統合してよろしいですか？");
							Optional<ButtonType> result = alert.showAndWait();
							if(result.get() == ButtonType.OK) dodo = true;
							if(result.get() == ButtonType.CANCEL) canceled = true;
						}
						if(dodo){
							if(lineList.get(i).getStations().get(j).isSet() == false){//座標非設置点だった場合
								double[] p = detectCoordinate(j,i);
								//接続点は座標を固定。
								lineList.get(i).getStations().get(j).setPoint(p[0], p[1]);
							}
							//駅オブジェクト自体を置き換えて共通化してしまう。
							lineList.get(lnIndex).getStations().set(stIndex, lineList.get(i).getStations().get(j));
							//lineList.get(lnIndex).getStations().get(stIndex).plusConnection();
						}
						break search;
					}
				}
			}
		}
		return canceled;
	}
	
	double[] detectCoordinate(int stIndex, int lnIndex){//座標非設定点でその駅の座標を特定するメソッド
		double[] detected = new double[2];
		if(lineList.get(lnIndex).getStations().get(stIndex).isSet()){
			return lineList.get(lnIndex).getStations().get(stIndex).getPoint();
		}else{
			double[] start = new double[2];
			double[] end = new double[2];
			int startIndex = 0;
			int endIndex = 0;
			//始点検索
			for(int i = stIndex - 1; i >= 0; i--){
				if(lineList.get(lnIndex).getStations().get(i).isSet()){
					start = lineList.get(lnIndex).getStations().get(i).getPoint();
					startIndex = i;
					break;
				}
			}
			//終点検索
			for(int i = stIndex + 1; i < lineList.get(lnIndex).getStations().size(); i++){
				if(lineList.get(lnIndex).getStations().get(i).isSet()){
					end = lineList.get(lnIndex).getStations().get(i).getPoint();
					endIndex = i;
					break;
				}
			}
			detected[0] = start[0] + (end[0] - start[0]) * (stIndex - startIndex) / (endIndex - startIndex);
			detected[1] = start[1] + (end[1] - start[1]) * (stIndex - startIndex) / (endIndex - startIndex);
			return detected;
		}
		
	}
	
	Station searchStation(double x, double y){
		for(int i = 0; i < lineList.size(); i++) {
			Line line = lineList.get(i);
			for(int j = 0; j < line.getStations().size(); j++){
				Station st = line.getStations().get(j);
				double[] p;
				if(st.isSet()){
					p = st.getPoint();
				}else{
					p = st.getInterPoint();
				}
				double dist_square = Math.pow(x-p[0], 2) + Math.pow(y-p[1], 2);
				if(dist_square <= Math.pow(6, 2)){
					RouteTable.getSelectionModel().select(i);//選択処理をする
					StationList.getSelectionModel().select(j);
					return st;
				}
			}
		}
		return null;
	}
	
	ObservableList<MvSta> searchStation(double x, double y, double w, double h){
		//ドラッグで生成された四角形の中に存在する駅をリストで返す。座標固定駅のみ。
		ObservableList<MvSta> staList = FXCollections.observableArrayList();
		for(Line l: lineList){
			for(Station sta: l.getStations()){
				if(sta.isSet()){
					double[] p = sta.getPoint();
					if(x <= p[0] && p[0] <= x + w && y <= p[1] && p[1] <= y + h){
						boolean contain = false;
						for(MvSta ms: staList){
							if(ms.sta == sta) contain = true;
						}
						if(! contain) staList.add(new MvSta(sta));//重複対策
					}
				}
			}
		}
		return staList;
	}
	
	void readERMFile(File file) throws IOException {
		// propertiesの読み込み
		InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
		Properties p = new Properties();
		p.load(isr);
		isr.close();
		// 画像の読み込み．同ディレクトリの全pngを対象にする．
		HashMap<Integer,Image> imageMap = new HashMap<Integer,Image>();
		for(File f: new File(file.getParent()).listFiles()) {
			System.out.println(f.getParent() + " -> " + f.getName());
			if(f.getName().substring(f.getName().lastIndexOf(".")).equals(".png")) {
				System.out.println("read.");
				readImage(f, imageMap);
			}
		}
		readProp(p,imageMap);
	}
	
	void readRMMFile(File file) throws IOException{
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)),
				Charset.forName("UTF-8"));
		ZipEntry entry = null;
		HashMap<Integer,Image> imageMap = new HashMap<Integer,Image>();//hashmapを使ってimageの読み込みを管理していく。
		File mainFile = null;
		Properties mainP = new Properties();
		while(( entry = zis.getNextEntry() ) != null ){
			if(entry.getName().equals("main.properties")){
				//mainの読み込み
				mainFile = new File(entry.getName());
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(mainFile));
				byte[] buf = new byte[1024];
				int size = 0;
				while((size = zis.read(buf)) > 0 ){
					os.write(buf, 0, size);
				}
				os.close();
				zis.closeEntry();
				InputStreamReader isr = new InputStreamReader(new FileInputStream(mainFile), "UTF-8");
				mainP.load(isr);
				isr.close();
			}
			if(entry.getName().contains(".png")){//画像
				File imageFile = new File(entry.getName());
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(imageFile));
				byte[] buf = new byte[1024];
				int size = 0;
				while((size = zis.read(buf)) > 0 ){
					os.write(buf, 0, size);
				}
				os.close();
				zis.closeEntry();
				readImage(imageFile, imageMap);
				imageFile.delete();
			}
		}
		//main.propertiesは全ての画像読み込みが終了してから行う。
		readProp(mainP,imageMap);
		mainFile.delete();
		zis.close();
	}
	
	//png画像を読みこんでimageMapに格納する
	void readImage(File imageFile, HashMap<Integer,Image> imageMap) throws IOException {
		try {
			int idx = Integer.valueOf(imageFile.getName().substring(0, imageFile.getName().indexOf(".")));
			Image im = SwingFXUtils.toFXImage(ImageIO.read(imageFile), null);
			imageMap.put(idx, im);
		}catch(NumberFormatException e) {
			// ファイル名が数字でない場合は読み込み処理を行わない．
		}
	}
	
	void saveRMMFile(File saveFile) throws IOException{
		final String fN = saveFile.getName();
		final boolean rmm = (fN.substring(fN.lastIndexOf(".")).equals(".rmm"));
		ArrayList<Image> images = new ArrayList<Image>();//書き出す画像を全部ここにストック。
		//画像はimagesのindex番号のみで識別する。propertiesも番号だけ。
		//mainの書き出し
		Properties mainP = new Properties();
		saveProp(mainP, images);
		try{
			// propertiesファイルの出力を文字列でソートする
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			mainP.store(pw, null);
			pw.flush();
			List<String> prop_str = Arrays.asList(sw.toString().split("\n"));
			Collections.sort(prop_str);
			// ソートした文字列をファイルに書き出し
			ArrayList<File> files = new ArrayList<File>();
			File mainF = rmm ? new File("main.properties") : saveFile; // ermの場合はsaveFileに書き込む
			BufferedWriter bw = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(mainF), "UTF-8"));
			for(String line: prop_str) {
				bw.append(line);
				bw.newLine();
			}
			files.add(mainF);
			bw.close();
			//画像の書き出し。
			String fileDir = rmm ? "" : saveFile.getParent() + "/";
			for(int i = 0; i < images.size(); i++){
				File imF = new File(fileDir + i + ".png");//番号+".png"
				ImageIO.write(SwingFXUtils.fromFXImage(images.get(i), null), "png", imF);
				files.add(imF);
			}
			if(rmm) {
				HandleZip.writeZip(saveFile, files);
				for(File f: files){//一時ファイルを消していく
					f.delete();
				}
			}
			urManager.saveUndoStackSize(); //保存カウントを更新
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			//zos.close();
		}
	}
	void readProp(Properties p, HashMap<Integer,Image> imageMap) throws IOException{//各種データをセットする。
		//Propertiesを渡す方式に変更しましたので以下の処理は呼び出し元でやってもらう。
		/*
		InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
		Properties p = new Properties();
		p.load(isr);
		isr.close();
		*/
		double pVersion = 0;
		try{
			pVersion = Double.parseDouble(p.getProperty("version"));
		}catch(NumberFormatException e){
			Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
			alert.getDialogPane().setContentText("ファイルが不正です。読み込みできません。");
			alert.showAndWait();
			return;
		}
		if(pVersion > version){
			Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
			alert.getDialogPane().setContentText("このバージョンのファイルには対応していません。読み込みできません。"
					+ "データのバージョン："+pVersion);
			alert.showAndWait();
			//return;
		}
		
		isLoading = true;
		
		double[] bgc = new double[4];
		bgc[0] = Double.parseDouble(p.getProperty("bgColorR"));
		bgc[1] = Double.parseDouble(p.getProperty("bgColorG"));
		bgc[2] = Double.parseDouble(p.getProperty("bgColorB"));
		bgc[3] = Double.parseDouble(p.getProperty("bgColorO"));
		bgColor.set(new Color(bgc[0],bgc[1],bgc[2],bgc[3]));
		stationFontFamily.set(p.getProperty("stationFont", "system"));
		//lineDashesを頂点とするデータ群
		lineDashes.clear();
		if(pVersion < 8){//バージョン8未満は初期化して終わり
			initializeLineDashes();
		}else{
			lineDashes.add(Train.NORMAL_LINE);//null値は先に入れておく。
			int numOfLineDashes = Integer.valueOf(p.getProperty("NumOfLineDashes"));
			for(int i = 1; i < numOfLineDashes; i++){//iは1から。（0はNORMAL_LINE）
				int length = Integer.valueOf(p.getProperty("LineDash" + i + "length"));
				double[] da = new double[length];
				for(int h = 0; h < length; h++){
					da[h] = Double.valueOf(p.getProperty("LineDash" + i + "." + h));
				}
				lineDashes.add(new DoubleArrayWrapper(da));
			}
		}
		//freeItemsを頂点とするデータ群
		freeItems.clear();
		if(pVersion >= 5){//freeItemはデータのバージョンが5以上のときのみ
			int numOfItems = Integer.valueOf(p.getProperty("NumOfFreeItems"));
			for(int i = 0; i < numOfItems; i++){
				int type = Integer.valueOf(p.getProperty("FreeItem" + i + ".type"));
				if(type == FreeItem.TEXT){
					FreeItem fi = new FreeItem(type);
					fi.setText(p.getProperty("FreeItem" + i + ".text"));
					for(int h = 0; h < 8; h++){
						fi.getParams()[h].set(Double.parseDouble(p.getProperty("FreeItem" + i + ".params" + h)));
					}
					fi.setColor(new Color(Double.parseDouble(p.getProperty("FreeItem" + i + ".ColorR")),
							Double.parseDouble(p.getProperty("FreeItem" + i + ".ColorG")),
							Double.parseDouble(p.getProperty("FreeItem" + i + ".ColorB")),
							Double.parseDouble(p.getProperty("FreeItem" + i + ".ColorO"))));
					fi.setFontName(p.getProperty("FreeItem" + i + ".font"));
					freeItems.add(fi);
				}
				if(type == FreeItem.IMAGE){
					int img_idx = Integer.valueOf(p.getProperty("FreeItem" + i + ".image"));
					Image img = imageMap.get(img_idx);
					if(img==null) {
						Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
						alert.getDialogPane().setContentText("画像ファイル " + img_idx + ".png が見つかりません．");
						alert.showAndWait();
						continue;
					}
					FreeItem fi = new FreeItem(type);
					fi.setText(p.getProperty("FreeItem" + i + ".text"));
					for(int h = 0; h < 5; h++){
						fi.getParams()[h].set(Double.parseDouble(p.getProperty("FreeItem" + i + ".params" + h)));
					}
					fi.setImage(img);
					freeItems.add(fi);
				}
			}
		}
		//customMarksを頂点とするデータ群（customMarksは後で使うので先に読み込んでおく。）
		customMarks.clear();
		if(pVersion >= 4){//マークの読み込み処理はデータのバージョンが4以上のときのみ。
			int numOfMarks = Integer.valueOf(p.getProperty("NumOfMarks"));
			for(int i = 0; i < numOfMarks; i++){
				customMarks.add(new StopMark());
				int numOfLayers = Integer.valueOf(p.getProperty("Mark" + i + ".NumOfLayers"));
				for(int h = 0; h < numOfLayers; h++){
					MarkLayer layer = new MarkLayer(Integer.valueOf(p.getProperty("Mark" + i + ".layer" + h + ".type")));
					layer.setPaint(Integer.valueOf(p.getProperty("Mark" + i + ".layer" + h + ".paint")));
					int numOfParams = Integer.valueOf(p.getProperty("Mark" + i + ".layer" + h + ".numOfParams"));
					for(int k = 0; k < numOfParams; k++){
						layer.addParam(Double.valueOf(p.getProperty("Mark" + i + ".layer" + h + ".param" + k)));
					}
					layer.setText(p.getProperty("Mark" + i + ".layer" + h + ".text"));
					layer.setFontName(p.getProperty("Mark" + i + ".layer" + h + ".fontName",null));
					layer.setColor(new Color(Double.valueOf(p.getProperty("Mark" + i + ".layer" + h + ".colorR")),
							Double.valueOf(p.getProperty("Mark" + i + ".layer" + h + ".colorG")),
							Double.valueOf(p.getProperty("Mark" + i + ".layer" + h + ".colorB")),
							Double.valueOf(p.getProperty("Mark" + i + ".layer" + h + ".colorO"))));
					if(layer.getType() == MarkLayer.IMAGE){
						Image im = imageMap.get(Integer.valueOf(p.getProperty("Mark" + i + ".layer" + h + ".image")));
						if(im == null){
							Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
							alert.getDialogPane().setContentText("カスタムマーク"+i+"レイヤー"+h+"は画像属性ですが画像が取得"
									+ "できませんでした。");
							alert.showAndWait();
						}else{
							layer.setImage(im);
						}
					}
					customMarks.get(i).getLayers().add(layer);
				}
			}
		}
		setMarkList();
		//lineListを頂点とするデータ群
		int numOfLines = Integer.parseInt(p.getProperty("NumOfLines"));
		lineList.clear();//lineListは全消去
		rnList.clear();
		for(int i = 0; i < numOfLines; i++){//lineの読み込み
			lineList.add(new Line(p.getProperty("line" + String.valueOf(i) + ".lineName")));
			rnList.add(lineList.get(i).getName());
			lineList.get(i).getStations().clear();//コンストラクタで生成された奴らを削除する必要がある。
			if(pVersion < 7){//上付き、下付きなど未対応のデータ
				boolean tategaki = Boolean.valueOf(p.getProperty("line" + String.valueOf(i) + ".tategaki"));
				if(tategaki) lineList.get(i).setNameLocation(Line.BOTTOM);
				if(! tategaki) lineList.get(i).setNameLocation(Line.RIGHT);
			}else{
				lineList.get(i).setNameLocation(Integer.valueOf(p.getProperty("line" + String.valueOf(i) + ".nameLocation")));
			}
			lineList.get(i).setTategaki(Boolean.valueOf(p.getProperty("line" + String.valueOf(i) + ".tategaki")));
			lineList.get(i).setNameStyle(Integer.valueOf(p.getProperty("line" + String.valueOf(i) + ".nameStyle")));
			lineList.get(i).setNameSize(Integer.valueOf(p.getProperty("line" + String.valueOf(i) + ".nameSize")));
			double[] cp = new double[4];
			cp[0] = Double.parseDouble(p.getProperty("line" + String.valueOf(i) + ".nameColorR"));
			cp[1] = Double.parseDouble(p.getProperty("line" + String.valueOf(i) + ".nameColorG"));
			cp[2] = Double.parseDouble(p.getProperty("line" + String.valueOf(i) + ".nameColorB"));
			cp[3] = Double.parseDouble(p.getProperty("line" + String.valueOf(i) + ".nameColorO"));
			lineList.get(i).setNameColor(new Color(cp[0],cp[1],cp[2],cp[3]));
			lineList.get(i).setNameX(Integer.parseInt(p.getProperty("line" + String.valueOf(i) + ".nameX")));
			lineList.get(i).setNameY(Integer.parseInt(p.getProperty("line" + String.valueOf(i) + ".nameY")));
			int numOfSta = Integer.parseInt(p.getProperty("line" + String.valueOf(i) + ".NumOfStations"));
			for(int h = 0; h < numOfSta; h++){//Stationの読み込み
				lineList.get(i).getStations().add(new Station
						(p.getProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".name")));
				double rx = Double.parseDouble(p.getProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".x"));
				double ry = Double.parseDouble(p.getProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".y"));
				if(Boolean.valueOf(p.getProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".pointSet"))){
					//pointSetがtrueのとき
					lineList.get(i).getStations().get(h).setPoint(rx, ry);
				}else{
					//falseのとき
					lineList.get(i).getStations().get(h).setInterPoint(rx, ry);
				}
				lineList.get(i).getStations().get(h).setConnection(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".stationConnection")));
				lineList.get(i).getStations().get(h).setMuki(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".textMuki")));
				lineList.get(i).getStations().get(h).setNameSize(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".size")));
				lineList.get(i).getStations().get(h).setNameStyle(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".style")));
				lineList.get(i).getStations().get(h).setNameX(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".nameX")));
				lineList.get(i).getStations().get(h).setNameY(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".nameY")));
				lineList.get(i).getStations().get(h).setShiftBase(Boolean.valueOf(p.getProperty
						("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".shiftOnStation")));
				stationConnect(h,i,true);//接続駅はオブジェクト共通化手続き
			}
			int numOfTrains = Integer.parseInt(p.getProperty("line" + String.valueOf(i) + ".NumOfTrains"));
			for(int h = 0; h < numOfTrains; h++){//Trainの読み込み
				lineList.get(i).getTrains().add(new Train(p.getProperty
						("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".name")));
				int count = 0;//停車駅の読み込みに使用する。
				System.out.println("Reading:line"+i+",train"+h);
				for(int k = 0; k < lineList.get(i).getStations().size(); k++){//路線の駅から停車駅を追加していく。
					String a = lineList.get(i).getStations().get(k).getName();
					String b = p.getProperty
							("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".sta" + String.valueOf(count));
					if(a.equals(b)){
						System.out.println("match:line-"+k+",stop-"+count);
						lineList.get(i).getTrains().get(h).getStops().add(new TrainStop(lineList.get(i).getStations().get(k)));
						lineList.get(i).getTrains().get(h).getStops().get(count).setShiftX(Integer.valueOf(p.getProperty
								("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".sta" + String.valueOf(count)
								+ ".shiftX", "0")));//記載がなかったら0を代入。
						lineList.get(i).getTrains().get(h).getStops().get(count).setShiftY(Integer.valueOf(p.getProperty
								("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".sta" + String.valueOf(count)
								+ ".shiftY", "0")));//記載がなかったら0を代入。
						//停車駅マークについて
						String ms = p.getProperty
								("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".sta" + String.valueOf(count)
								+ ".mark");
						if(ms == null){//デフォは路線準拠
							lineList.get(i).getTrains().get(h).getStops().get(count).setMark(StopMark.OBEY_LINE);
						}else if(ms.equals("OBEY_LINE")){
							lineList.get(i).getTrains().get(h).getStops().get(count).setMark(StopMark.OBEY_LINE);
						}else if(ms.equals("NO_DRAW")){
							lineList.get(i).getTrains().get(h).getStops().get(count).setMark(StopMark.NO_DRAW);
						}else if(ms.equals("CIRCLE")){
							lineList.get(i).getTrains().get(h).getStops().get(count).setMark(StopMark.CIRCLE);
						}else{//カスタムマークの場合は番号で。
							lineList.get(i).getTrains().get(h).getStops().get(count).setMark(customMarks.get(
									Integer.valueOf(ms)));
						}
						count++;
					}
				}
				if(count != Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".NumOfStations"))){
					System.out.println(p.getProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + 
							".NumOfStations") +"," +count);
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("データファイルに不備があります。読み込みは続行されます。\n"
							+ "以下のエラーメッセージを@himeshi_hobにお知らせください。\n"
							+ "line"+i+"train"+h+"で指定された駅数が停車駅として追加されていません。");
					alert.showAndWait();
					//throw new IllegalArgumentException("line"+i+"train"+h+"で指定された駅数が停車駅として追加されていません。");
				}
				double[][] dd = new double[3][4];
				for(int k = 0; k <= 3; k++){
					dd[0][k] = Double.parseDouble(p.getProperty
							("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".Color0" + String.valueOf(k)));
					dd[1][k] = Double.parseDouble(p.getProperty
							("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".Color1" + String.valueOf(k)));
					dd[2][k] = Double.parseDouble(p.getProperty
							("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".Color2" + String.valueOf(k)));
				}
				lineList.get(i).getTrains().get(h).setColorsInDouble(dd);
				lineList.get(i).getTrains().get(h).setLineWidth(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".lineWidth")));
				lineList.get(i).getTrains().get(h).setLineDistance(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".lineDistance")));
				//マークの読み込み
				String markString = p.getProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".mark");
				if(markString == null || markString.equals("CIRCLE")){
					lineList.get(i).getTrains().get(h).setMark(StopMark.CIRCLE);
				}else if(markString.equals("NO_DRAW")){
					lineList.get(i).getTrains().get(h).setMark(StopMark.NO_DRAW);
				}else{//カスタムマークの時は番号から読み込む
					lineList.get(i).getTrains().get(h).setMark(customMarks.get(Integer.valueOf(markString)));
				}
				lineList.get(i).getTrains().get(h).setMarkSize(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".markSize")));
				lineList.get(i).getTrains().get(h).setStaSize(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".staSize")));
				lineList.get(i).getTrains().get(h).setTategaki(Boolean.valueOf(p.getProperty
						("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".tategaki")));//コレ使ってるのか謎
				lineList.get(i).getTrains().get(h).setEdgeA(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".edgeFixA")));
				lineList.get(i).getTrains().get(h).setEdgeB(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".edgeFixB")));
				lineList.get(i).getTrains().get(h).setLineDash(lineDashes.get(Integer.parseInt(p.getProperty
						("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".lineDash","0"))));
			}
		}
		//canvasの設定
		x_largest = 0;
		y_largest = 0;
		for(int i = 0; i < lineList.size(); i++){
			for(int j = 0; j < lineList.get(i).getStations().size(); j++){
				if(lineList.get(i).getStations().get(j).isSet()){
					double[] pc = lineList.get(i).getStations().get(j).getPoint();
					if(pc[0] > x_largest) x_largest = pc[0]; 
					if(pc[1] > y_largest) y_largest = pc[1]; 
				}
			}
		}
		canvasOriginal[0] = x_largest + canvasMargin;
		canvasOriginal[1] = y_largest + canvasMargin;
		canvas.setWidth(x_largest + canvasMargin);
		canvas.setHeight(y_largest + canvasMargin);
		resetParams();//適切にGUIパラメータを再セット。
		rightEditButton.setSelected(true);//読み込み時は路線編集モードにする。
		isLoading = false;
		lineDraw();
	}
	void saveProp(Properties p, ArrayList<Image> images) throws IOException{//データの保存を行う。
		//int imageCount = 0;//イメージ番号は追加前にimages.size()で番号取得できるよね。
		p.setProperty("version", String.valueOf(version));
		p.setProperty("bgColorR", String.valueOf(bgColor.get().getRed()));
		p.setProperty("bgColorG", String.valueOf(bgColor.get().getGreen()));
		p.setProperty("bgColorB", String.valueOf(bgColor.get().getBlue()));
		p.setProperty("bgColorO", String.valueOf(bgColor.get().getOpacity()));
		p.setProperty("stationFont", stationFontFamily.get());
		p.setProperty("NumOfLines", String.valueOf(lineList.size()));
		//lineListを頂点とするデータ群
		for(int i = 0; i < lineList.size(); i++){
			p.setProperty("line" + String.valueOf(i) + ".lineName", lineList.get(i).getName());
			p.setProperty("line" + String.valueOf(i) + ".nameLocation", String.valueOf(lineList.get(i).getNameLocation()));
			p.setProperty("line" + String.valueOf(i) + ".nameStyle", String.valueOf(lineList.get(i).getNameStyle()));
			p.setProperty("line" + String.valueOf(i) + ".nameSize", String.valueOf(lineList.get(i).getNameSize()));
			p.setProperty("line" + String.valueOf(i) + ".nameColorR", String.valueOf(lineList.get(i).getNameColor().getRed()));
			p.setProperty("line" + String.valueOf(i) + ".nameColorG", String.valueOf(lineList.get(i).getNameColor().getGreen()));
			p.setProperty("line" + String.valueOf(i) + ".nameColorB", String.valueOf(lineList.get(i).getNameColor().getBlue()));
			p.setProperty("line" + String.valueOf(i) + ".nameColorO", String.valueOf(lineList.get(i).getNameColor().getOpacity()));
			p.setProperty("line" + String.valueOf(i) + ".nameX", String.valueOf(lineList.get(i).getNameZure()[0]));
			p.setProperty("line" + String.valueOf(i) + ".nameY", String.valueOf(lineList.get(i).getNameZure()[1]));
			p.setProperty("line" + String.valueOf(i) + ".NumOfStations", String.valueOf(lineList.get(i).getStations().size()));
			for(int h = 0; h < lineList.get(i).getStations().size(); h++){
				p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".name", 
						lineList.get(i).getStations().get(h).getName());
				p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".pointSet", 
						String.valueOf(lineList.get(i).getStations().get(h).isSet()));
				if(lineList.get(i).getStations().get(h).isSet()){
					p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".x", 
							String.valueOf(lineList.get(i).getStations().get(h).getPoint()[0]));
					p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".y", 
							String.valueOf(lineList.get(i).getStations().get(h).getPoint()[1]));
				}else{
					p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".x", 
							String.valueOf(lineList.get(i).getStations().get(h).getInterPoint()[0]));
					p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".y", 
							String.valueOf(lineList.get(i).getStations().get(h).getInterPoint()[1]));
				}
				p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".stationConnection", 
						String.valueOf(lineList.get(i).getStations().get(h).getConnection()));
				p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".textMuki", 
						String.valueOf(lineList.get(i).getStations().get(h).getMuki()));
				p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".size", 
						String.valueOf(lineList.get(i).getStations().get(h).getNameSize()));
				p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".style", 
						String.valueOf(lineList.get(i).getStations().get(h).getNameStyle()));
				p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".nameX", 
						String.valueOf(lineList.get(i).getStations().get(h).getNameZure()[0]));
				p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".nameY", 
						String.valueOf(lineList.get(i).getStations().get(h).getNameZure()[1]));
				p.setProperty("line" + String.valueOf(i) + ".sta" + String.valueOf(h) + ".shiftOnStation", 
						String.valueOf(lineList.get(i).getStations().get(h).shiftBasedOnStation()));
			}
			p.setProperty("line" + String.valueOf(i) + ".NumOfTrains", String.valueOf(lineList.get(i).getTrains().size()));
			for(int h = 0; h < lineList.get(i).getTrains().size(); h++){
				p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".name", 
						lineList.get(i).getTrains().get(h).getName());
				p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".NumOfStations", 
						String.valueOf(lineList.get(i).getTrains().get(h).getStops().size()));
				for(int k = 0; k < lineList.get(i).getTrains().get(h).getStops().size(); k++){//stationsは駅名のみ記録する。停車駅ごとのループ
					p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".sta" + String.valueOf(k), 
							lineList.get(i).getTrains().get(h).getStops().get(k).getSta().getName());
					p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".sta" + String.valueOf(k) + ".shiftX", 
							String.valueOf(lineList.get(i).getTrains().get(h).getStops().get(k).getShift()[0]));
					p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".sta" + String.valueOf(k) + ".shiftY", 
							String.valueOf(lineList.get(i).getTrains().get(h).getStops().get(k).getShift()[1]));
					//停車駅マークタイプに関する記述。カスタムマーク対応したらここも追記必要あり。
					StopMark sm = lineList.get(i).getTrains().get(h).getStops().get(k).getMark();
					if(sm == StopMark.OBEY_LINE){
						p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".sta" + String.valueOf(k) + ".mark", 
								"OBEY_LINE");
					}else if(sm == StopMark.NO_DRAW){
						p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".sta" + String.valueOf(k) + ".mark", 
								"NO_DRAW");
					}else if(sm == StopMark.CIRCLE){
						p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".sta" + String.valueOf(k) + ".mark", 
								"CIRCLE");
					}else{//カスタムマークの場合はcustomMarksの番号を記述する。
						p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".sta" + String.valueOf(k) + ".mark", 
								String.valueOf(customMarks.indexOf(sm)));
					}
				}
				double[][] cc = lineList.get(i).getTrains().get(h).getColorsInDouble();//色の記録を行う
				for(int k = 0; k <= 3; k++){//[手動][k]
					p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".Color0" + String.valueOf(k), 
							String.valueOf(cc[0][k]));
					p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".Color1" + String.valueOf(k), 
							String.valueOf(cc[1][k]));
					p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".Color2" + String.valueOf(k), 
							String.valueOf(cc[2][k]));
				}
				p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".lineWidth", 
						String.valueOf(lineList.get(i).getTrains().get(h).getLineWidth()));
				p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".lineDistance", 
						String.valueOf(lineList.get(i).getTrains().get(h).getLineDistance()));
				//マークの保存
				if(lineList.get(i).getTrains().get(h).getMark() == StopMark.CIRCLE){
					p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".mark", 
							"CIRCLE");
				}else if(lineList.get(i).getTrains().get(h).getMark() == StopMark.NO_DRAW){
					p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".mark", 
							"NO_DRAW");
				}else{//カスタムマークの場合は番号を記録
					p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".mark", 
							String.valueOf(customMarks.indexOf(lineList.get(i).getTrains().get(h).getMark())));
				}
				p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".markSize", 
						String.valueOf(lineList.get(i).getTrains().get(h).getMarkSize()));
				p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".staSize", 
						String.valueOf(lineList.get(i).getTrains().get(h).getStaSize()));
				p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".tategaki", 
						String.valueOf(lineList.get(i).getTrains().get(h).isTategaki()));//この属性本当に使ってるのか謎
				p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".edgeFixA", 
						String.valueOf(lineList.get(i).getTrains().get(h).getEdgeA()));
				p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".edgeFixB", 
						String.valueOf(lineList.get(i).getTrains().get(h).getEdgeB()));
				p.setProperty("line" + String.valueOf(i) + ".train" + String.valueOf(h) + ".lineDash",
						String.valueOf(lineDashes.indexOf(lineList.get(i).getTrains().get(h).getLineDash())));//lineDashは番号で。
			}
		}
		//customMarksを頂点とするデータ群
		p.setProperty("NumOfMarks", String.valueOf(customMarks.size()));
		for(int i = 0; i < customMarks.size(); i++){
			p.setProperty("Mark" + i + ".NumOfLayers", String.valueOf(customMarks.get(i).getLayers().size()));
			for(int h = 0; h < customMarks.get(i).getLayers().size(); h++){
				MarkLayer l = customMarks.get(i).getLayers().get(h);
				p.setProperty("Mark" + i + ".layer" + h + ".type", String.valueOf(l.getType()));
				p.setProperty("Mark" + i + ".layer" + h + ".paint", String.valueOf(l.getPaint()));
				p.setProperty("Mark" + i + ".layer" + h + ".numOfParams", String.valueOf(l.getParamProperty().size()));
				for(int k = 0; k < l.getParamProperty().size(); k++){
					p.setProperty("Mark" + i + ".layer" + h + ".param" + k, String.valueOf(l.getParam(k)));
				}
				p.setProperty("Mark" + i + ".layer" + h + ".text", String.valueOf(l.getText()));
				p.setProperty("Mark" + i + ".layer" + h + ".fontName", String.valueOf(l.getFontName()));
				p.setProperty("Mark" + i + ".layer" + h + ".colorR", String.valueOf(l.getColor().getRed()));
				p.setProperty("Mark" + i + ".layer" + h + ".colorG", String.valueOf(l.getColor().getGreen()));
				p.setProperty("Mark" + i + ".layer" + h + ".colorB", String.valueOf(l.getColor().getBlue()));
				p.setProperty("Mark" + i + ".layer" + h + ".colorO", String.valueOf(l.getColor().getOpacity()));
				if(l.getType() == MarkLayer.IMAGE && l.getImage() != null){
					p.setProperty("Mark" + i + ".layer" + h + ".image", String.valueOf(images.size()));
					images.add(l.getImage());
				}
			}
		}
		//freeItemsを頂点とするデータ群
		p.setProperty("NumOfFreeItems", String.valueOf(freeItems.size()));
		for(int i = 0; i < freeItems.size(); i++){
			p.setProperty("FreeItem" + i + ".type", String.valueOf(freeItems.get(i).getType()));
			if(freeItems.get(i).getType() == FreeItem.TEXT){
				p.setProperty("FreeItem" + i + ".text", freeItems.get(i).getText());
				for(int h = 0; h < freeItems.get(i).getParams().length; h++){
					p.setProperty("FreeItem" + i + ".params" + h, String.valueOf(freeItems.get(i).getParams()[h].getValue()));
				}
				p.setProperty("FreeItem" + i + ".ColorR", String.valueOf(freeItems.get(i).getColor().getRed()));
				p.setProperty("FreeItem" + i + ".ColorG", String.valueOf(freeItems.get(i).getColor().getGreen()));
				p.setProperty("FreeItem" + i + ".ColorB", String.valueOf(freeItems.get(i).getColor().getBlue()));
				p.setProperty("FreeItem" + i + ".ColorO", String.valueOf(freeItems.get(i).getColor().getOpacity()));
				p.setProperty("FreeItem" + i + ".font", freeItems.get(i).getFontName());
			}
			if(freeItems.get(i).getType() == FreeItem.IMAGE){
				p.setProperty("FreeItem" + i + ".text", freeItems.get(i).getText());
				for(int h = 0; h < freeItems.get(i).getParams().length; h++){
					p.setProperty("FreeItem" + i + ".params" + h, String.valueOf(freeItems.get(i).getParams()[h].getValue()));
				}
				p.setProperty("FreeItem" + i + ".image", String.valueOf(images.size()));
				images.add(freeItems.get(i).getImage());
			}
		}
		//lineDashesを頂点とするデータ群。index0は直線=nullなので保存しません。
		p.setProperty("NumOfLineDashes", String.valueOf(lineDashes.size()));
		for(int i = 1; i < lineDashes.size(); i++){
			p.setProperty("LineDash" + i + "length", String.valueOf(lineDashes.get(i).get().length));
			for(int h = 0; h < lineDashes.get(i).get().length; h++){
				p.setProperty("LineDash" + i + "." + h, String.valueOf(lineDashes.get(i).get()[h]));
			}
		}
		/*
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		p.store(osw, null);//書き出し
		osw.close();
		*/
	}
	void setObject(Stage s){//このコントローラーに渡したいデータがあればここで。
		this.mainStage = s;//結局使ってません
	}
	double[][] shiftPoint(double[] ini, double[] last, int zure){//線分の両端の座標を与えてzureの分だけずらした線分の両端の点を与える
		double[][] p = new double[2][2];
			//x:+sinθ、y:-cosθ
			p[0][0] = ini[0] + zure * (last[1] - ini[1]) / Math.sqrt(Math.pow(last[0] - ini[0], 2) + Math.pow(last[1] - ini[1], 2));
			p[0][1] = ini[1] - zure * (last[0] - ini[0]) / Math.sqrt(Math.pow(last[0] - ini[0], 2) + Math.pow(last[1] - ini[1], 2));
			p[1][0] = last[0] + zure * (last[1] - ini[1]) / Math.sqrt(Math.pow(last[0] - ini[0], 2) + Math.pow(last[1] - ini[1], 2));
			p[1][1] = last[1] - zure * (last[0] - ini[0]) / Math.sqrt(Math.pow(last[0] - ini[0], 2) + Math.pow(last[1] - ini[1], 2));
		return p;
	}
	ArrayList<Line> detectConnectedLine(Station sta){//与えられたstationが所属するlineを全て返す
		ArrayList<Line> connected = new ArrayList<Line>();
		for(int i = 0; i < lineList.size(); i++){
			for(int h = 0; h < lineList.get(i).getStations().size(); h++){
				if(lineList.get(i).getStations().get(h) == sta) connected.add(lineList.get(i));
			}
		}
		return connected;
	}
	void setMarkList(){//markListをセットする。
		markList.clear();
		trainMarkList.clear();
		markList.add(StopMark.OBEY_LINE);//駅ごとの設定なのでOBEY_LINEを入れておく。
		for(int i = 0; i < StopMark.DefaultMarks.length; i++){
			markList.add(StopMark.DefaultMarks[i]);
			trainMarkList.add(StopMark.DefaultMarks[i]);
		}
		for(StopMark m: customMarks){//カスタムマークを追加する
			markList.add(m);
			trainMarkList.add(m);
		}
	}
	void editTrainStops(Line l, Train t){//系統の停車駅編集は処理が長く色んな所で使うのでメソッド化
		editUIController euc = null;
		FXMLLoader editLoader = null;
		Stage editStage = new Stage();
		editStage.initModality(Modality.APPLICATION_MODAL);
		editStage.initStyle(StageStyle.UNDECORATED);
		AnchorPane ap = null;
		try {
			editLoader = new FXMLLoader(getClass().getResource("editUIController.fxml"));
			ap= (AnchorPane)editLoader.load();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		euc = (editUIController)editLoader.getController();
		euc.setObjects(l, t, editStage);
		Scene sc = new Scene(ap, 600, 300);
		editStage.setScene(sc);
		editStage.setTitle("駅編集ウィンドウ");
		editStage.showAndWait();
		tStaListOb.clear();
		for(int i = 0; i < t.getStops().size(); i++){
			tStaListOb.add(t.getStops().get(i).getSta().getName());
		}
		mapDraw();
	}
	String selectFontFamily(String current){//フォント選択画面を出す。選択されたフォントファミリ名を返す。（not exactフォント名）
		//個別のテキスト挿入にも対応したいので選択されたファミリ名を直接変数に代入することはしません
		String newFont = null;
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
		return newFont;
	}
	void exportImage(){
		//新しくウィンドウを開いて何倍にするか聞く
		Stage expStage = new Stage();
		expStage.initModality(Modality.APPLICATION_MODAL);
		VBox expBox = new VBox();
		Label l1 = new Label("出力するイメージの大きさを設定してください（現在の倍率：200%）");
		Label l2 = new Label("元のサイズ：縦" + canvasOriginal[0] + "、横" + canvasOriginal[1]);
		Label l3 = new Label("出力サイズ：縦" + canvasOriginal[0] * 2 + "、横" + canvasOriginal[1] * 2);
		Slider slider = new Slider();
		slider.setMin(100);
		slider.setMax(1600);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(200);
		slider.setMinorTickCount(1);
		slider.setSnapToTicks(true);
		slider.setValue(200);
		slider.valueProperty().addListener((obs, oldVal, newVal) -> {
			double zoomer = slider.getValue() / 100;
			l3.setText("元のサイズ：縦" + canvasOriginal[0] * zoomer + "、横" + canvasOriginal[1] * zoomer);
			l1.setText("出力するイメージの大きさを設定してください（現在の倍率：" + slider.getValue() + "%）");
		});
		Button b1 = new Button("出力");
		Button b2 = new Button("キャンセル");
		b1.setDefaultButton(true);
		b1.setOnAction((ActionEvent) ->{
			try{
				bgColor.set(re_bg_CP.getValue());
				SnapshotParameters ssp = new SnapshotParameters();
				ssp.setFill(bgColor.get());
				zoom = slider.getValue()/100;
				mapDraw();
				WritableImage wi = canvas.snapshot(ssp, null);
				zoom = 1;
				mapDraw();
				FileChooser fc = new FileChooser();
				fc.setTitle("画像の書き出し");
				FileChooser.ExtensionFilter[] fcef = new FileChooser.ExtensionFilter[4];
				fcef[0] = new FileChooser.ExtensionFilter("PNG形式（*.png）", "*.png");
				fcef[1] = new FileChooser.ExtensionFilter("JPEG形式（*.jpg）", "*.jpg");
				fcef[2] = new FileChooser.ExtensionFilter("Bitmap形式（*.bmp）", "*.bmp");
				fcef[3] = new FileChooser.ExtensionFilter("PDF形式（*.pdf）", "*.pdf");
				fc.getExtensionFilters().add(fcef[0]);
				//fc.getExtensionFilters().add(fcef[1]);
				//fc.getExtensionFilters().add(fcef[2]);
				//fc.getExtensionFilters().add(fcef[3]);
				File imageFile = fc.showSaveDialog(null);
				int format = 0;//書き出し形式特定用
				for(int k = 0; k < 4; k++){
					if(fc.getSelectedExtensionFilter() == fcef[k]) format = k;
				}
				if(imageFile != null){
					try{
						switch(format){
						case 0:
							ImageIO.write(SwingFXUtils.fromFXImage(wi,null), "png", imageFile);
							break;
						case 1:
							ImageIO.write(SwingFXUtils.fromFXImage(wi,null), "jpg", imageFile);
							break;
						case 2:
							ImageIO.write(SwingFXUtils.fromFXImage(wi,null), "bmp", imageFile);
							break;
						case 3:
							//PDFBoxを使って実装する
							break;
						}
					}catch(IOException e){
						Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
						alert.getDialogPane().setContentText("保存中にエラーが発生しました。");
						alert.showAndWait();
					}
				}
				expStage.close();
			}catch(RuntimeException e){
				e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("出力サイズが大きすぎるようです。倍率を下げてみてください。");
				alert.showAndWait();
			}
		});
		b2.setCancelButton(true);
		b2.setOnAction((ActionEvent) ->{
			expStage.close();
		});
		expBox.getChildren().add(l1);
		expBox.getChildren().add(l2);
		expBox.getChildren().add(l3);
		expBox.getChildren().add(slider);
		expBox.getChildren().add(b1);
		expBox.getChildren().add(b2);
		expStage.setScene(new Scene(expBox));
		expStage.showAndWait();
	}
	boolean checkUpdate(boolean onLaunch){//アップデートがあればtrue。なければfalse。このフラグは手動で確認が行われた時用。
		boolean nv = false;
		URL url;
		if(onLaunch) {
			postUsage();
		}
		try {
			url = new URL("https://spreadsheets.google.com/feeds/cells/1AzBe7Jdny2YnIW9hUixfbGBLmaj1xRGjEEl042fZ7kE/od6/public/values");
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			/*
			 * スプレッドシートの書式について　全てA列を使って（[a]はanalyzeXMLから返ってくる配列のindex）
			 * [0]1:最新バージョンの番号
			 * [1]2:Description in Japanese
			 * [2]3:URL for description in Japanese
			 * [3]4:Description in English
			 * [4]5:URL for description in English
			 * [5]6:Auto-Download URL
			 */
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
				System.out.println("接続に問題はありません。");
				InputStream is = conn.getInputStream();
				//BufferedReader br = new BufferedReader(new InputStreamReader(is));
				//System.out.println(br.readLine());
				BufferedInputStream bis = new BufferedInputStream(is);
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = factory.newDocumentBuilder();
				Document document = documentBuilder.parse(bis);
				bis.close();
				
				//XMLの解析は別メソッドに丸投げします。
				String[] xmlDatas = analyzeXML(document);//XMLを分析した結果はここに大きさ6の配列で返ってくる。
				if(Double.parseDouble(xmlDatas[0]) > ReleaseVersion){
					nv = true;
					Platform.runLater(() ->{
						Alert alert = new Alert(AlertType.INFORMATION,"",ButtonType.CLOSE);
						alert.getDialogPane().setHeaderText("新しいバージョンがリリースされています。");
						Hyperlink link = new Hyperlink(xmlDatas[2]);
						link.setOnAction((ActionEvent) -> {
							Desktop desktop = Desktop.getDesktop();
							URI uu;
							try {
								uu = new URI(xmlDatas[2]);
								desktop.browse(uu);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
						Label l1 = new Label("新しいバージョン：" + xmlDatas[0]);
						Label l2 = new Label(xmlDatas[1]);
						alert.getDialogPane().setContent(new VBox(4.0, l1, l2, link));
						alert.showAndWait();
					});
				}
				else {
					System.out.println("この本体は最新バージョンです．");
				}
			}else{
				nv = true;
				Platform.runLater(() ->{
					Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
					try {
						alert.getDialogPane().setContentText("ソフトウェアのアップデートを確認できませんでした。\n" + 
						conn.getResponseCode() + conn.getResponseMessage());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					alert.show();
				});
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
			nv = true;
			Platform.runLater(() ->{
				Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("ソフトウェアのアップデートを確認できませんでした。\n" + 
				"ネットワークに接続できません。");
				alert.show();
			});
		} catch(SAXException e){
			e.printStackTrace();
		} catch(ParserConfigurationException e){
			e.printStackTrace();
		}
		
		return nv;
	}
	
	void postUsage() {
		try {
			URL url = new URL("https://us-central1-routemapmaker-6c324.cloudfunctions.net/registerUsage?version="
		+ ReleaseVersion + "&os=" + URLEncoder.encode(System.getProperty("os.name"), "UTF-8")
		+ "&locale=" + Locale.getDefault().getCountry());
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
		} catch(IOException e) {
			// usageの送信だけなので特にエラー処理はしない．
			e.printStackTrace();
		}
	}

	String[] analyzeXML(Document document){
		String[] data = new String[6];
		Element root = document.getDocumentElement();
		NodeList children1 = root.getChildNodes();
		for(int i1 = 0 ; i1 < children1.getLength(); i1++){
			Node node1 = children1.item(i1);
			if(node1.getNodeType() == Node.ELEMENT_NODE){
				Element element1 = (Element)node1;
				if(element1.getNodeName().equals("entry")){
					NodeList children2 = node1.getChildNodes();
					String categoryTitle = null;//ここでこのcategoryがどのデータに相当するのかを記憶します。
					String categoryContent = null;
					for(int i2 = 0; i2 < children2.getLength(); i2++){
						Node node2 = children2.item(i2);
						if(node2.getNodeType() == Node.ELEMENT_NODE){
							Element element2 = (Element)node2;
							if(element2.getNodeName().equals("title")) categoryTitle = element2.getTextContent();
							if(element2.getNodeName().equals("content")) categoryContent = element2.getTextContent();
						}
					}
					if(categoryTitle != null){
						if(categoryTitle.equals("A1")) data[0] = categoryContent;
						if(categoryTitle.equals("A2")) data[1] = categoryContent;
						if(categoryTitle.equals("A3")) data[2] = categoryContent;
						if(categoryTitle.equals("A4")) data[3] = categoryContent;
						if(categoryTitle.equals("A5")) data[4] = categoryContent;
						if(categoryTitle.equals("A6")) data[5] = categoryContent;
					}
				}
			}
		}
		//全ての項目が満たされてるか一応チェック
		for(int i = 0; i < 6; i++){
			if(data[i] == null) throw new IllegalArgumentException("update情報項目" + i + "が取得できていません。");
		}
		return data;
	}
	void setAutoUpdate(Button bt, String surl){//オートアップデートを実行するメソッド。結局やらないことにしました。
		bt.setOnAction((ActionEvent) ->{
			boolean goNext = true;//エラーがあったらこいつをfalseにして止めます。
			try{
				URL url = new URL(surl);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setRequestMethod("GET");
				conn.connect();
				if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
					System.out.println("新しい実行ファイルのダウンロードに問題ありません。");
					String CURRENT_DIRECTORY = new File("").getAbsolutePath();
					InputStream is = conn.getInputStream();
				}
			}catch(MalformedURLException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
		});
	}
	void saveHistory(){//現在の状態をヒストリに加える。
		//最初に保存していいか確認
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setContentText("ヒストリに保存するために現在の状態を保存します。よろしいですか？");
		Optional<ButtonType> result = alert.showAndWait();
		if(result.get() == ButtonType.OK){
			
		}
	}
	protected void setMenuBarMode(boolean b){//メニューバーはシステムのものを使うかどうか設定
		menubar.setUseSystemMenuBar(b);
	}
	void initializeLineDashes(){
		lineDashes.clear();
		double[] d1 = {5d,5d};
		double[] d2 = {12d,8d};
		lineDashes.add(Train.NORMAL_LINE);//直線
		lineDashes.add(new DoubleArrayWrapper(d1));
		lineDashes.add(new DoubleArrayWrapper(d2));
	}
}

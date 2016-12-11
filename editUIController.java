package RouteMapMaker;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class editUIController implements Initializable{
	
	private Line line;
	private Train train;
	private Stage stage;
	private ObservableList<String> olB = FXCollections.observableArrayList();
	private ObservableList<String> olC = FXCollections.observableArrayList();
	
	@FXML ToggleButton Insert;
	@FXML ToggleButton Delete;
	@FXML ToggleButton InsertAll;
	@FXML ToggleButton DeleteAll;
	@FXML Button Close;
	@FXML ListView listB;
	@FXML ListView listC;
	@FXML Label infoLabel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		infoLabel.setText("停車駅は駅一覧の中から\n"
				+ "上から順に追加してください。\n\n"
				+ "挿入：挿入位置を右枠で選択し\n"
				+ "挿入ボタンを押してから左枠\n"
				+ "から挿入する駅を選択する。\n"
				+ "hint:反応しない場合は\n"
				+ "別の駅を選択してみてください\n\n"
				+ "削除：削除ボタンを押してから\n"
				+ "右枠で削除する駅を選択する。");
		listB.setItems(olB);
		listC.setItems(olC);
		ToggleGroup group = new ToggleGroup();
		Insert.setToggleGroup(group);
		Delete.setToggleGroup(group);
		InsertAll.setToggleGroup(group);
		DeleteAll.setToggleGroup(group);
		group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle old_toggle,
				Toggle new_toggle) ->{
					if(group.getSelectedToggle() == InsertAll){
						Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
						alert.setTitle("選択路線駅全追加の確認");
						alert.setContentText("全ての駅を停車駅として追加してよろしいですか？");
						Optional<ButtonType> result = alert.showAndWait();
						if(result.get() == ButtonType.OK){
							train.getStops().clear();//停車駅を一度全て削除
							for(int i = line.getStations().size() - 1; 0 <= i; i--){//最後から追加していく。
								train.getStops().add(0, new TrainStop(line.getStations().get(i)));
							}
							olC.clear();
							for(int i = 0; i < train.getStops().size(); i++){
								olC.add(train.getStops().get(i).getSta().getName());
							}
							olC.add("<最後に追加>");
							listC.getSelectionModel().select(0);
						}
						InsertAll.setSelected(false);
					}
					if(group.getSelectedToggle() == DeleteAll){
						Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
						alert.setTitle("選択駅全消去の確認");
						alert.setContentText("全ての停車駅を削除してよろしいですか？");
						Optional<ButtonType> result = alert.showAndWait();
						if(result.get() == ButtonType.OK){
							//全消去処理
							train.getStops().clear();
							olC.clear();
							olC.add("<最後に追加>");
							listC.getSelectionModel().select(train.getStops().size());
						}
						DeleteAll.setSelected(false);
					}
				});
		listB.setOnMouseClicked((MouseEvent) ->{
			if(group.getSelectedToggle() == Insert){
				int indexB = listB.getSelectionModel().getSelectedIndex();
				int indexC = listC.getSelectionModel().getSelectedIndex();
				if(indexC == -1){
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setContentText("停車駅を追加する位置を選んでください。");
					alert.showAndWait();
				}else if(indexB != -1){
					//追加して大丈夫か検査する。
					int pre;//前の停車駅の路線でのindex
					int next;//後の停車駅の路線でのindex
					try{
						pre = line.getStations().indexOf(train.getStops().get(indexC - 1).getSta());
					}catch(IndexOutOfBoundsException e){
						pre = -1;//系統の先頭に追加要求があった場合。
					}
					try{
						next = line.getStations().lastIndexOf(train.getStops().get(indexC).getSta());
					}catch(IndexOutOfBoundsException e){
						next = line.getStations().size();//系統の最後に追加要求があった場合
					}
					if(pre < indexB && indexB < next){
						boolean adjon = false;//連続して同じ駅が登録されていると都合が悪い。
						try{
							if(line.getStations().get(indexB) == train.getStops().get(indexC -1).getSta()) adjon = true;
							if(line.getStations().get(indexB) == train.getStops().get(indexC).getSta()) adjon = true;
						}catch(Exception e){
							//indexエラーはここでは無視していいので何もしない
						}
						if(adjon){
							Alert alert = new Alert(Alert.AlertType.WARNING);
							alert.setContentText("同じ駅を隣接して追加することはできません。");
							alert.showAndWait();
						}else{//順番検査と隣接検査をクリアしたら追加する。
							train.getStops().add(indexC, new TrainStop(line.getStations().get(indexB)));
						}
					}else{
						Alert alert = new Alert(Alert.AlertType.WARNING);
						alert.setContentText("停車駅は駅一覧の上から順である必要があります。");
						alert.showAndWait();
					}
					olC.clear();
					for(int i = 0; i < train.getStops().size(); i++){
						olC.add(train.getStops().get(i).getSta().getName());
					}
					olC.add("<最後に追加>");
					listC.getSelectionModel().select(indexC + 1);
				}
			}
		});
		listC.setOnMouseClicked((MouseEvent) ->{
			//動作に不具合は見られないけどIndexOutOfBoundsExceptionが出てくる
			if(group.getSelectedToggle() == Delete){
				int indexC = listC.getSelectionModel().getSelectedIndex();
				if(indexC != -1 && indexC < train.getStops().size()){
					train.getStops().remove(indexC);
					olC.clear();
					for(int i = 0; i < train.getStops().size(); i++){
						olC.add(train.getStops().get(i).getSta().getName());
					}
					olC.add("<最後に追加>");
					listC.getSelectionModel().select(train.getStops().size());
				}
			}
		});
		Close.setOnAction((ActionEvent) -> {
			stage.close();
		});
	}
	
	public void setObjects(Line line, Train train, Stage stage){
		this.line = line;
		this.train = train;
		this.stage = stage;
		//listBの初期設定
		olB.clear();
		for(int i = 0; i < line.getStations().size(); i++){
			olB.add(line.getStations().get(i).getName());
		}
		//listCの初期設定
		olC.clear();
		for(int i = 0; i < train.getStops().size(); i++){
			olC.add(train.getStops().get(i).getSta().getName());
		}
		olC.add("<最後に追加>");
		listC.getSelectionModel().select(train.getStops().size());
	}

}

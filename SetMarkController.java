package RouteMapMaker;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

public class SetMarkController implements Initializable{
	ObservableList<Line> lineList = null;
	ObservableList<StopMark> marks = FXCollections.observableArrayList();
	ObservableList<String> ObA = FXCollections.observableArrayList();
	ObservableList<String> ObB = FXCollections.observableArrayList();
	ObservableList<String> ObC = FXCollections.observableArrayList();
	
	@FXML ListView<String> listA;
	@FXML ListView<String> listB;
	@FXML ListView<String> listC;
	@FXML ListView<StopMark> listM;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		listA.setItems(ObA);
		listB.setItems(ObB);
		listC.setItems(ObC);
		listC.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listM.setItems(marks);
		listM.setCellFactory(new StopMarkCell());
		listA.getSelectionModel().selectedItemProperty().addListener((ov,oldVal,newVal) ->{
			ObC.clear();//誤選択を防ぐためにクリアする
			ObB.clear();
			for(Train train: lineList.get(listA.getSelectionModel().getSelectedIndex()).getTrains()){
				ObB.add(train.getName());
			}
		});
		listB.getSelectionModel().selectedItemProperty().addListener((ov,oldVal,newVal) ->{
			ObC.clear();
			int indexA = listA.getSelectionModel().getSelectedIndex();
			int indexB = listB.getSelectionModel().getSelectedIndex();
			if(indexA != -1 && indexB != -1){
				for(TrainStop stop: lineList.get(indexA).getTrains().get(indexB).getStops()){
					ObC.add(stop.getSta().getName());
					if(stop.getMark() == listM.getSelectionModel().getSelectedItem()) listC.getSelectionModel().selectLast();
				}
			}
		});
		listC.setOnMouseClicked((MouseEvent) ->{
			int indexA = listA.getSelectionModel().getSelectedIndex();
			int indexB = listB.getSelectionModel().getSelectedIndex();
			StopMark markM = listM.getSelectionModel().getSelectedItem();
			ObservableList<Integer> indexC = listC.getSelectionModel().getSelectedIndices();
			if(indexA != -1 && indexB != -1 && markM != null){
				for(int i = 0; i < lineList.get(indexA).getTrains().get(indexB).getStops().size(); i++){
					if(indexC.contains(i)) lineList.get(indexA).getTrains().get(indexB).getStops().get(i).setMark(markM);
				}
				//あとは消去した上で再び選択処理
				listC.getSelectionModel().clearSelection();//選択を解除
				for(int i = 0; i < lineList.get(indexA).getTrains().get(indexB).getStops().size(); i++){
					if(lineList.get(indexA).getTrains().get(indexB).getStops().get(i).getMark() == listM.getSelectionModel().getSelectedItem()){
						listC.getSelectionModel().select(i);
					}
				}
			}
		});
		listM.getSelectionModel().selectedItemProperty().addListener((ov,oldVal,newVal) ->{
			int indexA = listA.getSelectionModel().getSelectedIndex();
			int indexB = listB.getSelectionModel().getSelectedIndex();
			if(indexA != -1 && indexB != -1 && ObC.size() != 0){
				listC.getSelectionModel().clearSelection();//選択を解除
				for(int i = 0; i < lineList.get(indexA).getTrains().get(indexB).getStops().size(); i++){
					if(lineList.get(indexA).getTrains().get(indexB).getStops().get(i).getMark() == listM.getSelectionModel().getSelectedItem()){
						listC.getSelectionModel().select(i);
					}
				}
			}
		});
	}

	public void setObject(ObservableList<Line> lineList, ObservableList<StopMark> customMarks){
		this.lineList = lineList;
		//marksの初期設定。
		marks.add(StopMark.OBEY_LINE);
		for(StopMark m:StopMark.DefaultMarks){
			marks.add(m);
		}
		for(StopMark m:customMarks){
			marks.add(m);
		}
		listM.getSelectionModel().select(0);
		//ObAの初期設定
		ObA.clear();
		for(Line line:lineList){
			ObA.add(line.getName());
		}
	}
}

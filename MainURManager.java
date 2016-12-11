package RouteMapMaker;

import java.util.ArrayList;

import RouteMapMaker.URElements.ArrayCommands;
import RouteMapMaker.URElements.Type;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class MainURManager extends URElements {
	//main画面でundo/redoをサポートするため独自命令に対応するクラス。
	private ObservableList<canUR> MainCommandList = FXCollections.observableArrayList();
	
	public void push(ObservableList<Station> staList, int staIndex, Station removedSta,
			ObservableList<Train> trains, ArrayList<Integer[]> removeList, ObservableList<TrainStop> stopValue){//DELETE_LINE
		undoTypeStack.push(Type.SUBCLASS);
		undoIndexStack.push(MainCommandList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		Station_Delete sd = new Station_Delete();
		sd.staList = staList;
		sd.staIndex = staIndex;
		sd.removedSta = removedSta;
		sd.trains = trains;
		sd.removeList = removeList;
		sd.stopValue = stopValue;
		MainCommandList.add(sd);
		undoable.set(true);
		redoable.set(false);
	}
	public void push(ObservableList<Station> staList,Station oldSta,Station newSta,int staIndex,
				ObservableList<Train> trains,ArrayList<Integer[]> setList,ObservableList<TrainStop> stopValue){//Deconnect
		undoTypeStack.push(Type.SUBCLASS);
		undoIndexStack.push(MainCommandList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		Station_Deconnect sd = new Station_Deconnect(staList,oldSta,newSta,staIndex, trains, setList, stopValue);
		MainCommandList.add(sd);
		undoable.set(true);
		redoable.set(false);
	}
	public void push(ObservableList<MvSta> movingStList){//moveStations
		undoTypeStack.push(Type.SUBCLASS);
		undoIndexStack.push(MainCommandList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		moveStations ms = new moveStations(movingStList);
		MainCommandList.add(ms);
		undoable.set(true);
		redoable.set(false);
	}
	public void push(Train train, StopMark oldMark, StopMark newMark){
		undoTypeStack.push(Type.SUBCLASS);
		undoIndexStack.push(MainCommandList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		setTrainMark tm = new setTrainMark(train,oldMark,newMark);
		MainCommandList.add(tm);
		undoable.set(true);
		redoable.set(false);
	}
	public void push(TrainStop stop, StopMark oldMark, StopMark newMark){
		undoTypeStack.push(Type.SUBCLASS);
		undoIndexStack.push(MainCommandList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		setStopMark sm = new setStopMark(stop,oldMark,newMark);
		MainCommandList.add(sm);
		undoable.set(true);
		redoable.set(false);
	}
	public void push(Train train, DoubleArrayWrapper oldArray, DoubleArrayWrapper newArray){
		undoTypeStack.push(Type.SUBCLASS);
		undoIndexStack.push(MainCommandList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		setLineDashes sl = new setLineDashes(train, oldArray, newArray);
		MainCommandList.add(sl);
		undoable.set(true);
		redoable.set(false);
	}
	public void push(ObservableList<DoubleProperty> props, ObservableList<Double> oldVals, ObservableList<Double> newVals,
			double[] oldSize, double[] newSize, UIController uic){
		undoTypeStack.push(Type.SUBCLASS);
		undoIndexStack.push(MainCommandList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		transform tf = new transform(props, oldVals, newVals, oldSize, newSize,uic);
		MainCommandList.add(tf);
		undoable.set(true);
		redoable.set(false);
	}
	public void push(ObservableList<Line> lineList, ArrayList<Integer[]> replaceIndex, Station prevSta,
			Station replacing, boolean fixed){
		undoTypeStack.push(Type.SUBCLASS);
		undoIndexStack.push(MainCommandList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		integrateSta is = new integrateSta(lineList, replaceIndex, prevSta, replacing, fixed);
		MainCommandList.add(is);
		undoable.set(true);
		redoable.set(false);
	}
	@Override
	public void undo(){
		super.undo();
		if(type == URElements.Type.SUBCLASS){
			MainCommandList.get(index).undo();
		}
	}
	@Override
	public void redo(){
		super.redo();
		if(type == URElements.Type.SUBCLASS){
			MainCommandList.get(index).redo();
		}
	}
	interface canUR{//各命令を保持する内部クラス群はすべてコレを実装する。
		void undo();
		void redo();
	}
	//以下、各命令を保持する内部クラス群
	public class Station_Delete implements canUR{
		ObservableList<Station> staList;
		int staIndex;
		Station removedSta;
		ObservableList<Train> trains;
		ArrayList<Integer[]> removeList;
		ObservableList<TrainStop> stopValue;
		@Override
		public void undo() {
			// TODO Auto-generated method stub
			staList.add(staIndex, removedSta);//駅自体の復元
			//一緒に削除された停車駅の復元
			for(int i = 0; i < removeList.size(); i++){
				Integer[] removeIndex = removeList.get(i);
				trains.get(removeIndex[0]).getStops().add(removeIndex[1], stopValue.get(i));
			}
		}
		@Override
		public void redo() {
			// TODO Auto-generated method stub
			staList.remove(staIndex);//駅自体の削除
			//停車駅の削除
			for(int i = 0; i < removeList.size(); i++){
				Integer[] removeIndex = removeList.get(i);
				trains.get(removeIndex[0].intValue()).getStops().remove(removeIndex[1].intValue());
			}
		}
	}
	public class Station_Deconnect implements canUR{
		ObservableList<Station> staList;
		Station oldSta;//置き換え前の駅
		Station newSta;//接続を切った後の新駅
		int staIndex;//line.stationsの方の置き換えるindex
		ObservableList<Train> trains;
		ArrayList<Integer[]> setList;//{系統番号,停車駅番号}
		ObservableList<TrainStop> stopValue;//偶数はold,奇数はnewにする
		Station_Deconnect(ObservableList<Station> staList,Station oldSta,Station newSta,int staIndex,
				ObservableList<Train> trains,ArrayList<Integer[]> setList,ObservableList<TrainStop> stopValue){
			this.staList = staList;
			this.oldSta = oldSta;
			this.newSta = newSta;
			this.staIndex = staIndex;
			this.trains = trains;
			this.setList = setList;
			this.stopValue = stopValue;
		}
		@Override
		public void undo() {
			// TODO Auto-generated method stub
			staList.set(staIndex, oldSta);
			for(int i = 0; i < setList.size(); i++){
				Integer[] setIndex = setList.get(i);
				trains.get(setIndex[0].intValue()).getStops().set(setIndex[1].intValue(), stopValue.get(i * 2));
			}
		}
		@Override
		public void redo() {
			// TODO Auto-generated method stub
			staList.set(staIndex, newSta);
			for(int i = 0; i < setList.size(); i++){
				Integer[] setIndex = setList.get(i);
				trains.get(setIndex[0].intValue()).getStops().set(setIndex[1].intValue(), stopValue.get(i * 2 + 1));
			}
		}
	}
	public class moveStations implements canUR{
		//MvStaオブジェクトをそのまま使うと外部から変更された時にヤバイので全て値をコピーして使います。
		ObservableList<Station> station = FXCollections.observableArrayList();
		ObservableList<Boolean> isSet = FXCollections.observableArrayList();//変更前の状態（変更後は必ずtrueなので）
		ObservableList<Double> startX = FXCollections.observableArrayList();
		ObservableList<Double> startY = FXCollections.observableArrayList();
		ObservableList<Double> afterX = FXCollections.observableArrayList();
		ObservableList<Double> afterY = FXCollections.observableArrayList();
		moveStations(ObservableList<MvSta> movingStList){
			for(MvSta ms: movingStList){
				station.add(ms.sta);
				isSet.add(ms.isSet);
				startX.add(ms.start[0]);
				startY.add(ms.start[1]);
				afterX.add(ms.sta.getPoint()[0]);
				afterY.add(ms.sta.getPoint()[1]);
			}
		}
		@Override
		public void undo() {
			// TODO Auto-generated method stub
			for(int i = 0; i < station.size(); i++){
				if(isSet.get(i)) station.get(i).setPoint(startX.get(i), startY.get(i));
				if(! isSet.get(i)) station.get(i).setInterPoint(startX.get(i), startY.get(i));
			}
		}
		@Override
		public void redo() {
			// TODO Auto-generated method stub
			for(int i = 0; i < station.size(); i++){
				station.get(i).setPoint(afterX.get(i), afterY.get(i));
			}
		}
	}
	public class setTrainMark implements canUR{
		Train train;
		StopMark oldMark;
		StopMark newMark;
		setTrainMark(Train train, StopMark oldMark, StopMark newMark){
			this.train = train;
			this.oldMark = oldMark;
			this.newMark = newMark;
		}
		@Override
		public void undo() {
			// TODO Auto-generated method stub
			train.setMark(oldMark);
		}
		@Override
		public void redo() {
			// TODO Auto-generated method stub
			train.setMark(newMark);
		}
	}
	public class setStopMark implements canUR{
		TrainStop stop;
		StopMark oldMark;
		StopMark newMark;
		setStopMark(TrainStop stop, StopMark oldMark, StopMark newMark){
			this.stop = stop;
			this.oldMark = oldMark;
			this.newMark = newMark;
		}
		@Override
		public void undo() {
			// TODO Auto-generated method stub
			stop.setMark(oldMark);
		}
		@Override
		public void redo() {
			// TODO Auto-generated method stub
			stop.setMark(newMark);
		}
	}
	public class setLineDashes implements canUR{
		Train train;
		DoubleArrayWrapper oldArray;
		DoubleArrayWrapper newArray;
		setLineDashes(Train train, DoubleArrayWrapper oldArray, DoubleArrayWrapper newArray){
			this.train = train;
			this.oldArray = oldArray;
			this.newArray = newArray;
		}
		@Override
		public void undo() {
			// TODO Auto-generated method stub
			train.setLineDash(oldArray);
		}
		@Override
		public void redo() {
			// TODO Auto-generated method stub
			train.setLineDash(newArray);
		}
	}
	public class transform implements canUR{
		ObservableList<DoubleProperty> props = FXCollections.observableArrayList();
		ObservableList<Double> oldVals = FXCollections.observableArrayList();
		ObservableList<Double> newVals = FXCollections.observableArrayList();
		double[] oldSize;
		double[] newSize;
		UIController uic;
		transform(ObservableList<DoubleProperty> props, ObservableList<Double> oldVals, ObservableList<Double> newVals,
				double[] oldSize, double[] newSize, UIController uic){
			this.props = props;
			this.oldVals = oldVals;
			this.newVals = newVals;
			this.oldSize = oldSize;
			this.newSize = newSize;
			this.uic = uic;
		}
		@Override
		public void undo() {
			// TODO Auto-generated method stub
			for(int i = 0; i < props.size(); i++){
				props.get(i).set(oldVals.get(i));
			}
			uic.canvasOriginal = oldSize;
		}
		@Override
		public void redo() {
			// TODO Auto-generated method stub
			for(int i = 0; i < props.size(); i++){
				props.get(i).set(newVals.get(i));
			}
			uic.canvasOriginal = newSize;
		}
	}
	public class integrateSta implements canUR{
		ObservableList<Line> lineList;
		ArrayList<Integer[]> replaceIndex;
		Station prevSta;//置き換え前
		Station replacing;//置き換え後
		boolean fixed;//以前座標固定点だったか否か
		integrateSta(ObservableList<Line> lineList, ArrayList<Integer[]> replaceIndex, Station prevSta,
				Station replacing, boolean fixed){
			this.lineList = lineList;
			this.replaceIndex = replaceIndex;
			this.prevSta = prevSta;
			this.replacing = replacing;
			this.fixed = fixed;
		}
		@Override
		public void undo() {
			// TODO Auto-generated method stub
			for(Integer[] index: replaceIndex){
				lineList.get(index[0].intValue()).getStations().set(index[1].intValue(), prevSta);
			}
			if(! fixed) replacing.erasePoint();//座標非固定点ならば非固定にする。
		}
		@Override
		public void redo() {
			// TODO Auto-generated method stub
			if(! fixed) replacing.setPoint(replacing.getInterPoint()[0], replacing.getInterPoint()[1]);
			for(Integer[] index: replaceIndex){
				lineList.get(index[0].intValue()).getStations().set(index[1].intValue(),replacing);
			}
		}
	}
}

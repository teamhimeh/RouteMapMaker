package RouteMapMaker;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class StopMark implements Cloneable{//駅停車マークの種類を保持する。
	public static final StopMark OBEY_LINE = new StopMark();
	//以下の２つについてはとりあえず処理を外部に丸投げしてしまおう。
	public static final StopMark CIRCLE = new StopMark();
	public static final StopMark NO_DRAW = new StopMark();
	public static final StopMark[] DefaultMarks = {NO_DRAW, CIRCLE};
	
	ObservableList<MarkLayer> layers = FXCollections.observableArrayList();//カスタムマークはコレで内容を定義。
	public StopMark(){
		
	}
	public ObservableList<MarkLayer> getLayers(){
		return this.layers;
	}
	@Override
	public StopMark clone(){
		StopMark t = null;
		try {
			t = (StopMark)super.clone();
			t.layers = FXCollections.observableArrayList();
			for(int i = 0;i < this.layers.size(); i++) t.layers.add(this.layers.get(i).clone());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;
	}

}

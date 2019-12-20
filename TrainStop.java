package RouteMapMaker;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class TrainStop {//系統毎に保持する必要がある駅に関する情報を駅オブジェクトと関連付けて保存。
	private Station station;//駅
	private IntegerProperty shiftX = new SimpleIntegerProperty();//停車駅印のシフト
	private IntegerProperty shiftY = new SimpleIntegerProperty();
	private StopMark mark = StopMark.OBEY_LINE;//駅ごとの停車駅印。
	
	public TrainStop(Station s){//オブジェクトの生成時はstationを要求。
		this.station = s;
		shiftX.set(0);
		shiftY.set(0);
		if(s.getNameSize() == -1) mark = StopMark.NO_DRAW;//中継点な時はデフォルトでNO_DRAWを設定する。
	}
	public void setShiftX(int x){
		this.shiftX.set(x);
	}
	public void setShiftY(int y){
		this.shiftY.set(y);
	}
	public IntegerProperty getShiftXProperty(){
		return this.shiftX;
	}
	public IntegerProperty getShiftYProperty(){
		return this.shiftY;
	}
	public int[] getShift(){
		int s[] = {shiftX.get(), shiftY.get()};
		return s;
	}
	public Station getSta(){
		return this.station;
	}
	public void setSta(Station s) {
		station = s;
	}
	public void setMark(StopMark m){
		this.mark = m;
	}
	public StopMark getMark(){
		return this.mark;
	}
}

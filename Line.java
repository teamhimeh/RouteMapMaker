package RouteMapMaker;

import java.util.ArrayList;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class Line {//路線の情報を保持するクラス。
	public static final int REGULAR = 0;
	public static final int ITALIC = 1;
	public static final int BOLD = 2;
	public static final int ITALIC_BOLD = 3;
	
	public static final int RIGHT = 0;
	public static final int LEFT = 1;
	public static final int TOP = 2;
	public static final int BOTTOM = 3;
	
	private ObservableList<Station> stations;//駅名を保持する。順番は大事。
	private ObservableList<Train> trains;//運転系統を保持する。
	private ObservableList<Integer> curveIdxs; //楕円曲線で線をつなぐ駅のidxを記録する
	private StringProperty lineName = new SimpleStringProperty();//路線名
	private BooleanProperty tategaki = new SimpleBooleanProperty(false);//縦書きか横書きか。trueなら縦書き
	private IntegerProperty nameStyle = new SimpleIntegerProperty(REGULAR);
	private IntegerProperty nameSize = new SimpleIntegerProperty(15);
	private IntegerProperty nameLocation = new SimpleIntegerProperty(BOTTOM);
	private ColorWrapper nameColor = new ColorWrapper(Color.BLACK);
	private IntegerProperty NameX = new SimpleIntegerProperty(0);
	private IntegerProperty NameY = new SimpleIntegerProperty(0);
	
	public Line(String name){//コンストラクタ
		stations = FXCollections.observableArrayList();
		trains = FXCollections.observableArrayList();
		curveIdxs = FXCollections.observableArrayList();
		setName(name);
		//始点と終点は確保しておく
		stations.add(new Station(name + "始点"));
		stations.add(new Station(name + "終点"));
	}
	
	public String getName(){
		return lineName.get();
	}
	public StringProperty getNameProperty(){
		return this.lineName;
	}
	public void setName(String name){
		this.lineName.set(name);
	}
	public ObservableList<Station> getStations(){
		return stations;
	}
	public void setStations(ObservableList<Station> st){
		stations = st;
	}
	public boolean isTategaki(){
		return tategaki.get();
	}
	public BooleanProperty getTategakiProperty(){
		return this.tategaki;
	}
	public void setTategaki(boolean t){
		this.tategaki.set(t);
	}
	public void setNameStyle(int i){
		this.nameStyle.set(i);
	}
	public IntegerProperty getNameStyleProperty(){
		return this.nameStyle;
	}
	public int getNameStyle(){
		return this.nameStyle.get();
	}
	public int getNameSize(){
		return this.nameSize.get();
	}
	public IntegerProperty getNameSizeProperty(){
		return this.nameSize;
	}
	public void setNameSize(int i){
		if(i <= 0) throw new IllegalArgumentException("lineのNameSizeは0以下にできません");//0以下は許容しません。
		this.nameSize.set(i);
	}
	public int getNameLocation(){
		return this.nameLocation.get();
	}
	public IntegerProperty getNameLocationProperty(){
		return this.nameLocation;
	}
	public void setNameLocation(int i){
		this.nameLocation.set(i);
	}
	public Color getNameColor(){
		return this.nameColor.get();
	}
	public ColorWrapper getNameColorProperty(){
		return this.nameColor;
	}
	public void setNameColor(Color c){
		this.nameColor.set(c);
	}
	public void setNameX(int i){
		this.NameX.set(i);
	}
	public void setNameY(int i){
		this.NameY.set(i);
	}
	public IntegerProperty getNameXProperty(){
		return this.NameX;
	}
	public IntegerProperty getNameYProperty(){
		return this.NameY;
	}
	public int[] getNameZure(){
		int[] ia = {NameX.get(), NameY.get()};
		return ia;
	}
	public ObservableList<Train> getTrains(){
		return trains;
	}
	public void setTrains(ObservableList<Train> t){
		trains = t;
	}
	public ObservableList<Integer> getCurveIdxs() {
		return curveIdxs;
	}
	public boolean isCurvable(int idx) {
		return idx!=0 && stations.size()-idx>2 && //端条件
				!curveIdxs.contains(idx-1) && !curveIdxs.contains(idx+1); //隣条件
	}
	public boolean getCurveConnection(int idx) {
		return curveIdxs.contains(idx);
	}
}

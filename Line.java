package RouteMapMaker;

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
	
	class Connection {
		Station station;
		BooleanProperty curve;
		Connection(Station s, boolean c) {
			station = s;
			curve = new SimpleBooleanProperty(c);
		}
		Connection(Station s) {
			station = s;
			curve = new SimpleBooleanProperty(false);
		}
	};
	
	private ObservableList<Connection> connections; //<駅，曲線接続> で駅同士の接続を保持．
	private ObservableList<Train> trains;//運転系統を保持する。
	private StringProperty lineName = new SimpleStringProperty();//路線名
	private BooleanProperty tategaki = new SimpleBooleanProperty(false);//縦書きか横書きか。trueなら縦書き
	private IntegerProperty nameStyle = new SimpleIntegerProperty(REGULAR);
	private IntegerProperty nameSize = new SimpleIntegerProperty(15);
	private IntegerProperty nameLocation = new SimpleIntegerProperty(BOTTOM);
	private ColorWrapper nameColor = new ColorWrapper(Color.BLACK);
	private IntegerProperty NameX = new SimpleIntegerProperty(0);
	private IntegerProperty NameY = new SimpleIntegerProperty(0);
	
	public Line(String name){//コンストラクタ
		connections = FXCollections.observableArrayList();
		trains = FXCollections.observableArrayList();
		setName(name);
		//始点と終点は確保しておく
		addStation(new Station(name + "始点"));
		addStation(new Station(name + "終点"));
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
	// ここで得られるListは編集可能ではないので注意
	public ObservableList<Station> getStations(){
		ObservableList<Station> staList = FXCollections.observableArrayList();
		connections.forEach(c -> staList.add(c.station));
		return staList;
	}
	public void setStations(ObservableList<Station> st){
		connections.clear();
		st.forEach(s -> connections.add(new Connection(s)));
	}
	public Connection insertStation(int idx, Station sta) {
		Connection c = new Connection(sta);
		connections.add(idx, c);
		return c;
	}
	public Connection addStation(Station sta) {
		Connection c = new Connection(sta);
		connections.add(c);
		return c;
	}
	public Connection removeStation(int idx) {
		return connections.remove(idx);
	}
	public ObservableList<Connection> getConnections() {
		return connections;
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
	public boolean isCurvable(int idx) {
		return idx>1 && connections.size()-idx>1 && //端条件
				connections.get(idx-1).station.isSet() && connections.get(idx).station.isSet() && //固定条件
				!connections.get(idx-1).curve.get() && !connections.get(idx+1).curve.get(); //連続条件
	}
	public boolean getCurveConnection(int idx) {
		return connections.get(idx).curve.get();
	}
}

package RouteMapMaker;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Station {//駅に関する情報を保持するクラス
	
	public static final int TEXT_YOKO_RIGHT = 10;
	public static final int TEXT_YOKO_LEFT = 9;
	public static final int TEXT_TATE_BOTTOM = 11;
	public static final int TEXT_TATE_TOP = 12;
	public static final int TEXT_UNSET = -11;
	
	public static final int REGULAR = 0;
	public static final int ITALIC = 1;
	public static final int BOLD = 2;
	public static final int BOLD_ITALIC = 3;
	public static final int STYLE_UNSET = 4;
	
	private StringProperty name = new SimpleStringProperty();//駅名
	private BooleanProperty pointSet = new SimpleBooleanProperty(false);//固定座標があるか否か
	private DoubleProperty x = new SimpleDoubleProperty(0.0);//固定座標x
	private DoubleProperty y = new SimpleDoubleProperty(0.0);//固定座標y
	private int stationConnection = 0;
	private IntegerProperty textMuki = new SimpleIntegerProperty(TEXT_UNSET);//駅ごとの縦書き横書き指定
	private IntegerProperty size = new SimpleIntegerProperty(0);//駅ごとに設定される文字サイズ。0は経路準拠
	private IntegerProperty style = new SimpleIntegerProperty(STYLE_UNSET);//駅ごとに設定される文字スタイル
	private IntegerProperty nameX = new SimpleIntegerProperty(0);//駅名の描画位置のズレ
	private IntegerProperty nameY = new SimpleIntegerProperty(0);
	private BooleanProperty shiftOnStation = new SimpleBooleanProperty(false);//描画位置修正を駅ごとの設定に従うか否か
	private double[] shiftCoor = new double[2];//mapDrawで使う一時保管用の変数。他の場所で使うなかれ。保存しない。
	private boolean isdrawn = false;//その駅名がすでに描画されたか。駅名多重描画の防止に使う。駅座標変換フラグにも使う。
	
	public Station(String name){
		setName(name);
	}
	
	public void setName(String name){
		this.name.set(name);
	}
	public StringProperty getNameProperty(){
		return this.name;
	}
	public String getName(){
		return name.get();
	}
	public void setPoint(double x, double y){
		this.x.set(x);
		this.y.set(y);
		pointSet.set(true);
	}
	public DoubleProperty[] getPointProperty(){
		DoubleProperty[] dpa = new DoubleProperty[2];
		dpa[0] = this.x;
		dpa[1] = this.y;
		return dpa;
	}
	public void erasePoint(){
		pointSet.set(false);
	}
	public BooleanProperty getPointSetProperty(){
		return this.pointSet;
	}
	public boolean isSet(){
		return pointSet.get();
	}
	public double[] getPoint(){
		if(pointSet.get()){
			double[] p = new double[2];
			p[0] = x.get();
			p[1] = y.get();
			return p;
		}else{
			throw new IllegalArgumentException();
		}
	}
	public void plusConnection(){
		stationConnection ++;
	}
	public void minusConnection(){
		stationConnection --;
	}
	public void setConnection(int c){
		stationConnection = c;
	}
	public int getConnection(){
		return stationConnection;
	}
	public void setInterPoint(double x, double y){
		pointSet.set(false);
		this.x.set(x);
		this.y.set(y);
	}
	public double[] getInterPoint(){
		if(pointSet.get()){
			throw new IllegalArgumentException();
		}else{
			double[] p = new double[2];
			p[0] = x.get();
			p[1] = y.get();
			return p;
		}
	}
	public void setMuki(int muki){
		this.textMuki.set(muki);
	}
	public IntegerProperty getMukiProperty(){
		return this.textMuki;
	}
	public int getMuki(){
		return textMuki.get();
	}
	public double[] getPointUS(){//isSetを考慮しません。使うのは全ての座標が決定した後にしましょう。
		double[] p = new double[2];
		p[0] = x.get();
		p[1] = y.get();
		return p;
	}
	public int getNameSize(){
		return size.get();
	}
	public IntegerProperty getNameSizeProperty(){
		return this.size;
	}
	public void setNameSize(int i){
		this.size.set(i);
	}
	public int getNameStyle(){
		return this.style.get();
	}
	public IntegerProperty getNameStyleProperty(){
		return this.style;
	}
	public void setNameStyle(int i){
		this.style.set(i);
	}
	public void setNameX(int i){
		this.nameX.set(i);
	}
	public void setNameY(int i){
		this.nameY.set(i);
	}
	public IntegerProperty getNameXProperty(){
		return this.nameX;
	}
	public IntegerProperty getNameYProperty(){
		return this.nameY;
	}
	public int[] getNameZure(){
		int[] ia = {nameX.get(), nameY.get()};
		return ia;
	}
	public boolean shiftBasedOnStation(){
		return this.shiftOnStation.get();
	}
	public BooleanProperty getShiftOnStationProperty(){
		return this.shiftOnStation;
	}
	public void setShiftBase(boolean b){
		this.shiftOnStation.set(b);
	}
	public double[] getShiftCoor(){
		return this.shiftCoor;
	}
	public void setShiftCoor(double[] ia){
		this.shiftCoor = ia;
	}
	public boolean isDrawn(){
		return this.isdrawn;
	}
	public void setDrawn(boolean b){
		this.isdrawn = b;
	}

}

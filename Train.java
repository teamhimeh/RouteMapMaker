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

public class Train implements Cloneable{
	public static final DoubleArrayWrapper NORMAL_LINE = new DoubleArrayWrapper(null);
	
	private ObservableList<TrainStop> stops;//この運転系統の駅オブジェクトを保持する。
	private StringProperty name = new SimpleStringProperty();//駅名
	private ColorWrapper lineColor = new ColorWrapper();
	private ColorWrapper markColor = new ColorWrapper();
	private ColorWrapper staColor = new ColorWrapper();
	private IntegerProperty lineWidth = new SimpleIntegerProperty();
	private IntegerProperty lineDistance = new SimpleIntegerProperty();
	private StopMark mark;
	private IntegerProperty markSize = new SimpleIntegerProperty();
	private IntegerProperty staSize = new SimpleIntegerProperty();
	private BooleanProperty tategaki = new SimpleBooleanProperty(true);//trueなら縦書き。
	private IntegerProperty edgeFixA = new SimpleIntegerProperty();//端の補正をどれだけするか。
	private IntegerProperty edgeFixB = new SimpleIntegerProperty();
	private DoubleArrayWrapper lineDash;//ラインの破線パターン。nullでただの線。
	
	public Train(String name){
		stops = FXCollections.observableArrayList();
		this.name.set(name);
		//初期設定
		lineColor.set(Color.BLACK);
		markColor.set(Color.WHITE);
		staColor.set(Color.BLACK);
		lineWidth.set(10);
		lineDistance.set(0);
		mark = StopMark.CIRCLE;
		markSize.set(8);
		tategaki.set(true);
		staSize.set(15);
		edgeFixA.set(0);
		edgeFixB.set(0);
		lineDash = NORMAL_LINE;
	}
	public ObservableList<TrainStop> getStops(){
		return stops;
	}
	public void setName(String n){
		name.set(n);
	}
	public StringProperty getNameProperty(){
		return this.name;
	}
	public String getName(){
		return name.get();
	}
	public void setLineColor(Color c){
		lineColor.set(c);
	}
	public ColorWrapper getLineColorProperty(){
		return this.lineColor;
	}
	public Color getLineColor(){
		return lineColor.get();
	}
	public void setMarkColor(Color c){
		markColor.set(c);
	}
	public ColorWrapper getMarkColorProperty(){
		return this.markColor;
	}
	public Color getMarkColor(){
		return markColor.get();
	}
	public void setStaColor(Color c){
		staColor.set(c);
	}
	public ColorWrapper getStaColorProperty(){
		return this.staColor;
	}
	public Color getStaColor(){
		return staColor.get();
	}
	public void setColorsInDouble(double[][] param){
		/*[A][B]
		 * A：0LineColor、1MarkColor、2StaColor
		 * B:0red、1green、2blue、3opacity
		 */
		lineColor.set(new Color(param[0][0],param[0][1],param[0][2],param[0][3]));
		markColor.set(new Color(param[1][0],param[1][1],param[1][2],param[1][3]));
		staColor.set(new Color(param[2][0],param[2][1],param[2][2],param[2][3]));
	}
	public double[][] getColorsInDouble(){
		double[][] cc = new double[3][4];
		cc[0][0] = lineColor.get().getRed();
		cc[0][1] = lineColor.get().getGreen();
		cc[0][2] = lineColor.get().getBlue();
		cc[0][3] = lineColor.get().getOpacity();
		cc[1][0] = markColor.get().getRed();
		cc[1][1] = markColor.get().getGreen();
		cc[1][2] = markColor.get().getBlue();
		cc[1][3] = markColor.get().getOpacity();
		cc[2][0] = staColor.get().getRed();
		cc[2][1] = staColor.get().getGreen();
		cc[2][2] = staColor.get().getBlue();
		cc[2][3] = staColor.get().getOpacity();
		return cc;
	}
	public int getLineWidth(){
		return lineWidth.get();
	}
	public IntegerProperty getLineWidthProperty(){
		return this.lineWidth;
	}
	public void setLineWidth(int d){
		lineWidth.set(d);
	}
	public int getLineDistance(){
		return lineDistance.get();
	}
	public IntegerProperty getLineDistanceProperty(){
		return this.lineDistance;
	}
	public void setLineDistance(int i){
		this.lineDistance.set(i);
	}
	public StopMark getMark(){
		return mark;
	}
	public void setMark(StopMark m){
		this.mark = m;
	}
	public int getMarkSize(){
		return markSize.get();
	}
	public IntegerProperty getMarkSizeProperty(){
		return this.markSize;
	}
	public void setMarkSize(int size){
		this.markSize.set(size);
	}
	public boolean isTategaki(){
		return tategaki.get();
	}
	public void setTategaki(boolean b){
		this.tategaki.set(b);
	}
	public int getStaSize(){
		return staSize.get();
	}
	public IntegerProperty getStaSizeProperty(){
		return staSize;
	}
	public void setStaSize(int i){
		this.staSize.set(i);
	}
	public int getEdgeA(){
		return this.edgeFixA.get();
	}
	public IntegerProperty getEdgeAProperty(){
		return this.edgeFixA;
	}
	public void setEdgeA(int i){
		this.edgeFixA.set(i);
	}
	public int getEdgeB(){
		return this.edgeFixB.get();
	}
	public IntegerProperty getEdgeBProperty(){
		return this.edgeFixB;
	}
	public void setEdgeB(int i){
		this.edgeFixB.set(i);
	}
	public DoubleArrayWrapper getLineDash(){
		return this.lineDash;
	}
	public void setLineDash(DoubleArrayWrapper d){
		this.lineDash = d;
	}
	@Override
	public Train clone(){
		Train t = null;
		try {
			t = (Train)super.clone();
			t.stops = FXCollections.observableArrayList();
			for(TrainStop ts: this.stops){
				t.stops.add(ts);
			}
			t.name = new SimpleStringProperty(this.name.get());
			t.lineColor = new ColorWrapper(this.lineColor.get());
			t.markColor = new ColorWrapper(this.markColor.get());
			t.staColor = new ColorWrapper(this.staColor.get());
			t.lineWidth = new SimpleIntegerProperty(this.lineWidth.get());
			t.lineDistance = new SimpleIntegerProperty(this.lineDistance.get());
			t.markSize = new SimpleIntegerProperty(this.markSize.get());
			t.staSize = new SimpleIntegerProperty(this.staSize.get());
			t.edgeFixA = new SimpleIntegerProperty(this.edgeFixA.get());
			t.edgeFixB = new SimpleIntegerProperty(this.edgeFixB.get());
			t.tategaki = new SimpleBooleanProperty(this.tategaki.get());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;
	}

}

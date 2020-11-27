package RouteMapMaker;

import java.util.ArrayList;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class MarkLayer implements Cloneable{//マーク編集における各レイヤーを保持するクラス。
	public static final int FILL = -1;//FILL属性
	public static final int STROKE = -2;//STROKE属性
	public static final int OVAL = 0;//楕円。円を含む。
	public static final int ARC = 1;//円弧。通常の円は楕円を使う。
	public static final int RECT = 2;//角の丸い長方形。ただの長方形含む。shapeとcanvasでパラメーターが全然違うので注意！
	public static final int POLYGON = 3;//多角形
	public static final int LINE = 4;//直線
	public static final int TEXT = 5;//文字列
	public static final int IMAGE = 6;//外部画像
	/*
	 * マークは基本図形の重ねあわせで実装する。レイヤーごとに一つのオブジェクトが用意される。
	 * shapeを使うかcanvasを使うかで実装を分ける必要がある。
	 * サポートする基本図形のパラメーターは以下のとおり
	 * OVAL：Color,paint,{左上X,左上Y,直径X,直径Y,lineWidth}
	 * ARC：Color,paint,{X,Y,幅,高さ,始角,角の大きさ,lineWidth,閉じタイプ（0:CHORD,1:OPEN,2:ROUND）
	 * RECT：Color,paint,{左上X,左上Y,幅,高さ,円弧幅,円弧高さ,lineWidth}
	 * POLYGON
	 * LINE：Color,paint,{始点X,始点Y,終点X,終点Y,lineWidth}
	 * TEXT：Color,paint,text,{X,Y,文字サイズ,lineWidth,文字タイプ}
	 * IMAGE：image,text,{左上X,左上Y,描画幅,描画高さ}
	 */
	private int type;
	private IntegerProperty paint = new SimpleIntegerProperty(FILL);//fillかstrokeか。デフォルトはfill。
	private ObservableList<DoubleProperty> params = FXCollections.observableArrayList();//パラメーターを保持
	private final boolean[] paramsProportion;//格納されているパラメーターはマークの大きさに依存するか。
	private StringProperty text = new SimpleStringProperty();//文字列だった場合にはtextを保持。IMAGEの場合は画像名
	private StringProperty fontName = new SimpleStringProperty();//文字列だった場合にfontNameを保持
	private FXImageWrapper imageWrapper = new FXImageWrapper();//IMAGEだった場合には内容を保持
	private ColorWrapper color = new ColorWrapper();//図形の色を保持
	
	//typeは始めに設定し、設定したらもう変更できない仕様にする。
	public MarkLayer(int type){
		this.type = type;
		switch(this.type){//パラメーターがマークサイズに比例するか否か
		case OVAL:
			boolean [] b = {true,true,true,true,true};
			paramsProportion = b;
			break;
		case ARC:
			boolean [] b1 = {true,true,true,true,false,false,true,false};
			paramsProportion = b1;
			break;
		case RECT:
			boolean[] b2 = {true,true,true,true,true,true,true};
			paramsProportion = b2;
			break;
		case LINE:
			boolean[] b5 = {true,true,true,true,true,false};
			paramsProportion = b5;
			break;
		case TEXT:
			boolean[] b3 = {true,true,true,true,false};
			paramsProportion = b3;
			break;
		case IMAGE:
			boolean[] b4 = {true,true,true,true};
			paramsProportion = b4;
			break;
		default:
			paramsProportion = null;
		}
	}
	public int getType(){
		return this.type;
	}
	public boolean[] getParamsProportion(){
		return this.paramsProportion;
	}
	public int getPaint(){
		return this.paint.get();
	}
	public IntegerProperty getPaintProperty(){
		return this.paint;
	}
	public void setPaint(int p){
		this.paint.set(p);
	}
	public ObservableList<DoubleProperty> getParamProperty(){
		return this.params;
	}
	public double getParam(int index){
		return this.params.get(index).get();
	}
	public void addParam(double d){
		this.params.add(new SimpleDoubleProperty(d));
	}
	public void setParam(int index, double d){
		this.params.get(index).set(d);
	}
	public String getText(){
		return this.text.get();
	}
	public StringProperty getTextProperty(){
		return this.text;
	}
	public void setText(String text){
		this.text.set(text);
	}
	public String getFontName(){
		return this.fontName.get();
	}
	public StringProperty getFontNameProperty(){
		return this.fontName;
	}
	public void setFontName(String name){
		this.fontName.set(name);
	}
	public Color getColor(){
		return this.color.get();
	}
	public ColorWrapper getColorProperty(){
		return this.color;
	}
	public void setColor(Color c){
		this.color.set(c);
	}
	public Image getImage(){
		return this.imageWrapper.get();
	}
	public FXImageWrapper getImageProperty(){
		return this.imageWrapper;
	}
	public void setImage(Image image){
		this.imageWrapper.set(image);
	}
	@Override
	public MarkLayer clone(){
		MarkLayer t = null;
		try {
			t = (MarkLayer)super.clone();
			//参照型の変数は全てクローンしてください。
			t.params = FXCollections.observableArrayList();
			for(int i = 0;i < this.params.size(); i++) t.params.add(new SimpleDoubleProperty(this.params.get(i).get()));
			t.paint = new SimpleIntegerProperty(this.paint.get());
			t.text = new SimpleStringProperty(this.text.get());
			t.fontName = new SimpleStringProperty(this.fontName.get());
			t.color = new ColorWrapper(this.color.get());
			t.imageWrapper = new FXImageWrapper(this.imageWrapper.get());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;
	}

}

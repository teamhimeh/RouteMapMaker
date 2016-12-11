package RouteMapMaker;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class FreeItem implements Cloneable{//路線図上に自由挿入できるアイテムを保持するクラス。テキストと画像をサポート
	public static final int TEXT = 0;
	public static final int IMAGE = 1;
	
	private IntegerProperty type = new SimpleIntegerProperty();
	private Image image;
	private String text;//IMAGEなら画像の名前、TEXTなら文字列を格納する
	private DoubleProperty[] params;
	private DoubleBinding[] dragArea = new DoubleBinding[4];//ドラッグ有効エリアを定義する。
	private Color color = Color.BLACK;
	private String font;//フォントファミリ名
	//<dragArea>0:左上X,1:左上Y,2:右下X,3:右下Y
	/*
	 * IMAGE：image,{左上X,左上Y,描画幅,描画高さ,回転}
	 * TEXT：Color,text,{X,Y,文字サイズ,lineWidth,回転,文字style,strokeOrFill(0:Fill,1:Stroke),縦書き横書き(0:横)},FontName
	 */
	public FreeItem(int type){//一度タイプを設定したら変更できない仕様
		this.type.set(type);
		if(this.type.get() == TEXT){
			params = new DoubleProperty[8];
			for(int i = 0; i < 8; i++){
				params[i] = new SimpleDoubleProperty(0.0);
			}
		}else if(this.type.get() == IMAGE){
			params = new DoubleProperty[5];
			for(int i = 0; i < 5; i++){
				params[i] = new SimpleDoubleProperty(0.0);
			}
			//以下、dragAreaのBinding処理
			dragArea[0] = Bindings.add(0.0, params[0]);//そのまんま代入するていうのができないので0加算でごまかす
			dragArea[1] = Bindings.add(0.0, params[1]);
			dragArea[2] = (DoubleBinding) Bindings.add(params[0], params[2]);
			dragArea[3] = (DoubleBinding) Bindings.add(params[1], params[3]);
		}else{
			throw new IllegalArgumentException("typeがTEXTでもIMAGEでもありません！");
		}
	}
	public int getType(){
		return this.type.get();
	}
	public DoubleProperty[] getParams(){
		return this.params;
	}
	public void setImage(Image image){
		this.image = image;
	}
	public Image getImage(){
		return this.image;
	}
	public void setText(String text){
		this.text = text;
	}
	public String getText(){
		return this.text;
	}
	public void setColor(Color c){
		this.color = c;
	}
	public Color getColor(){
		return this.color;
	}
	public void setFontName(String n){
		this.font = n;
	}
	public String getFontName(){
		return this.font;
	}
	public DoubleBinding[] getDragArea(){
		return this.dragArea;
	}
	
	@Override
	public FreeItem clone(){
		FreeItem f = null;
		try {
			f = (FreeItem) super.clone();
			f.type = new SimpleIntegerProperty(this.getType());
			//imageはそのまま参照させる。
			f.params = new DoubleProperty[this.getParams().length];
			for(int i = 0; i < this.getParams().length; i++){
				f.params[i] = new SimpleDoubleProperty(this.getParams()[i].get());
			}
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}
}

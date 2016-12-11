package RouteMapMaker;

import javafx.scene.paint.Color;

public class ColorWrapper {//javafxのColorをラップするだけ。こうすることでredo/undoに対応する。
	private Color color = Color.BLACK;
	public ColorWrapper(){
		
	}
	public ColorWrapper(Color c){
		this.color = c;
	}
	public Color get(){
		return this.color;
	}
	public void set(Color c){
		this.color = c;
	}

}

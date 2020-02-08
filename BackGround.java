package RouteMapMaker;

import java.util.HashMap;
import java.util.Properties;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class BackGround implements Cloneable{
	// Objectを複製してUndo/Redoを管理
	Color color;
	Image image;
	int x, y; // 原点座標
	int zoomRatio, opacity; // %単位
	
	public BackGround() {
		color = Color.WHITESMOKE;
		image = null;
		x = y = opacity = 0;
		zoomRatio = 100;
	}
	
	@Override
	public BackGround clone() {
		BackGround c = new BackGround();
		c.copyParams(this);
		return c;
	}
	
	public void copyParams(BackGround ref) {
		this.color = ref.color;
		this.image = ref.image;
		this.x = ref.x;
		this.y = ref.y;
		this.zoomRatio = ref.zoomRatio;
		this.opacity = ref.opacity;
	}
	
	public void setColor(Color c) {
		image = null;
		color = c;
	}
	
	public void read(Properties p, HashMap<Integer,Image> imageMap) {
		
	}
	
	public void save(Properties p, HashMap<Integer,Image> imageMap) {
		
	}
}

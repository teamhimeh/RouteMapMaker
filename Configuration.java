package RouteMapMaker;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javafx.scene.paint.Color;

public class Configuration {//ソフトウェアの環境設定を保持するクラス。
	//R_は路線編集モードで有効な項目。S_は運転系統編集モードで有効な項目
	private boolean R_grid = true;//グリッドを表示するか
	private int R_gridInterval = 20;
	private boolean R_bindToGridX = true;
	private boolean R_bindToGridY = true;
	private Color fixedColor = Color.CORNFLOWERBLUE;
	private Color nonFixedColor = Color.BLACK;
	private boolean triangleGrid = false;
	
	private boolean menubarMode = true;
	
	private boolean no_alert = false;//起動時使用上の注意を表示しない
	
	public boolean getR_grid(){return this.R_grid;}
	public void setR_grid(boolean b){this.R_grid = b;}
	public int getR_gridInterval(){return this.R_gridInterval;}
	public void setR_gridInterval(int b){this.R_gridInterval = b;}
	public boolean getR_bindToGridX(){return this.R_bindToGridX;}
	public void setR_bindToGridX(boolean b){this.R_bindToGridX = b;}
	public boolean getR_bindToGridY(){return this.R_bindToGridY;}
	public void setR_bindToGridY(boolean b){this.R_bindToGridY = b;}
	public Color getFixedColor(){return this.fixedColor;}
	public void setFixedColor(Color c){this.fixedColor = c;}
	public Color getNonFixedColor(){return this.nonFixedColor;}
	public void setNonFixedColor(Color c){this.nonFixedColor = c;}
	public boolean getMenubarMode(){return this.menubarMode;}
	public void setMenubarMode(boolean b){this.menubarMode = b;}
	public boolean getNoAlert(){return this.no_alert;}
	public void setNoAlert(boolean b){this.no_alert = b;}
	public boolean isGridTriangle() {return this.triangleGrid;}
	public void setGridTriangle(boolean b) {this.triangleGrid = b;}
	
	public void read(){
		File file = new File("config.properties");
		if(file.exists()){
			Properties p = new Properties();
			try {
				FileReader fr = new FileReader(file);
				p.load(fr);
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//以下各項目の設定
			R_grid = Boolean.valueOf(p.getProperty("R_grid", "true"));
			R_gridInterval = Integer.valueOf(p.getProperty("R_gridInterval", "20"));
			R_bindToGridX = Boolean.valueOf(p.getProperty("R_bindToGridX", "true"));
			R_bindToGridY = Boolean.valueOf(p.getProperty("R_bindToGridY", "true"));
			fixedColor = Color.valueOf(p.getProperty("fixedColor",Color.CORNFLOWERBLUE.toString()));
			nonFixedColor = Color.valueOf(p.getProperty("nonFixedColor",Color.BLACK.toString()));
			menubarMode = Boolean.valueOf(p.getProperty("menubarMode", "true"));
			no_alert = Boolean.valueOf(p.getProperty("no_alert", "false"));
			triangleGrid = Boolean.valueOf(p.getProperty("triangleGrid", "false"));
		}
	}
	public void save(){
		try {
			Properties p = new Properties();
			p.setProperty("R_grid", String.valueOf(R_grid));
			p.setProperty("R_gridInterval", String.valueOf(R_gridInterval));
			p.setProperty("R_bindToGridX", String.valueOf(R_bindToGridX));
			p.setProperty("R_bindToGridY", String.valueOf(R_bindToGridY));
			p.setProperty("menubarMode", String.valueOf(menubarMode));
			p.setProperty("fixedColor", fixedColor.toString());
			p.setProperty("nonFixedColor", nonFixedColor.toString());
			p.setProperty("no_alert", String.valueOf(no_alert));
			p.setProperty("triangleGrid", String.valueOf(triangleGrid));
			FileWriter fw = new FileWriter("config.properties");
			p.store(fw, null);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

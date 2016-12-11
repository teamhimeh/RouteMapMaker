package RouteMapMaker;

import javafx.scene.image.Image;

public class FXImageWrapper {
	private Image image;
	
	public FXImageWrapper(){
		
	}
	public FXImageWrapper(Image im){
		this.image = im;
	}
	public Image get(){
		return this.image;
	}
	public void set(Image im){
		this.image = im;
	}
}

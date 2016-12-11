package RouteMapMaker;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

public class FreeItemCell extends ListCell<FreeItem> implements Callback<ListView<FreeItem>, ListCell<FreeItem>>{

	@Override
	public ListCell<FreeItem> call(ListView<FreeItem> param) {
		// TODO Auto-generated method stub
		return new FreeItemCell(){
			@Override
			protected void updateItem(FreeItem item, boolean empty){
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
				} else {
					if(item.getType() == FreeItem.IMAGE){
						setText("[画像]"+item.getText());
					}
					if(item.getType() == FreeItem.TEXT){
						//属性テキストの時の処理をここに書く。
						setText("[text]"+item.getText());
					}
				}
			}
		};
	}

}

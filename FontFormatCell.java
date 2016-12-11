package RouteMapMaker;

import javafx.scene.control.ListCell;
import javafx.scene.text.Font;

//情報としてStringを渡してください。
public class FontFormatCell extends ListCell<String> {
	@Override
	protected void updateItem(String item, boolean empty){
		super.updateItem(item, empty);
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
		} else {
			setFont(Font.font(item));
			setText(item);
		}
	}

}

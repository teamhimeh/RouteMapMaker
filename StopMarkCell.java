package RouteMapMaker;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Callback;

public class StopMarkCell extends ListCell<StopMark> implements Callback<ListView<StopMark>, ListCell<StopMark>>{
	final double prevSize = 20;

	@Override
	public ListCell<StopMark> call(ListView<StopMark> param) {
		// TODO Auto-generated method stub
		return new StopMarkCell(){
			@Override
			protected void updateItem(StopMark item, boolean empty){
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
				} else {
					//実際のcellでの描画処理はここ。
					if(item == StopMark.OBEY_LINE){
						setGraphic(null);
						setText("経路準拠");
					}else if(item == StopMark.NO_DRAW){
						setGraphic(null);
						setText("非表示");
					}else if(item == StopMark.CIRCLE){
						setText(null);
						Circle circle = new Circle();
						circle.setCenterX(7);
						circle.setCenterY(7);
						circle.setRadius(5);
						circle.setFill(Color.BLACK);
						setGraphic(circle);
					}else{
						if(item.getLayers().size() == 0){//まだ中身の無い空マークだった場合
							setText("空のマーク");
						}else{
							Pane p = new Pane();
							p.setPrefSize(prevSize, prevSize);
							//ここから、実際の描画処理
							Canvas canvas = new Canvas(prevSize,prevSize);
							GraphicsContext gc = canvas.getGraphicsContext2D();
							CustomMarkController.markDraw(gc, item, prevSize);
							p.getChildren().add(canvas);
							setText(null);
							setGraphic(p);
						}
					}
				}
			}
		};
	}

}

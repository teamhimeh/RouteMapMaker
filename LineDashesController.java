package RouteMapMaker;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;

public class LineDashesController implements Initializable{

	@FXML ListView<DoubleArrayWrapper> list;
	@FXML Canvas canvas;
	@FXML Label label;
	@FXML TextField textField;
	@FXML Button add;
	@FXML Button delete;
	ObservableList<DoubleArrayWrapper> lineDashes;
	LineDashCell ldCell;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setLineWidth(3);
		gc.setStroke(Color.BLACK);
		ldCell = new LineDashCell();
		list.setCellFactory(ldCell);
		list.getSelectionModel().selectedItemProperty().addListener((ov, oldVal, newVal) ->{
			DoubleArrayWrapper daw = list.getSelectionModel().getSelectedItem();
			if(daw == null){
				textField.setDisable(true);
				label.setText("左のリストから選択してください");
				gc.clearRect(0, 0, 200, 20);
				delete.setDisable(true);
			}else if(daw == Train.NORMAL_LINE){
				textField.setDisable(true);
				label.setText("この項目は編集できません。");
				gc.clearRect(0, 0, 200, 20);
				gc.setLineDashes(daw.get());
				gc.strokeLine(0, 10, 200, 10);
				delete.setDisable(true);
			}else{
				textField.setDisable(false);
				label.setText("半角でコードを入力");
				setText(daw);
				gc.clearRect(0, 0, 200, 20);
				gc.setLineDashes(daw.get());
				gc.strokeLine(0, 10, 200, 10);
				delete.setDisable(false);
			}
		});
		add.setOnAction((ActionEvent) ->{
			int selectedIndex = list.getSelectionModel().getSelectedIndex();
			double[] da = {10d,10d};
			if(selectedIndex == -1){
				lineDashes.add(new DoubleArrayWrapper(da));//最後に追加
				list.getSelectionModel().selectLast();
			}else{//直後に追加する
				lineDashes.add(selectedIndex + 1,new DoubleArrayWrapper(da));
				list.getSelectionModel().select(selectedIndex + 1);
			}
		});
		delete.setOnAction((ActionEvent) ->{
			int selectedIndex = list.getSelectionModel().getSelectedIndex();
			if(selectedIndex == -1){
				Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("項目を選択してください。");
				alert.showAndWait();
			}else if(selectedIndex == 0){
				Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("直線パターンは削除できません。");
				alert.showAndWait();
			}else{
				lineDashes.remove(selectedIndex);
				list.getSelectionModel().select(selectedIndex - 1);
			}
		});
		textField.setOnAction((ActionEvent) ->{
			String text = textField.getText();
			//以下テキスト解析
			String[] split = text.split(",",0);
			DoubleArrayWrapper daw = list.getSelectionModel().getSelectedItem();
			if(daw == null){
				Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
				alert.getDialogPane().setContentText("項目を選択してください");
				alert.showAndWait();
			}else{
				if(split.length == 0 || split.length == 1){
					Alert alert = new Alert(AlertType.WARNING,"",ButtonType.CLOSE);
					alert.getDialogPane().setContentText("破線パターンを表す要素数は2つ以上の必要があります。");
					alert.showAndWait();
					setText(daw);
				}else{
					double[] da = new double[split.length];
					boolean error = false;
					for(int i = 0; i < split.length; i++){
						try{
							da[i] = Double.valueOf(split[i]);
							if(da[i] < 1){
								Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
								alert.getDialogPane().setContentText("要素として使用できるのは1以上の半角数字のみです。");
								alert.showAndWait();
								error = true;
								break;
							}
						}catch(NumberFormatException e){
							error = true;
							Alert alert = new Alert(AlertType.ERROR,"",ButtonType.CLOSE);
							alert.getDialogPane().setContentText("不適切な文字が使用されています。使用できるのは1以上の半角数字と区切りカンマのみです。");
							alert.showAndWait();
						}
					}
					if(! error) daw.set(da);
					setText(daw);
					gc.clearRect(0, 0, 200, 20);
					gc.setLineDashes(daw.get());
					gc.strokeLine(0, 10, 200, 10);
					list.setItems(null);
					list.setItems(lineDashes);
					list.getSelectionModel().select(daw);
				}
			}
		});
	}
	public void setObject(ObservableList<DoubleArrayWrapper> lineDashes){
		this.lineDashes = lineDashes;
		list.setItems(lineDashes);
	}
	private void setText(DoubleArrayWrapper daw){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < daw.get().length - 1; i++){
			sb.append(String.valueOf((int)daw.get()[i]) + ",");
		}
		sb.append(String.valueOf((int)daw.get()[daw.get().length - 1]));
		textField.setText(sb.toString());
	}

}

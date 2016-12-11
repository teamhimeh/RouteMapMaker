package RouteMapMaker;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class selectFontController implements Initializable{

	@FXML Button saveBT;
	@FXML Button cancelBT;
	@FXML Button defaultFont;
	@FXML Label sampleText;
	@FXML ListView<String> FontList;
	private Stage stage;//このstageを保持する
	private int fontIndex;//今どれが選択されているか
	private int defaultIndex;//デフォルトのフォントのindexを保持する
	private boolean saved;//戻る時に変更を反映するかしないか
	private ObservableList<String> fn = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		for(int i = 0; i < Font.getFamilies().size(); i++){//デフォルトフォントを探索
			if(Font.getFamilies().get(i).equals("System")) defaultIndex = i;
		}
		for(int i = 0; i < Font.getFamilies().size(); i++){
			fn.add(Font.getFamilies().get(i));
		}
		FontList.setItems(fn);
		FontList.setCellFactory((ListView<String> l) -> new FontFormatCell());
		FontList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			fontIndex = FontList.getSelectionModel().getSelectedIndex();
			sampleText.setFont(Font.font(Font.getFamilies().get(fontIndex)));
		});
		defaultFont.setOnAction((ActionEvent) ->{
			fontIndex = defaultIndex;
			FontList.getSelectionModel().select(fontIndex);
			sampleText.setFont(Font.font(Font.getFamilies().get(fontIndex)));
		});
		cancelBT.setOnAction((ActionEvent) ->{
			saved = false;
			stage.close();
		});
		saveBT.setOnAction((ActionEvent) ->{
			saved = true;
			stage.close();
		});
	}
	public void setObject(Stage stage, String currentFont){
		this.stage = stage;
		if(currentFont == null){//nullのときはSystemを指定します。
			fontIndex = defaultIndex;
		}else{
			boolean found = false;
			for(int i = 0; i < Font.getFamilies().size(); i++){
				if(Font.getFamilies().get(i).equals(currentFont)){
					fontIndex = i;
					found = true;
				}
			}
			if(! found){
				fontIndex = defaultIndex;
			}
		}
		FontList.getSelectionModel().select(fontIndex);
	}
	public boolean shouldSave(){
		return saved;
	}
	public String getFontName(){
		if(saved){
			return Font.getFamilies().get(fontIndex);
		}else{
			throw new IllegalArgumentException("shouldSaveがfalseです。フォント名は渡せません");
		}
	}

}

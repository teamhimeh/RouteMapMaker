package RouteMapMaker;

import javafx.event.EventHandler;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class IntegerSpinnerEventHandler implements EventHandler<KeyEvent> {
	Spinner<Integer> spinner;
	int val = 0;
	// constructor実行時のSpinnerの値を初期値とするので，このHandlerはSpinnerValueFactoryを設定後に生成すること
	IntegerSpinnerEventHandler(Spinner<Integer> s) {
		this.spinner = s;
		this.val = s.getValue();
	}

	@Override
	public void handle(KeyEvent event) {
		if(event.getCode() != KeyCode.ENTER) {
			//Enterが押されたときだけvaridationを行う
			return;
		}
		try {
			val = Integer.parseInt(spinner.getEditor().textProperty().get());
		}catch(NumberFormatException e) {
			spinner.getEditor().textProperty().set(String.valueOf(val));
		}
	}
}

package RouteMapMaker;

import java.util.ArrayDeque;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class URElements {//undo_redoのための情報を保持する。
	
	protected enum Type{
		BOOLEAN,
		INTEGER,
		DOUBLE,
		STRING,
		COLOR,
		ARRAY,
		SUBCLASS
	}
	public enum ArrayCommands{
		ADD,
		SET,
		REMOVE,
		UP,
		DOWN
	}

	protected ObservableList<BooleanProperty> BooleanPropertyList = FXCollections.observableArrayList();
	protected ObservableList<Boolean> BooleanValueList = FXCollections.observableArrayList();
	protected ObservableList<IntegerProperty> IntegerPropertyList = FXCollections.observableArrayList();
	protected ObservableList<Integer> IntegerOldValueList = FXCollections.observableArrayList();
	protected ObservableList<Integer> IntegerNewValueList = FXCollections.observableArrayList();
	protected ObservableList<DoubleProperty> DoublePropertyList = FXCollections.observableArrayList();
	protected ObservableList<Double> DoubleOldValueList = FXCollections.observableArrayList();
	protected ObservableList<Double> DoubleNewValueList = FXCollections.observableArrayList();
	protected ObservableList<StringProperty> StringPropertyList = FXCollections.observableArrayList();
	protected ObservableList<String> StringOldValueList = FXCollections.observableArrayList();
	protected ObservableList<String> StringNewValueList = FXCollections.observableArrayList();
	protected ObservableList<ColorWrapper> ColorPropertyList = FXCollections.observableArrayList();
	protected ObservableList<Color> ColorOldValueList = FXCollections.observableArrayList();
	protected ObservableList<Color> ColorNewValueList = FXCollections.observableArrayList();
	protected ObservableList<ObservableList> ArrayPropertyList = FXCollections.observableArrayList();
	protected ObservableList ArrayValueList = FXCollections.observableArrayList();
	protected ObservableList ArrayValue2List = FXCollections.observableArrayList();
	protected ObservableList<Integer> ArrayIndexList = FXCollections.observableArrayList();
	protected ObservableList<ArrayCommands> ArrayCommandList = FXCollections.observableArrayList();
	protected ArrayDeque<Type> undoTypeStack = new ArrayDeque<Type>();//どのListに所属しているか(undo)
	protected ArrayDeque<Integer> undoIndexStack = new ArrayDeque<Integer>();//Listの何番か(undo)
	protected ArrayDeque<Type> redoTypeStack = new ArrayDeque<Type>();//どのListに所属しているか(redo)
	protected ArrayDeque<Integer> redoIndexStack = new ArrayDeque<Integer>();//Listの何番か(redo)
	protected BooleanProperty undoable = new SimpleBooleanProperty(false);//undoできるか否か
	protected BooleanProperty redoable = new SimpleBooleanProperty(false);//redoできるか否か
	protected Type type;//サブクラスからの参照用。undo/redoStackから取り出した命令のtype
	protected int index;//サブクラスからの参照用。undo/redoStackから取り出した命令のindex
	protected int prevUndoStackSize = 0; //前回のセーブ時のUndoTypeStackのサイズ．終了の警告に使う
	
	public void addObserve(IntegerProperty... iops){//監視対象を追加する。
		for(IntegerProperty iop: iops){
			iop.addListener((ov,oldVal,newVal) ->{//値が変更されたら自動的にpushするようにする。
				push(iop,oldVal.intValue(),newVal.intValue());
			});
		}
	}
	public void addObserve(DoubleProperty... dops){
		for(DoubleProperty dop: dops){
			dop.addListener((ov,oldVal,newVal) ->{
				push(dop,oldVal.doubleValue(),newVal.doubleValue());
			});
		}
	}
	public void addObserve(StringProperty... sops){
		for(StringProperty sop: sops){
			sop.addListener((ov,oldVal,newVal) ->{
				push(sop,oldVal,newVal);
			});
		}
	}
	public void push(BooleanProperty bp, boolean newVal){
		undoTypeStack.push(Type.BOOLEAN);
		undoIndexStack.push(BooleanPropertyList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		BooleanPropertyList.add(bp);
		BooleanValueList.add(newVal);
		undoable.set(true);
		redoable.set(false);
	}
	public void push(IntegerProperty ip, int oldVal, int newVal){
		undoTypeStack.push(Type.INTEGER);
		undoIndexStack.push(IntegerPropertyList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		IntegerPropertyList.add(ip);
		IntegerOldValueList.add(oldVal);
		IntegerNewValueList.add(newVal);
		undoable.set(true);
		redoable.set(false);
	}
	public void push(DoubleProperty dp, double oldVal, double newVal){
		undoTypeStack.push(Type.DOUBLE);
		undoIndexStack.push(DoublePropertyList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		DoublePropertyList.add(dp);
		DoubleOldValueList.add(oldVal);
		DoubleNewValueList.add(newVal);
		undoable.set(true);
		redoable.set(false);
	}
	public void push(StringProperty sp, String oldVal, String newVal){
		undoTypeStack.push(Type.STRING);
		undoIndexStack.push(StringPropertyList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		StringPropertyList.add(sp);
		StringOldValueList.add(oldVal);
		StringNewValueList.add(newVal);
		undoable.set(true);
		redoable.set(false);
	}
	public void push(ColorWrapper cw, Color oldVal, Color newVal){
		undoTypeStack.push(Type.COLOR);
		undoIndexStack.push(ColorPropertyList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		ColorPropertyList.add(cw);
		ColorOldValueList.add(oldVal);
		ColorNewValueList.add(newVal);
		undoable.set(true);
		redoable.set(false);
	}
	public <T> void push(ObservableList<T> nl, ArrayCommands cmd, int index, T value, T value2){//setはこっち
		undoTypeStack.push(Type.ARRAY);
		undoIndexStack.push(ArrayPropertyList.size());
		redoTypeStack.clear();
		redoIndexStack.clear();
		ArrayPropertyList.add(nl);
		ArrayCommandList.add(cmd);
		ArrayIndexList.add(index);
		ArrayValueList.add(value);
		ArrayValue2List.add(value2);
		undoable.set(true);
		redoable.set(false);
	}
	/*
	 * add:valueは追加するアイテム
	 * remove:valueは取り除くアイテム
	 * up:indexは入れ替える下側インデックス。valueはnullを渡す
	 * down:indexは入れ替える上側インデックス。valueはnullを渡す。
	 * set:valueは置き換えられるアイテム。value2は新しくsetするアイテム。
	 */
	public <T> void push(ObservableList<T> nl, ArrayCommands cmd, int index, T value){//add,remove,up,downはこっち
		push(nl,cmd,index,value,null);
	}
	
	public void undo(){
		type = undoTypeStack.pop();
		if(type == null){//undoStackが空の時
			throw new IllegalArgumentException("UndoStackに値がありません！");
		}else{
			index = undoIndexStack.pop();
			redoTypeStack.push(type);//undoスタックから取り出したらredoスタックに入れてしまう
			redoIndexStack.push(index);
			redoable.set(true);
			if(undoTypeStack.size() == 0) undoable.set(false);
			//値代入処理
			switch(type){
			case INTEGER:
				IntegerPropertyList.get(index).set(IntegerOldValueList.get(index));
				break;
			case DOUBLE:
				DoublePropertyList.get(index).set(DoubleOldValueList.get(index));
				break;
			case STRING:
				StringPropertyList.get(index).set(StringOldValueList.get(index));
				break;
			case COLOR:
				ColorPropertyList.get(index).set(ColorOldValueList.get(index));
				break;
			case ARRAY:
				ArrayCommands cmd = ArrayCommandList.get(index);
				switch(cmd){
				case ADD://追加の反対はremove
					ArrayPropertyList.get(index).remove(ArrayIndexList.get(index).intValue());
					break;
				case SET://setは前の値を代入する
					ArrayPropertyList.get(index).set(ArrayIndexList.get(index).intValue(), ArrayValueList.get(index));
					break;
				case REMOVE://削除の反対は追加
					ArrayPropertyList.get(index).add(ArrayIndexList.get(index).intValue(), ArrayValueList.get(index));
					break;
				case UP:
					Object o3 = ArrayPropertyList.get(index).get(ArrayIndexList.get(index).intValue());
					Object o4 = ArrayPropertyList.get(index).get(ArrayIndexList.get(index).intValue() - 1);
					ArrayPropertyList.get(index).set(ArrayIndexList.get(index).intValue(), o4);
					ArrayPropertyList.get(index).set(ArrayIndexList.get(index).intValue() - 1, o3);
					break;
				case DOWN:
					ObservableList ol = ArrayPropertyList.get(index);
					Object o1 = ol.get(ArrayIndexList.get(index).intValue());
					Object o2 = ol.get(ArrayIndexList.get(index).intValue() + 1);
					ol.set(ArrayIndexList.get(index).intValue(), o2);
					ol.set(ArrayIndexList.get(index).intValue() + 1, o1);
					break;
				}
				break;
			}
		}
	}
	public void redo(){
		type = redoTypeStack.pop();
		if(type == null){
			throw new IllegalArgumentException("RedoStackに値がありません！");
		}else{
			index = redoIndexStack.pop();
			undoTypeStack.push(type);//redoスタックから取り出したらundoスタックに入れてしまう
			undoIndexStack.push(index);
			undoable.set(true);
			if(redoTypeStack.size() == 0) redoable.set(false);
			//値代入処理
			switch(type){
			case INTEGER:
				IntegerPropertyList.get(index).set(IntegerNewValueList.get(index));
				break;
			case DOUBLE:
				DoublePropertyList.get(index).set(DoubleNewValueList.get(index));
				break;
			case STRING:
				StringPropertyList.get(index).set(StringNewValueList.get(index));
				break;
			case COLOR:
				ColorPropertyList.get(index).set(ColorNewValueList.get(index));
				break;
			case ARRAY:
				ArrayCommands cmd = ArrayCommandList.get(index);
				switch(cmd){
				case ADD:
					ArrayPropertyList.get(index).add(ArrayIndexList.get(index).intValue(), ArrayValueList.get(index));
					break;
				case SET://setのredoはArrayValue2Listを使う
					ArrayPropertyList.get(index).set(ArrayIndexList.get(index).intValue(), ArrayValue2List.get(index));
					break;
				case REMOVE:
					ArrayPropertyList.get(index).remove(ArrayIndexList.get(index).intValue());
					break;
				case UP:
					Object o3 = ArrayPropertyList.get(index).get(ArrayIndexList.get(index).intValue());
					Object o4 = ArrayPropertyList.get(index).get(ArrayIndexList.get(index).intValue() - 1);
					ArrayPropertyList.get(index).set(ArrayIndexList.get(index).intValue(), o4);
					ArrayPropertyList.get(index).set(ArrayIndexList.get(index).intValue() - 1, o3);
					break;
				case DOWN:
					ObservableList ol = ArrayPropertyList.get(index);
					Object o1 = ol.get(ArrayIndexList.get(index).intValue());
					Object o2 = ol.get(ArrayIndexList.get(index).intValue() + 1);
					ol.set(ArrayIndexList.get(index).intValue(), o2);
					ol.set(ArrayIndexList.get(index).intValue() + 1, o1);
					break;
				}
				break;
			}
		}
	}
	public BooleanProperty getUndoableProperty(){
		return this.undoable;
	}
	public BooleanProperty getRedoableProperty(){
		return this.redoable;
	}
	public void clear(){//蓄積されたundo/redoをクリアする。
		undoTypeStack = new ArrayDeque<Type>();//どのListに所属しているか(undo)
		undoIndexStack = new ArrayDeque<Integer>();//Listの何番か(undo)
		redoTypeStack = new ArrayDeque<Type>();//どのListに所属しているか(redo)
		redoIndexStack = new ArrayDeque<Integer>();//Listの何番か(redo)
		undoable.set(false);
		redoable.set(false);
		prevUndoStackSize = 0;
	}
	public void saveUndoStackSize() {
		prevUndoStackSize = undoTypeStack.size();
	}
	public boolean isSaveNeeded() {
		return undoTypeStack.size() != prevUndoStackSize;
	}
	public int getStackCount() {
		return undoTypeStack.size();
	}
}

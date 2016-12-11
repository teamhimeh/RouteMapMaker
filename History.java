package RouteMapMaker;

import java.time.ZonedDateTime;

public class History {//バージョンヒストリ機能で各ヒストリを保持する。
	private ZonedDateTime date;//保存された日時
	private int fileNum;//実際のファイル番号。ファイル名はi.histといった具合になる。ファイル本体の保持はおこなわない。
	
	public History(ZonedDateTime date, int fileNum){
		this.date = date;
		this.fileNum = fileNum;
	}
	public History(String dateString, int fileNum){
		this.date = ZonedDateTime.parse(dateString);
		this.fileNum = fileNum;
	}
	public ZonedDateTime getZoneDateTime(){
		return this.date;
	}
	public int getFileNum(){
		return this.fileNum;
	}
	public String getDateString(){//zone情報アリの文字列表現を返す
		return this.date.toString();
	}
	public String getDateStringWithoutZone(){//zone情報ナシの文字列表現を返す
		return this.date.toLocalDateTime().toString();
	}
}

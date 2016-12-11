package RouteMapMaker;

public class MvSta {//駅座標の移動に関する情報を保持するクラス
	Station sta;
	double[] start = new double[2];
	boolean isSet;//最初固定点だったか非固定点だったか
	MvSta(Station sta){
		this.sta = sta;
		this.start[0] = sta.getPointUS()[0];//後からの値の変更を防ぐ
		this.start[1] = sta.getPointUS()[1];
		this.isSet = sta.isSet();
	}
}

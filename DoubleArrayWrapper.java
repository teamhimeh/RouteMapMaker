package RouteMapMaker;

public class DoubleArrayWrapper {
	private double[] da;
	DoubleArrayWrapper(){}
	DoubleArrayWrapper(double[] d){
		da = d;
	}
	public void set(double[] d){
		da = d;
	}
	public double[] get(){
		return da;
	}
}

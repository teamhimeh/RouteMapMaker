package RouteMapMaker;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class ErrorReporter {
	
	private static ErrorReporter er = new ErrorReporter();
	private String reportUrl;
	private double version;
	private static boolean debug = false;
	
	public static boolean isDebugBuild() {
		return debug;
	}
	
	public static void setReportURL(String s, double version) {
		er.reportUrl = s;
		er.version = version;
	}
	
	public static void report(Throwable e) {
		if(er.reportUrl==null) {
			return;
		}
		System.out.println("send error report to " + er.reportUrl);
		try {
			URL url = new URL(er.reportUrl);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "text/plain");
			StringBuilder sb = new StringBuilder("version: " + String.valueOf(er.version) + "\n"); //バージョン
			sb.append(e.toString() + "\n"); //エラーメッセージ
			Arrays.asList(e.getStackTrace()).forEach(st -> sb.append(st.toString() + "\n"));
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			out.write(sb.toString());
			out.close();
			conn.connect();
			conn.getInputStream(); //これをもってHTTP接続が行われる．
		} catch(IOException ioe) {
			// stacktraceの送信だけなので特にエラー処理はしない．
			ioe.printStackTrace();
		}
	}
}

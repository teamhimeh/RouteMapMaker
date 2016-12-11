package RouteMapMaker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

//javaSE7以降の標準APIでzipの書き込み、読み出しをサポートするメソッド。エンコーディングはUTF-8。
//単一階層のみサポートします。主にデータのラップ用
//writeZip：fileのArrayListを渡してzipFileを作る。
//readZip：zipFileを渡してfileのArrayListを返す
public class HandleZip {
	public static void writeZip(File zipFile, ArrayList<File> files) throws IOException{
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)), 
				Charset.forName("UTF-8"));
		byte[] buf = new byte[1024];
		InputStream is = null;
		try{
			for(File file: files){
				ZipEntry entry = new ZipEntry(file.getName());
				zos.putNextEntry(entry);
				is = new BufferedInputStream(new FileInputStream(file));
				int len = 0;
				while ((len = is.read(buf)) != -1) {
					zos.write(buf, 0, len);
				}
				zos.closeEntry();
			}
		} finally{
			is.close();
		}
		zos.close();
	}
	public static ArrayList<File> readZip(File zipFile) throws IOException{
		ArrayList<File> files = new ArrayList<File>();
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)),
				Charset.forName("UTF-8"));
		OutputStream os = null;
		try{
			ZipEntry entry = null;
			while(( entry = zis.getNextEntry() ) != null ){
				File file = new File(entry.getName());
				files.add(file);
				os = new BufferedOutputStream(new FileOutputStream(file));
				byte[] buf = new byte[1024];
				int size = 0;
				while((size = zis.read(buf)) > 0 ){
					os.write(buf, 0, size);
				}
				os.close();
				zis.closeEntry();
			}
		}finally{
			zis.close();
		}
		return files;
	}
}

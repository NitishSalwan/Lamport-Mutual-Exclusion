import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
	private static final String NEW_LINE = System.getProperty("line.separator");
	private static Node parentNode = null;
	private static final Object mLock = new Object();

	public static void setParentNode(Node node) {
		parentNode = node;
		getFile(true);
	}

	public static void println(String log) {
		if (parentNode == null) {
			throw new NullPointerException("Parent Node is null");
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(getFile(false), true);
			fw.write(log);
			fw.write(NEW_LINE);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			try {
				fw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void csLog(String log) {
		synchronized (mLock) {
			if (parentNode == null) {
				throw new NullPointerException("Parent Node is null");
			}
			FileWriter fw = null;
			try {

				fw = new FileWriter("logger_CS.txt", true);
				fw.write(parentNode.getId() + " :: " + parentNode.getTimeStampNow() + " :: ");
				fw.write(log);
				fw.write(NEW_LINE);
			} catch (Exception e) {
				e.printStackTrace();

			} finally {
				try {
					fw.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private static File getFile(boolean newFile) {
		File file = new File("logger_" + parentNode.getId() + ".txt");
		if (newFile && file.exists()) {
			file.delete();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

}
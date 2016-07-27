import java.io.FileWriter;

public class Logger {
	private static final String NEW_LINE = System.getProperty("line.separator");
	private static Node parentNode = null;

	public static void setParentNode(Node node) {
		parentNode = node;
	}

	public static void println(String log) {
		if (parentNode == null) {
			throw new NullPointerException("Parent Node is null");
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter("logger_"+parentNode.getId()+".txt", true);
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MyApplication {

	private final int TOTAL_NODES;
	public static final int MESSAGE_SIZE = 1 * 100 * 1024; // 100KB

	// static HashMap<Integer,HashMap<Integer,Boolean>> cs_request_outer= new
	// HashMap<Integer,HashMap<Integer,Boolean>>();
	// static HashMap<Integer,Boolean> cs_request_inner = new
	// HashMap<Integer,Boolean>();
	// static int clock_value=0;

	HashMap<String, Node> host_to_port = new HashMap<String, Node>();
	// static HashMap<String,Integer> host_to_node = new
	// HashMap<String,Integer>();

	private Node node;

	public MyApplication(final int node_id) throws IOException {

		FileInputStream fis_lan = null;
		File file = new File("config1.txt");
		fis_lan = new FileInputStream(file);
		// while((f2=fis_lan.getChannel().tryLock())==null){}
		BufferedReader br = new BufferedReader(new InputStreamReader(fis_lan));

		String first_line = br.readLine();
		String[] messages_props = first_line.split("\\s+");
		TOTAL_NODES = Integer.parseInt(messages_props[0]);

		// System.out.println(ob1.d=Integer.parseInt(messages_props[1]));
		// System.out.println(ob1.c=Integer.parseInt(messages_props[2]));
		// System.out.println(ob1.critical_request_num=Integer.parseInt(messages_props[3]));

		String currentline;
		int count = Integer.parseInt(messages_props[0]);
		int i = 0;

		while (((currentline = br.readLine()) != null) && count > i) {
			if (!currentline.equals("")) {
				String[] words = currentline.split("\\s+");

				if (Integer.parseInt(words[0]) == node.getId()) {
					System.out.println("currentline " + currentline);
					node = new Node(node_id);
					node.setListeningPort(Integer.parseInt(words[2]));
					node.setName(words[1]);
					System.out.println("Node :" + node.toString());

				} else {
					host_to_port.put(words[1],
							new Node(Integer.parseInt(words[0]), words[1], Integer.parseInt(words[2])));
							// host_to_node.put(words[1],
							// Integer.parseInt(words[0]));

					// aos2.cs_request_inner.put(Integer.parseInt(words[0]),
					// false);
				}
				i++;
			}

		}
		node.setNeighbors(host_to_port);
		br.close();
	}

	public static void main(String[] args) throws IOException {
		MyApplication myApp = new MyApplication(Integer.parseInt(args[0]));
		myApp.node.startConnections();

		Client client = myApp.node.getClient();
		client.sendRequest();
		Server server = myApp.node.getServer();
	}
}


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

public class Server implements Runnable {
	private Node node;
	private SctpServerChannel sctpServerChannel;
	private SctpChannel sctpChannel;

	public Server(Node parent) {
		node = parent;
	}

	@Override
	public void run() {
		try {
			sctpServerChannel = SctpServerChannel.open();
			// Create a socket addess in the current machine at port 5000
			InetSocketAddress serverAddr = new InetSocketAddress(node.getListeningPort());
			// Bind the channel's socket to the server in the current machine at
			// port 5000
			sctpServerChannel.bind(serverAddr);
			// Server goes into a permanent loop accepting connections from
			// client
			System.out.println("Server up for Node :" + node.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		while (true) {
			try {
				sctpChannel = sctpServerChannel.accept();
				System.out.println("Request accepted at Node : " + node.toString());
				new RequestHandler(sctpChannel, node);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}

class RequestHandler {
	private SctpChannel sctpChannel;

	public RequestHandler(SctpChannel socket, Node node) throws Exception {
		this.sctpChannel = socket;

		System.out.println("Initializing Handler for node : " + node.getId() + " at Port : " + node.getListeningPort());
		System.out.println(
				"Server at node " + node.getId() + "'s queue size (before processing) : " + node.getNodeQueue().size());
		ByteBuffer byteBuffer = ByteBuffer.allocate(MyApplication.MESSAGE_SIZE);
		MessageInfo messageInfo = null;

		do {
			messageInfo = sctpChannel.receive(byteBuffer, null, null);
			Message message = parseMessage(byteToString(byteBuffer));
			
			/*
			 *  first check what is content of message 
			 *   1. add the NODE (message sender) to node->boolean hashmap  
 			 *   2. Check for hashmap if we have got all trues in hashmap of step 2 and peek element belongs to current node then execute critical section. 
			 *   3) if request -> 
			 *    a. call send_reply method from client
			 *    b. add this message to the queue
			 *   4). if reply 	 ->  nothing  
			 *   5). if release   ->  element remove from queue
			 *   
			 * 
			 */
			
			
			//TODO: Print accordingly
			System.out.println("Message received at node " + node.getId() + " : " + message+" & Queue Size :"+ node.getNodeQueue().size());
			
			
			

		} while (messageInfo != null);
	}

	public String byteToString(ByteBuffer byteBuffer) {
		byteBuffer.position(0);
		byteBuffer.limit(MyApplication.MESSAGE_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return new String(bufArr);
	}

	private Message parseMessage(String message){
		String[] message_array=message.split("\\s+");
		return new Message(message_array[0].trim(), Integer.parseInt(message_array[1].trim()),Integer.parseInt(message_array[2].trim()));
	}
}


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;

public class Server implements Runnable {
	private Node parentNode;
	private ServerSocket serverSocket;
	private Socket socket;

	public Server(Node parent) {
		parentNode = parent;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(parentNode.getListeningPort());
			Logger.println("Server up for Node :" + parentNode.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		while (true) {
			try {
				// sctpChannel = sctpServerChannel.accept();
				socket = serverSocket.accept();
				Logger.println("Request accepted at Node : " + parentNode.toString());
				new Thread(new RequestHandler(socket, parentNode)).start();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}

class RequestHandler implements Runnable {
	private Node node;
	private Socket socket;
	private BufferedReader reader;
	
	public RequestHandler(Socket socket, Node node) throws Exception {
		this.node = node;
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		// writer = new PrintWriter(socket.getOutputStream());

		/**
		 * 1. add the NODE (message send er) to node->boolean hashmap 2. Check
		 * for hashmap if we have got all trues in hashmap of step 2 and peek
		 * element belongs to current node then execute critical section.
		 * 
		 * 3. check what is content of message
		 * 
		 * 4. if request -> b. call send_reply method from client a. add this
		 * message to the queue
		 * 
		 * 5. if reply -> nothing
		 * 
		 * 6. if release -> element remove from queue
		 * 
		 * 
		 */

	}

	public void run() {
		Logger.println("Server at node " + node.getId() + "'s queue size (before processing) : "
				+ node.getMessageReceivedQueue().size());

		String messageStr = null;
		try {
			while ((messageStr = reader.readLine()) != null) {
				node.incrementClock();
				Message message = parseMessage(messageStr);
				Logger.println("Message received at node " + node.getId() + " : " + message.toString()
						+ " & Queue Size :" + node.getMessageReceivedQueue().size());

				// Step 1 - Put the node in
				node.putReceived(message.getSenderNodeId());

				// Step 2
				boolean flag = true;
				for (Map.Entry<Node, Boolean> map : node.getReceivedMap().entrySet()) {
					if (!map.getValue()) {
						flag = false;
						break;
					}
				}
				processMessage(message, flag);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private void processMessage(Message message, boolean canExecuteCritialSection) throws IOException {
		if (message.getContent().trim().equals("request")) {
			Logger.println("Message received as request, Execution will begin at " + node.getId());
			node.putMessageInQueue(message);
			criticalSectionCall(canExecuteCritialSection);
			node.sendReplyToServer(message);
		} else if (message.getContent().trim().equals("reply")) {
			Logger.println("Message received as reply, No execution at " + node.getId());
			criticalSectionCall(canExecuteCritialSection);
		} else if (message.getContent().trim().equals("release")) {
			Logger.println("Message received as release, Execution will begin at " + node.getId());
			node.removeMessageFromQueue(message);
			criticalSectionCall(canExecuteCritialSection);
		} else {
			throw new UnsupportedOperationException("No method with corresponding message");
		}
	}

	public void criticalSectionCall(boolean canExecute) throws IOException {
		node.callCriticalSection(canExecute);
	}

	public String byteToString(ByteBuffer byteBuffer) {
		byteBuffer.position(0);
		byteBuffer.limit(MyApplication.MESSAGE_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return new String(bufArr);
	}

	private Message parseMessage(String message) {
		String[] message_array = message.split("\\s+");
		return new Message(message_array[0].trim(), Integer.parseInt(message_array[1].trim()),
				Integer.parseInt(message_array[2].trim()));
	}
}

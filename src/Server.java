
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;


public class Server implements Runnable {
	private Node parentNode;
	private SctpServerChannel sctpServerChannel;
	private SctpChannel sctpChannel;

	public Server(Node parent) {
		parentNode = parent;
	}

	@Override
	public void run() {
		try {
			sctpServerChannel = SctpServerChannel.open();
			// Create a socket addess in the current machine at port 5000
			InetSocketAddress serverAddr = new InetSocketAddress(parentNode.getListeningPort());
			// Bind the channel's socket to the server in the current machine at
			// port 5000
			sctpServerChannel.bind(serverAddr);
			// Server goes into a permanent loop accepting connections from
			// client
			System.out.println("Server up for Node :" + parentNode.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		while (true) {
			try {
				sctpChannel = sctpServerChannel.accept();
				System.out.println("Request accepted at Node : " + parentNode.toString());
				new RequestHandler(sctpChannel, parentNode);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}

class RequestHandler {
	private SctpChannel sctpChannel;
	private Node parentNode;

	public RequestHandler(SctpChannel socket, Node node) throws Exception {
		this.sctpChannel = socket;
		this.parentNode = node;

		System.out.println("Initializing Handler for node : " + node.getId() + " at Port : " + node.getListeningPort());
		System.out.println("Server at node " + node.getId() + "'s queue size (before processing) : "
				+ node.getMessageReceivedQueue().size());
		ByteBuffer byteBuffer = ByteBuffer.allocate(MyApplication.MESSAGE_SIZE);
		MessageInfo messageInfo = null;

		 do {
		// do
		// {

		messageInfo = sctpChannel.receive(byteBuffer, null, null);
		// }while(messageInfo==null);
		if(messageInfo == null){
		 System.out.println("MessageInfo Null,Skipping rest of the server code at : Node " + parentNode.getId());
		 continue;
		 }

		parentNode.incrementClock();
		
		Message message = parseMessage(byteToString(byteBuffer));

		// TODO: Print accordingly
		System.out.println("Message received at node " + node.getId() + " : " + message + " & Queue Size :"
				+ node.getMessageReceivedQueue().size()); 

		
		/**
		 * 1. add the NODE (message sender) to node->boolean hashmap 2. Check
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
		// Step 3 - 6

		} while (messageInfo != null);
	}

	private void processMessage(Message message, boolean canExecuteCritialSection) throws IOException {
		if (message.getContent().trim().equals("request")) {
			System.out.println("Message received as request, Execution will begin at " + parentNode.getId());
			parentNode.putMessageInQueue(message);
			criticalSectionCall(canExecuteCritialSection);
			parentNode.sendReplyToServer(message);
		} else if (message.getContent().trim().equals("reply")) {
			System.out.println("Message received as reply, No execution at " + parentNode.getId());
			criticalSectionCall(canExecuteCritialSection);
		} else if (message.getContent().trim().equals("release")) {
			System.out.println("Message received as release, Execution will begin at " + parentNode.getId());
			parentNode.removeMessageFromQueue(message);
			criticalSectionCall(canExecuteCritialSection);
		} else {
			throw new UnsupportedOperationException("No method with corresponding message");
		}
	}

	public void criticalSectionCall(boolean canExecute) throws IOException {
		// Critical Section Execution
		if (!parentNode.getMessageReceivedQueue().isEmpty()) {
			if (canExecute && parentNode.getMessageReceivedQueue().peek().getSenderNodeId() == parentNode.getId()) {
				// Execute critical section
				parentNode.executeCriticalSection();

				// remove the request from queue
				parentNode.getMessageReceivedQueue().poll();

				// release message
				parentNode.sendRelease();
			}
		}

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

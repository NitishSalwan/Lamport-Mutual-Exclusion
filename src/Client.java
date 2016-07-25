
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class Client {
	private Node parent;
	private Map<Node, SCTPMessageInfo> socketMap = null;

	public Client(Node parent) {
		this.parent = parent;
		initSocketConnection();
	}

	public synchronized void sendRequest() throws IOException {
		parent.emptyReceivedMap();
		parent.setClock_value(parent.getClock_value() + 1);
		Message message = new Message("request", parent.getId(), parent.getClock_value());
		parent.putMessage(message);
		System.out.println("Message added in Queue from : " + parent.getId() + " at clock " + parent.getClock_value()
				+ "\nContent: " + message.toString());

		for (Map.Entry<Node, SCTPMessageInfo> entry : socketMap.entrySet()) {
			SctpChannel temp_socket = (SctpChannel) entry.getValue().sctpChannel;
			ByteBuffer temp_buffer = ByteBuffer.allocate(MyApplication.MESSAGE_SIZE);

			// convert the string message into bytes and put it in the byte
			// buffer
			temp_buffer.put(message.toString().getBytes());
			// Reset a pointer to point to the start of buffer
			temp_buffer.flip();
			// Send a message in the channel (byte format)
			temp_socket.send(temp_buffer, entry.getValue().messageInfo);

			System.out.println("Message sent from : " + parent.getId() + " to " + entry.getKey().getId() + " at clock "
					+ parent.getClock_value() + "\nContent: " + message.toString());

			System.out.println("Client queue Size " + parent.getId() + "  : " + parent.getNodeQueue().size());
		}

	}

	private void initSocketConnection() {
		socketMap = getSocketMap();
		for (Map.Entry<String, Node> neighbors : parent.getNeighbors().entrySet()) {
			Node neighbor = neighbors.getValue();

			int i = 1;
			try {
				SocketAddress socketAddress = new InetSocketAddress(neighbor.getName(), neighbor.getListeningPort());
				SctpChannel sctpChannel = SctpChannel.open(socketAddress, 1, 1);
				MessageInfo messageInfo = MessageInfo.createOutgoing(socketAddress, i++);
				socketMap.put(neighbor, new SCTPMessageInfo(messageInfo, sctpChannel));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Map<Node, SCTPMessageInfo> getSocketMap() {
		if (socketMap == null) {
			socketMap = new HashMap<>();
		}
		return socketMap;
	}

	private static class SCTPMessageInfo {
		private MessageInfo messageInfo;
		private SctpChannel sctpChannel;

		public SCTPMessageInfo(MessageInfo messageInfo, SctpChannel sctpChannel) {
			this.messageInfo = messageInfo;
			this.sctpChannel = sctpChannel;
		}

	}

}

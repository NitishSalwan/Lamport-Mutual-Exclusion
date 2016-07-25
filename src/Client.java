
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
		parent.incrementClock();
		Message message = new Message("request", parent.getId(), parent.getClock_value());
		parent.putMessageInQueue(message);
		System.out.println("Message added in Queue : " + parent.getId() + " at clock " + parent.getClock_value()
				+ " content: " + message.toString());

		System.out.println("Queue Size at" + parent.getId() + "  : " + parent.getNodeQueue().size());

		for (Map.Entry<Node, SCTPMessageInfo> entry : socketMap.entrySet()) {
			SctpChannel temp_socket = (SctpChannel) entry.getValue().sctpChannel;
			ByteBuffer temp_buffer = ByteBuffer.allocate(MyApplication.MESSAGE_SIZE);
			temp_buffer.put(message.toString().getBytes());
			temp_buffer.flip();
			temp_socket.send(temp_buffer, entry.getValue().messageInfo);

			System.out.println("Message sent from : " + parent.getId() + " to " + entry.getKey().getId() + " at clock "
					+ parent.getClock_value() + " content: " + message.toString());
		}
	}

	public synchronized void sendReply(Message msgToReplyFor) throws IOException {

		parent.incrementClock();
		String message = "reply " + parent.getId() + " " + parent.getClock_value();

		Node receiverNode = null;
		for (Map.Entry<Node, SCTPMessageInfo> entry : socketMap.entrySet()) {
			if (entry.getKey().getId() == msgToReplyFor.getSenderNodeId()) {
				receiverNode = entry.getKey();
			}
		}

		SctpChannel temp_socket = (SctpChannel) socketMap.get(receiverNode).sctpChannel;
		ByteBuffer temp_buffer = ByteBuffer.allocate(MyApplication.MESSAGE_SIZE);
		temp_buffer.put(message.toString().getBytes());
		temp_buffer.flip();
		temp_socket.send(temp_buffer, socketMap.get(receiverNode).messageInfo);
	}

	private void initSocketConnection() {
		socketMap = getSocketMap();
		int i = 1;
		for (Map.Entry<Integer, Node> neighbors : parent.getNeighbors().entrySet()) {
			Node neighbor = neighbors.getValue();
			try {
				SocketAddress socketAddress = new InetSocketAddress(neighbor.getName(), neighbor.getListeningPort());
				SctpChannel sctpChannel = SctpChannel.open(socketAddress, 5, 5);
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

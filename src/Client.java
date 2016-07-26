
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
	private Node parentNode;
	private Map<Node, SCTPMessageInfo> socketMap = null;
	
	
	public Client(Node parent) {
		this.parentNode = parent;
		initSocketConnection();
	}

	public synchronized void sendRequest() throws IOException {
		parentNode.emptyReceivedMap();
		parentNode.incrementClock();
		Message message = new Message("request", parentNode.getId(), parentNode.getClock_value());
		parentNode.putMessageInQueue(message);
		System.out.println("Message added in Queue : " + parentNode.getId() + " at clock " + parentNode.getClock_value()
				+ " content: " + message.toString());

		System.out.println("Queue Size at" + parentNode.getId() + "  : " + parentNode.getMessageReceivedQueue().size());

		for (Map.Entry<Node, SCTPMessageInfo> entry : socketMap.entrySet()) {
			SctpChannel temp_socket = (SctpChannel) entry.getValue().sctpChannel;
			ByteBuffer temp_buffer = ByteBuffer.allocate(MyApplication.MESSAGE_SIZE);
			temp_buffer.put(message.toString().getBytes());
			temp_buffer.flip();
			temp_socket.send(temp_buffer, entry.getValue().messageInfo);

			System.out.println("Message sent from : " + parentNode.getId() + " to " + entry.getKey().getId() + " at clock "
					+ parentNode.getClock_value() + " content: " + message.toString());
		}
	}

	public synchronized void sendReply(Message msgToReplyFor) throws IOException {

		parentNode.incrementClock();
		String message = "reply " + parentNode.getId() + " " + parentNode.getClock_value();

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

	public synchronized void sendRelease() throws IOException {
		parentNode.emptyReceivedMap();
		parentNode.incrementClock();
		Message message = new Message("release", parentNode.getId(), parentNode.getClock_value());
		
		System.out.println("Release Message sent by : " + parentNode.getId() + " at clock " + parentNode.getClock_value()
				+ " content: " + message.toString());

		for (Map.Entry<Node, SCTPMessageInfo> entry : socketMap.entrySet()) {
			SctpChannel temp_socket = (SctpChannel) entry.getValue().sctpChannel;
			ByteBuffer temp_buffer = ByteBuffer.allocate(MyApplication.MESSAGE_SIZE);
			temp_buffer.put(message.toString().getBytes());
			temp_buffer.flip();
			temp_socket.send(temp_buffer, entry.getValue().messageInfo);

			System.out.println("Message sent from : " + parentNode.getId() + " to " + entry.getKey().getId() + " at clock "
					+ parentNode.getClock_value() + " content: " + message.toString());
		}
	}
	
	
	
	public void initSocketConnection() {
		socketMap = getSocketMap();
		int i = 1;
		for (Map.Entry<Integer, Node> neighbors : parentNode.getNeighbors().entrySet()) {
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

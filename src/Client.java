
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client {
	private Node parentNode;
	private Map<Node, SocketMap> socketMap = null;

	public Client(Node parent) {
		this.parentNode = parent;
		initSocketConnection();
	}

	public synchronized void sendRequest() throws IOException {
		parentNode.emptyReceivedMap();
		parentNode.incrementClock();
		Message message = new Message("request", parentNode.getId(), parentNode.getClock_value());
		parentNode.putMessageInQueue(message);
		Logger.println("Message added in Queue : " + parentNode.getId() + " at clock " + parentNode.getClock_value()
				+ " content: " + message.toString());

		Logger.println("Queue Size at" + parentNode.getId() + "  : " + parentNode.getMessageReceivedQueue().size());

		for (Map.Entry<Node, SocketMap> entry : socketMap.entrySet()) {
			PrintWriter writer = entry.getValue().writer;
			writer.println(message.toString());
			writer.flush();

			Logger.println("Message sent from : " + parentNode.getId() + " to " + entry.getKey().getId() + " at clock "
					+ parentNode.getClock_value() + " content: " + message.toString());
		}
	}

	public void sendReply(Message msgToReplyFor) throws IOException {
		parentNode.incrementClock();
		String message = "reply " + parentNode.getId() + " " + parentNode.getClock_value();

		Node receiverNode = null;
		for (Map.Entry<Node, SocketMap> entry : socketMap.entrySet()) {
			if (entry.getKey().getId() == msgToReplyFor.getSenderNodeId()) {
				receiverNode = entry.getKey();
			}
		}

		try {
			PrintWriter printwriter = socketMap.get(receiverNode).writer;
			printwriter.println(message.toString());
			printwriter.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Logger.println("Message sent from : " + parentNode.getId() + " to " + msgToReplyFor.getSenderNodeId()
				+ " at clock " + parentNode.getClock_value() + " content: " + message.toString());
	}

	public synchronized void sendRelease() throws IOException {
		parentNode.emptyReceivedMap();
		parentNode.incrementClock();
		Message message = new Message("release", parentNode.getId(), parentNode.getClock_value());

		Logger.println("Release Message sent by : " + parentNode.getId() + " at clock " + parentNode.getClock_value()
				+ " content: " + message.toString());

		for (Map.Entry<Node, SocketMap> entry : socketMap.entrySet()) {
			PrintWriter writer = entry.getValue().writer;
			writer.println(message.toString());
			writer.flush();
			Logger.println("Message sent from : " + parentNode.getId() + " to " + entry.getKey().getId() + " at clock "
					+ parentNode.getClock_value() + " content: " + message.toString());
		}
	}

	public void initSocketConnection() {
		socketMap = getSocketMap();
		for (Map.Entry<Integer, Node> neighbors : parentNode.getNeighbors().entrySet()) {
			Node neighbor = neighbors.getValue();
			try {
				Socket socket = new Socket();
				socket.setKeepAlive(true);
				socket.connect(new InetSocketAddress(neighbor.getName(), neighbor.getListeningPort()), 10000);
				socketMap.put(neighbor, new SocketMap(socket, new PrintWriter(socket.getOutputStream())));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private Map<Node, SocketMap> getSocketMap() {
		if (socketMap == null) {
			socketMap = new HashMap<>();
		}
		return socketMap;
	}

	private static class SocketMap {
		private Socket socket;
		private PrintWriter writer;

		public SocketMap(Socket socket, PrintWriter writer) {
			super();
			this.socket = socket;
			this.writer = writer;
		}

	}
	//
	// private static class SCTPMessageInfo {
	// private MessageInfo messageInfo;
	// private SctpChannel sctpChannel;
	//
	// public SCTPMessageInfo(MessageInfo messageInfo, SctpChannel sctpChannel)
	// {
	// this.messageInfo = messageInfo;
	// this.sctpChannel = sctpChannel;
	// }
	//
	// }

}

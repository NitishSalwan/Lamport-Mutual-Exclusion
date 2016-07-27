
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

public class Node {
	private int _id;
	private String name;
	private int listeningPort;
	private int clock_value = -1;

	private PriorityBlockingQueue<Message> messageQueue = null;
	private Map<Node, Boolean> messageReceivedMap = null;

	private Server server;
	private Client client;

	private Map<Integer, Node> neighbors = null;

	private static final Object mObject = new Object();

	Comparator<Message> comparator = new Comparator<Message>() {
		@Override
		public int compare(Message m1, Message m2) {
			if ((m1.getTime() != m2.getTime())) {
				if (m1.getTime() > m2.getTime()) {
					return 1;
				} else {
					return -1;
				}
			} else {

				if (m1.getSenderNodeId() > m2.getSenderNodeId()) {
					return 1;
				} else {
					return -1;
				}
			}
		}
	};

	// Current Instance
	public Node(int nodeId) {
		_id = nodeId;
		clock_value = 0;
		neighbors = new HashMap<>();
		messageReceivedMap = new HashMap<>();
		messageQueue = new PriorityBlockingQueue<>(10, comparator);
	}

	public Node(int nodeId, String name, int listenPort) {
		_id = nodeId;
		this.name = name;
		listeningPort = listenPort;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setListeningPort(int listeningPort) {
		this.listeningPort = listeningPort;
	}

	public int getId() {
		return _id;
	}

	public String getName() {
		return name;
	}

	public int getListeningPort() {
		return listeningPort;
	}

	public int getClock_value() {
		return clock_value;
	}

	public synchronized void incrementClock() {
		++clock_value;
	}
	// public void setClock_value(int clock_value) {
	// this.clock_value = clock_value;
	// }

	public PriorityBlockingQueue<Message> getMessageReceivedQueue() {
		return messageQueue;
	}

	public void setNodeQueue(PriorityBlockingQueue<Message> nodeQueue) {
		this.messageQueue = nodeQueue;
	}

	public Map<Node, Boolean> getReceivedMap() {
		if (messageReceivedMap == null) {
			messageReceivedMap = new HashMap<>();
		}
		return messageReceivedMap;
	}

	public void setReceivedMap(Map<Node, Boolean> receivedMap) {
		this.messageReceivedMap = receivedMap;
	}

	public void emptyReceivedMap() {
		synchronized (mObject) {
			for (Map.Entry<Integer, Node> rmap : neighbors.entrySet()) {
				messageReceivedMap.put(rmap.getValue(), false);
			}
		}
	}

	@Override
	public String toString() {
		return _id + " " + name + " " + listeningPort;
	}

	public Map<Integer, Node> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(Map<Integer, Node> host_to_port) {
		neighbors = host_to_port;
	}

	public void putMessageInQueue(Message message) {
		synchronized (mObject) {
			messageQueue.put(message);
		}
	}

	public synchronized void startConnections() {
		server = new Server(this);
		new Thread(server).start();

		try {
			Thread.sleep(2000);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Start Client to send Requests
		new Thread(new Runnable() {
			@Override
			public void run() {
				client = new Client(Node.this);
			}
		}).start();

	}

	public Server getServer() {
		return server;
	}

	public Client getClient() {
		return client;
	}

	public void putReceived(int nodeId) {
		synchronized (mObject) {
			messageReceivedMap.put(neighbors.get(nodeId), true);
			Logger.println("Received HashMap value in node :" + this._id);
			for (Map.Entry<Node, Boolean> rMap : messageReceivedMap.entrySet()) {
				Logger.println(rMap.getKey() + " " + rMap.getValue());
			}
		}
	}

	public void executeCriticalSection() {

		Logger.println("Critical Section for Node : " + this._id + " executed");
		PriorityBlockingQueue<Message> queue = new PriorityBlockingQueue<>(10, comparator);

		Logger.println("Message in queue are in order");
		while (!messageQueue.isEmpty()) {
			Message message = messageQueue.poll();
			Logger.println(message.toString());
			queue.put(message);
		}
		messageQueue = queue;

	}

	public void removeMessageFromQueue(Message messageToRemove) {
		synchronized (mObject) {
			for (Message tempMessage : messageQueue) {
				if (tempMessage.getSenderNodeId() == messageToRemove.getSenderNodeId()) {
					messageQueue.remove(tempMessage);
				}
			}
		}
	}

	public void sendReplyToServer(Message message) throws IOException {
		synchronized (mObject) {
			client.sendReply(message);
		}
	}

	public void sendRelease() throws IOException {
		client.sendRelease();
	}

	public void callCriticalSection(boolean canExecute) throws IOException {
		synchronized (mObject) {
			Logger.println("In critical section fucntion call");
			// Critical Section Execution
			if (!messageQueue.isEmpty()) {
				if (canExecute && messageQueue.peek().getSenderNodeId() == this.getId()) {
					// Execute critical section
					executeCriticalSection();

					// remove the request from queue
					messageQueue.poll();

					// release message
					sendRelease();
				} else {
					Logger.println("Critical Section Not executed");
				}
			}
		}

	}

}

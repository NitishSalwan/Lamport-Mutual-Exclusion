
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
	private Map<Node, Boolean> receivedMap = null;

	private Server server;
	private Client client;

	private Map<Integer, Node> neighbors = null;

	// Current Instance
	public Node(int nodeId) {
		_id = nodeId;
		clock_value = 0;
		neighbors = new HashMap<>();
		receivedMap = new HashMap<>();
		messageQueue = new PriorityBlockingQueue<>(10, new Comparator<Message>() {
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
		});
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

	public synchronized void incrementClock(){
		++clock_value;
	}
//	public void setClock_value(int clock_value) {
//		this.clock_value = clock_value;
//	}

	public PriorityBlockingQueue<Message> getNodeQueue() {
		return messageQueue;
	}

	public void setNodeQueue(PriorityBlockingQueue<Message> nodeQueue) {
		this.messageQueue = nodeQueue;
	}

	public Map<Node, Boolean> getReceivedMap() {
		if (receivedMap == null) {
			receivedMap = new HashMap<>();
		}
		return receivedMap;
	}

	public void setReceivedMap(Map<Node, Boolean> receivedMap) {
		this.receivedMap = receivedMap;
	}

	public void emptyReceivedMap() {
		for(Map.Entry<Node, Boolean> rmap  : receivedMap.entrySet())
		{
			receivedMap.put(rmap.getKey(),false);
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
		// TODO Auto-generated method stub
		messageQueue.put(message);
	}

	public synchronized void startConnections() {
		// Start Server to Receive requests
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
		receivedMap.put(neighbors.get(nodeId), true);
	}

	public synchronized void executeCriticalSection() {
		System.out.println("Critical Section for Node : " + this._id + " executed");
	}

	public void removeMessageFromQueue(Message message) {
		for (Message tempMessage : messageQueue) {
			if (tempMessage.getSenderNodeId() == message.getSenderNodeId()) {
				messageQueue.remove(tempMessage);
			}
		}
	}

	public void sendReplyToServer(Message message) throws IOException{
		client.sendReply(message);
	}

}

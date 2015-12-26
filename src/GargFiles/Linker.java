package GargFiles;
import java.util.*; import java.io.*;import java.net.Socket;
public class Linker implements MsgHandler {
	public int myId;	
	Connector connector;
	MsgHandler app = null;// upper layer
	public boolean appFinished = false;
	public LinkedList<Integer> neighbors = new LinkedList<Integer>();	
	public Linker(String args[]) throws Exception { 
		String basename = args[0];
		myId = Integer.parseInt(args[1]);
		Topology.readNeighbors(myId, neighbors);
		connector = new Connector();
		connector.Connect(basename, myId, neighbors);
	}
	public void init(MsgHandler app){
		this.app = app;
		startListening();
	}
	public synchronized void handleMsg(Msg m, int src, String tag) { }
	public synchronized void executeMsg(Msg m) {	
		handleMsg(m, m.src, m.tag);
		notifyAll();
		if (app != null) 
			app.executeMsg(m);		
	}
	public void sendMsg(int destId, Object ... objects) {
		int j = neighbors.indexOf(destId);
		try {
			LinkedList<Object> objectList = Util.getLinkedList(objects);
			ObjectOutputStream os = connector.dataOut[j];
			os.writeObject(Integer.valueOf(objectList.size()));
			for (Object object : objectList) {
				os.writeObject(object);
			}
			os.flush();
		} catch (IOException e) {System.out.println(e);close();	}
	}
	public Msg receiveMsg(int fromId) {
		int i = neighbors.indexOf(fromId);
		try {
			
			ObjectInputStream oi = connector.dataIn[i];
			int numItems = ((Integer) oi.readObject()).intValue();
			LinkedList<Object> recvdMessage = new LinkedList<Object>();
			for (int j = 0; j < numItems; j++) 
				recvdMessage.add(oi.readObject());
			String tag = (String) recvdMessage.removeFirst();
			return new Msg(fromId, myId, tag, recvdMessage);
		} catch (Exception e) {
			System.out.println(e);
			close();
			return null;		
		}
		
	}	
	private void startListening(){
		for (int pid : neighbors)
			(new ListenerThread(pid, this)).start();		    	
	}
	public synchronized int getMyId() { return myId; }
	public LinkedList<Integer> getNeighbors() { return neighbors; }
	public void close() { appFinished = true; connector.closeSockets(); }
	public void turnPassive() {}
	
}
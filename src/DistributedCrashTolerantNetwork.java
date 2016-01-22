import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class DistributedCrashTolerantNetwork {
	
	volatile SynchronizedLibrary library;
	int myId = -1;
	volatile int crashK = -1;
	volatile int crashTime = -1;
	volatile int numCommands = 0;
	int numBooks = 0;
	//volatile int numClients = 0;
	int numOtherServers = 0;
	volatile int[] serverRequests;
	volatile Queue<String> localRequests;
	volatile DirectClock myClock;
	//volatile Queue<Integer> replies;
	volatile boolean crashed = false;
	//volatile boolean executing = false; //myTurn if
	//boolean requesting = false;
	volatile ConcurrentHashMap<Integer, int[]> sendRequest;
	volatile ConcurrentHashMap<Integer, int[]> sendRelease;
	volatile int[] sendLibrariesRequest;
	volatile boolean libUpdated = false;
	//volatile ConcurrentHashMap<Integer,Integer> initialServersUp;
	public Lock lock;
	
	public DistributedCrashTolerantNetwork(int numBooks, int numOtherServers, int myId){
		library = new SynchronizedLibrary(numBooks);
		this.numBooks = numBooks;
		//replies = new ConcurrentLinkedQueue<Integer>();
		this.myId = myId;
		this.numOtherServers = numOtherServers;
		sendRequest = new ConcurrentHashMap<Integer,int[]>();
		sendRelease = new ConcurrentHashMap<Integer, int[]>();
		sendLibrariesRequest = new int[numOtherServers+1+1];
		for(int q=0;q<sendLibrariesRequest.length;q++){
			sendLibrariesRequest[q]=0;
		}
		serverRequests = new int[numOtherServers+1+1];
		for (int j = 0; j < numOtherServers+1+1; j++){
            serverRequests[j] = Integer.MAX_VALUE; // infinity
		}
		localRequests = new ConcurrentLinkedQueue<String>();
		myClock = new DirectClock(numOtherServers+1+1,myId);
		//initialServersUp = new ConcurrentHashMap<Integer,Integer>();
		lock = new Lock();
	}
	
	public synchronized void releaseCS(int clientId){
		serverRequests[myId] = Integer.MAX_VALUE; // infinity
        for(int q = 0; q < sendRelease.get(clientId).length; q++){
        	if(!(q==myId)){
        		sendRelease.get(clientId)[q]=1;
        	}
		}
        libUpdated = false;
	}
	
	public synchronized void requestCS(int clientId, String action){
		myClock.tick();
	    serverRequests[myId] = myClock.getValue(myId);
	    //Set<Integer> tempServersAvailable = initialServersUp.keySet();
		for(int q = 0; q < sendRequest.get(clientId).length; q++){
			if(!(q==myId)){ //&& tempServersAvailable.contains(q)){
				sendRequest.get(clientId)[q]=1;
			}
			//else if(!(q==myId) && !tempServersAvailable.contains(q)){
			//	myClock.setValue(q, Integer.MAX_VALUE); //because the server is never up, so it doesn't need a reply
			//}
		}
	}
	
	public synchronized void receiveMessage(int src, int otherTimeStamp, String tag){
		int timeStamp = otherTimeStamp;
        myClock.receiveAction(src, timeStamp);
        if (tag.equals("Request")) {
            serverRequests[src] = timeStamp;
            //sendMsg(src, "ack", v.getValue(myId));
        } else if (tag.equals("Release"))
            serverRequests[src] = Integer.MAX_VALUE;
	}
	
	
	public static void main(String[] args){
		//NEED TO IMPLEMENT CHECKING K AND CRASHK TO SEE IF WE NEED TO CRASH SERVERS
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line = "";
		int numBooks = -1;
		int numServers = -1;
		int myId = -1;
		int localTCPPort = -1;
		String[] serverIps = null;
		///ArrayList<ServerTCPAndMessageListenerThread> threadList1 = new ArrayList<ServerTCPAndMessageListenerThread>();
		///ArrayList<ServerMessageSenderThread> threadList2 = new ArrayList<ServerMessageSenderThread>();
		
		try {
			line = in.readLine();
			String swag[] = line.split(" ");
			numBooks = Integer.parseInt(swag[2]);
			numServers = Integer.parseInt(swag[1]);
			myId = Integer.parseInt(swag[0]);
			serverIps = new String[numServers];
			
			for(int z = 0;z<numServers;z++){
				line = in.readLine();
				serverIps[z] = line; //put in each server's IP in order
				if((z+1)==myId && serverIps[z].substring(0, serverIps[z].indexOf(":")).equals("127.0.0.1")){
					localTCPPort = Integer.parseInt(serverIps[z].substring(serverIps[z].indexOf(":")+1));
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(localTCPPort!=-1){
			DistributedCrashTolerantNetwork server = new DistributedCrashTolerantNetwork(numBooks,numServers-1,myId);
			Thread stdinReader = new ServerCommandReaderThread(server, in);
			stdinReader.start();
			
			Thread crashMonitorAndSendThreadStarter = new ServerCrashControlThread(server, serverIps);
			crashMonitorAndSendThreadStarter.start();
				
			try {
				//Start serverMessageSenderThread for all other servers..MOVE TO OTHER THREAD AND HAVE TIMEOUT LOOPING
				
				//put each client connection into a separate thread that deals with each connected client's stream
				//if matching server ip is localhost, start listening at the designated port for messages from cients and other servers
				ServerSocket listener = new ServerSocket(localTCPPort);
				Socket s;
				
				//you also need to start a socket and connect with all other servers to send and receive messages
				//could possibly combine into one thread?
				
				while ( (s = listener.accept()) != null) {	//MIGHT NEED TO PUT THREAD.SLEEP IN ALL THE OTHER THREADS. OR MAYBE JUST IN STDIN COMMAND THREAD, AND HAVE IT CONTROL THE VALUES.
					if(!server.crashed){	//*******************NOT SURE IF THIS WILL DO TIMEOUT, NEED TO TEST
						Thread t = new ServerTCPAndMessageListenerThread(s, server);
						///threadList1.add((ServerTCPAndMessageListenerThread)t);
						t.start();
					} else{
						s.setSoTimeout(400);
					}
				}
				
			} catch (IOException e) {
				System.err.println("Server aborted:" + e);
			}
		}
	}
	
}

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


public class ServerCrashControlThread extends Thread{
	volatile Master server;
	String[] serverIps;
	int[] sendThreadStarted;
	boolean sendThreadsConnected;
	
	public ServerCrashControlThread(Master server, String[] ips){
		this.server = server;
		this.serverIps = ips;
		sendThreadStarted = new int[serverIps.length];
		for(int x = 0; x<sendThreadStarted.length;x++){
			sendThreadStarted[x]=0;
		}
		sendThreadsConnected = false;
	}
	
	public void run(){
		while(true){
			//always check if server crashed, and if it did make everything pause...which maybe we'll have to do actively except let messages keep running. or maybe just fake it and stop the stdin and new clients
			if((server.crashK!=-1) && (server.crashK<=server.numCommands)){	//NOT SURE THIS IS OK SPOT FOR THIS************MIGHT CAUSE PROBLEMS WITH NOT UPDATED LIBRARY. PERHAPS IT'S BEST TO FAKE CRASH SOMEWHERE WITH THREAD.SLEEP AND NOT DISRUPT THE WHOLE MESSAGING PROCESS
				//*************************CRASH NOT WORKING LOL FIX IT..I THINK SOME THREAD HANGS WHEN IT DOES !CRASHED
				server.crashed = true;
				server.crashK = -1;
				server.numCommands = 0;
				//this might cause trouble since not synchronized
				server.library = new SynchronizedLibrary(server.numBooks);
				try {
					//System.out.println("Crashig");
					Thread.sleep(server.crashTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				server.crashed = false;
			}
			
			//keep trying to connect sendThread if not connected
			if(!sendThreadsConnected){
				sendThreadsConnected = true;
				for(int y=0;y<serverIps.length;y++){
					if((y+1)!=server.myId && sendThreadStarted[y]==0){
						InetAddress inetIp = null;
						try {
							inetIp = InetAddress.getByName(serverIps[y].substring(0, serverIps[y].indexOf(":")));
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} //specify only address part before colon
						Socket socket = new Socket();
						try {
							socket.connect(new InetSocketAddress(inetIp, Integer.parseInt(serverIps[y].substring(serverIps[y].indexOf(":")+1))),100);
							Thread t1 = new ServerMessageSenderThread(socket,server,y+1);
							//threadList2.add((ServerMessageSenderThread) t1);
							t1.start();
							sendThreadStarted[y]=1;
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch(IOException e){
							//if didnt connect, go to next server/loop again. notConnect still true.
							sendThreadsConnected = false;
						}
					}
				}
			}
		}
	}

}

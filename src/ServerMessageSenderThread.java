import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class ServerMessageSenderThread extends Thread{
	
	volatile DistributedCrashTolerantNetwork server;
	Socket socket;
	boolean paused = false;
	int otherServerId;
	Scanner din;
	PrintStream pout;
	
	public ServerMessageSenderThread(Socket socket, DistributedCrashTolerantNetwork server, int otherServerId){
		this.server = server;
		this.socket = socket;
		this.otherServerId = otherServerId;
	}

	public void run(){
		try {
			socket.setSoTimeout(500);
			din = new Scanner(socket.getInputStream());
			pout = new PrintStream(socket.getOutputStream());
			
			//initial output to tell receiving thread it's a server not a client
			pout.println("Server,"+server.myId);
			pout.flush();
			
			//System.out.println("Server: " + server.myId + " -> " + otherServerId + " Sending thread started");
			
			while(true){
				if(!server.crashed){
					for (Integer key : server.sendRequest.keySet()) {
						if((server.sendRequest.get(key))[otherServerId]==1){
							sendMessage("Request");
							(server.sendRequest.get(key))[otherServerId] = 0;
						}
					}
					
					for (Integer key : server.sendRelease.keySet()) {
						if((server.sendRelease.get(key))[otherServerId]==1){
							sendMessage("Release");
							(server.sendRelease.get(key))[otherServerId] = 0;
						}
					}
					
					if(server.sendLibrariesRequest[otherServerId]==1){
						sendMessage("RequestLibrariesData");
						//server.sendLibrariesRequest[otherServerId]=0;
					}
				}
			}
			/*MyClient.close();*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String type){
		if(type.equals("Request")){
			pout.println(new Message(server.serverRequests[server.myId],server.myId,"Request"));
			pout.flush();
			String result = null;
			try{
				result = din.nextLine();
			} catch(Exception e){
				server.serverRequests[otherServerId] = Integer.MAX_VALUE;
				server.myClock.receiveAction(otherServerId, Integer.MAX_VALUE);
				return;
			}
			//waiting for reply
			String[] tempS = result.split(",");
			int replyTimeStamp = Integer.parseInt(tempS[0]);
			//DO I NEED TO RECORD NUM REPLIES RECIEVED?
			int otherId = Integer.parseInt(tempS[1]);
			server.serverRequests[otherId] = replyTimeStamp;
			server.myClock.receiveAction(otherId, replyTimeStamp); //not sure if this is how you treat a reply...
			//System.out.println(server.myId + " Received Reply from server " + otherServerId);
			//System.out.println(server.myId + " clock val: " + otherServerId);
		}
		else if(type.equals("Release")){
			try{
			pout.println(new Message(server.myClock.getValue(server.myId),server.myId,"Release",server.library.toString()));
			pout.flush();
			}catch(Exception e){
				//
			}
		}
		else if(type.equals("RequestLibrariesData")){
			pout.println(new Message(server.myId, "RequestLibrariesData"));
			pout.flush();
			String result = null;
			try{
				result = din.nextLine();
			} catch(Exception e){
				boolean updateDone = true;
				server.sendLibrariesRequest[otherServerId]=0;
				for(int x=0; x<server.sendLibrariesRequest.length; x++){
					if(x!=0 && x!=server.myId && server.sendLibrariesRequest[x]==1){
						updateDone = false;
						//System.out.println("Update not done b/c index: "+ x);
					}
				}
				if(updateDone){
					server.libUpdated=true;
				}
				return;
			}
			server.sendLibrariesRequest[otherServerId]=0;
			//waiting for library info from other library..should prob update timestamp somewhere
			String[] tempS = result.split(",");
			if(Integer.parseInt(tempS[0])>server.library.getTimeStamp()){ //**************************Should it be greater than?idk i'm confused lol which one...
				server.library.updateLibrary(result);
			}
			boolean updateDone = true;
			for(int x=0; x<server.sendLibrariesRequest.length; x++){
				if(x!=0 && x!=server.myId && server.sendLibrariesRequest[x]==1){
					updateDone = false;
					//System.out.println("Update not done b/c index: "+ x);
				}
			}
			if(updateDone){
				server.libUpdated=true;
			}
		}
	}
	
	public void pauseThread(){
		paused = true;
	}
	
	public void unPauseThread(){
		paused = false;
		notify();
	}
	
}

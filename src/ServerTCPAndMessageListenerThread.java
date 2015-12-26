import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;


public class ServerTCPAndMessageListenerThread extends Thread {
	
	Socket client;
	volatile SynchronizedLibrary library;
	volatile Master server;
	int otherServerId;
	boolean paused = false;
	
	enum threadType{SERVER,CLIENT};
	threadType type = threadType.CLIENT;
	boolean firstTime;
	
	public ServerTCPAndMessageListenerThread(Socket s, Master server){
		this.client = s;
		this.library = server.library;
		this.server = server;
	}
	
	public void run(){
		Scanner sc;
		String[] tempS;
		try {
			//MAYBE CUT OUT DUPLICATE S1->S2 AND S2->S1 THREADS BY DETECTING IF ALREADY HAVE ONE, AND USING JUST THE 1 CHANNEL
			sc = new Scanner(client.getInputStream());
			PrintWriter pout = new PrintWriter(client.getOutputStream());
			String firstInput;
			try{
			firstInput = sc.nextLine();
			}catch(Exception e){
				return;
			}
			if(firstInput.contains("Server")){
				type = threadType.SERVER;
			}
			firstTime = true;
			
			//IF CLIENT only recieves 1 command then disconnects and thread dies...might need to change
			if(type.equals(threadType.CLIENT)){
				while(server.crashed){
					//idk lol wait here if server crashed i guess lol
					client.close();
					return;
				}
				tempS = firstInput.split(" ");
				int clientId = Integer.parseInt(tempS[0]);
				if(!server.sendRequest.containsKey(clientId)){
					server.sendRequest.put(clientId, new int[server.numOtherServers+1+1]);
					for(int j=0;j<server.sendRequest.get(clientId).length;j++){
						server.sendRequest.get(clientId)[j]=0;
					}
				}
				if(!server.sendRelease.containsKey(clientId)){
					server.sendRelease.put(clientId, new int[server.numOtherServers+1+1]);
					for(int j=0;j<server.sendRelease.get(clientId).length;j++){
						server.sendRelease.get(clientId)[j]=0;
					}
				}
				//if not first time, hang
				////String command = sc.nextLine();
				//if(firstTime){
				//Request CS NEEDS TO BE SYNCHRONIZED METHOD, SO DOES RELEASE AND REPLY OR WON'T WORK WITH CLOCK TICK*******************************
				//System.out.println("Client " + clientId + " thread about to wait for requesting");
				
				//localQueue.add...
				server.localRequests.add(clientId+" "+tempS[1]+" "+tempS[2]);
				//only allow one thread, all else wait for lock 1 at a time
				while(true){
					server.lock.lock();
					if(server.localRequests.peek().equals(clientId+" "+tempS[1]+" "+tempS[2])){
						break;
					} else{server.lock.unlock();}
				}
				server.requestCS(clientId, " "+tempS[1]+" "+tempS[2]);
				//local.add// however client can't add additional commands while this is happening ********************************
				//wait till all replies are sent and my request is at top of queue
				while(!okayCS()){
					//wait(); // but then who will notify??
				}
				//System.out.println("Client " + clientId + "Inside Critical Section");
				//inside critical section
				//update library..make sure this can't lead to deadlock b/c of something waiting for reply..remember 1 to 1 threads
			    //Set<Integer> tempServersAvailable = server.initialServersUp.keySet();
				for(int q = 0;q<server.sendLibrariesRequest.length;q++){
					if(!(q==server.myId)){ //&& tempServersAvailable.contains(q)){
						server.sendLibrariesRequest[q]=1;
					}
				}
				if(server.numOtherServers>0){
					while(!server.libUpdated){
						//poll till lib updated
					}
				}
				//System.out.println(clientId + "Past libupdate");

				if(tempS[2].equals("reserve")){
					if(library.reserveBook(Integer.parseInt(tempS[0]), Integer.parseInt(tempS[1]))){
						pout.println("success");
					} else{
						pout.println("fail");
					}
				} else if(tempS[2].equals("return")){
					if(library.returnBook(Integer.parseInt(tempS[0]), Integer.parseInt(tempS[1]))){
						pout.println("success freed");
					} else{
						pout.println("fail");
					}
				}
				pout.flush();
				//update lib timestamp
				library.setTimeStamp(server.serverRequests[server.myId]);
				server.localRequests.poll();
				server.numCommands++; //one more command responded to.
				//out of critical section
				//Release CS
		        server.releaseCS(clientId);
		        //System.out.println("ClientId " + clientId + " has left critical section");
				client.close();
				
				//release lock, allow next local dude in line
				server.lock.unlock();
			}
			//IF SERVER, stays forever listening and responding to sendthread from otherserver
			else if(type.equals(threadType.SERVER)){
				//System.out.println(firstInput + " receiving thread started");
				while(true){
					//NEEDS TO BE SYNCHRONIZED**************
					if(!server.crashed){
						if(firstTime){
							firstTime = false;
						} else{
							try{
								firstInput = sc.nextLine();
							} catch(Exception e){
								return;
							}
						}
						tempS = firstInput.split(",");
						//System.out.println(firstInput);
						if(tempS.length==3 && tempS[2].equals("Request")){
							int otherTimeStamp = Integer.parseInt(tempS[0]);
							int srcId = Integer.parseInt(tempS[1]);
							server.receiveMessage(srcId, otherTimeStamp, tempS[2]);
							//sendMsg(src, "ack", v.getValue(myId));
							pout.println(new Message(server.myClock.getValue(server.myId),server.myId,"Reply"));
							pout.flush();
							//System.out.println(server.myId + " Received Request from server " + otherServerId);
						}
						else if(tempS.length>=4 && tempS[2].equals("Release")){
							int otherTimeStamp = Integer.parseInt(tempS[0]);
							int srcId = Integer.parseInt(tempS[1]);
							server.receiveMessage(srcId, otherTimeStamp, tempS[2]);
							//System.out.println(firstInput.substring(firstInput.indexOf("Release")+8));
							//if(Integer.parseInt(tempS[0])>server.library.getTimeStamp()){????????????????????
							server.library.updateLibrary(firstInput.substring(firstInput.indexOf("Release")+8));
							//System.out.println(server.myId + " Received Release from server " + otherServerId);
						}
						else if(tempS.length==2 && tempS[1].equals("RequestLibrariesData")){
							//send lib data bak
							pout.println(server.library.toString());
							pout.flush();
							//System.out.println(server.myId + " Received RequestLibrariesData from server " + otherServerId);
						}
						/*else if(tempS.length==2 && tempS[0].equals("Server")){
							server.initialServersUp.put(Integer.parseInt(tempS[1]), 1);
							server.myClock.setValue(otherServerId, 0); //initialize for server
						}*/
					}
				}
			}
			//pout.flush();
			//client.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Boolean okayCS() {
        for (int j = 0; j < server.numOtherServers+1+1; j++){
            if (isGreater(server.serverRequests[server.myId], server.myId, server.serverRequests[j], j)){
            	//System.out.println("Not at top of request list");
            	return false;
            }
            if (isGreater(server.serverRequests[server.myId], server.myId, server.myClock.getValue(j), j)){
            	//System.out.println("Not all replies returned/not most recent...wot");
            	//System.out.println(server.myId + ": " + server.serverRequests[server.myId] + "    " + j + ": " + server.myClock.getValue(j));
            	return false;
            }
            
        }
        return true;
    }
    boolean isGreater(int entry1, int pid1, int entry2, int pid2) {
        return ((entry1 > entry2) || ((entry1 == entry2) && (pid1 > pid2)));
    }
	
	public void pauseThread(){
		paused = true;
		//wait();//?
	}
	
	public void unPauseThread(){
		paused = false;
		notify();
	}

}

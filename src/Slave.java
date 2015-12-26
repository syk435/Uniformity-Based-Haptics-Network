import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class Slave {
	
	public static void main(String[] args){
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    String s;
	    String[] tempS;
		int numLines = 0;
		
		int numServers = -1;
		String[] serverIps = null;
		int clientId = -1;
		
		Socket MyClient = null;
		PrintStream pout;
		Scanner din;
		
		boolean insideInnerLoop = false;
		boolean connectCrash = false;
		
	    try {
	    	while ((s = in.readLine()) != null && s.length() != 0){ // An empty line or Ctrl-Z terminates the program
				//if first line read in, get client_id and ip_server address
				tempS = s.split(" ");
				if(numLines==0){
					clientId = Integer.parseInt(tempS[0].substring(1));
					numServers = Integer.parseInt(tempS[1]);
					serverIps = new String[numServers];
				}
				else if(numLines <= numServers){
					serverIps[numLines-1] = tempS[0]; //put in each server's IP in order
					//System.out.println("IP Stored: " + serverIps[numLines-1]);
				}
				else{
					if(tempS[0].equals("sleep")){
						Thread.sleep(Integer.parseInt(tempS[1]));
					} else{
					//only tcp, get tcp socket connection and send data
					try {
						//while loop till connect
						int serverNum = 0;
						boolean notConnected = true;
						while(notConnected){
							InetAddress inetIp = InetAddress.getByName(serverIps[serverNum].substring(0, serverIps[serverNum].indexOf(":"))); //specify only address part before colon
							MyClient = new Socket();
							try{
								MyClient.connect(new InetSocketAddress(inetIp, Integer.parseInt(serverIps[serverNum].substring(serverIps[serverNum].indexOf(":")+1))), 100);
								//if connect, notConnected = false and serverNum = 0 i guess
								notConnected = false;
							}catch(SocketTimeoutException e){
								//if didnt connect, go to next server/loop again. notConnect still true.
								++serverNum;
								serverNum%=numServers;
							}
						if(!notConnected){
							//System.out.println("Connected to: " + Integer.parseInt(serverIps[serverNum].substring(serverIps[serverNum].indexOf(":")+1)));
							if(insideInnerLoop && !connectCrash){
								s = in.readLine();
								if(s == null || s.length() == 0){
									MyClient.close();
									return;
								}
								tempS = s.split(" ");
							}try{
							if(tempS[0].equals("sleep")){
								Thread.sleep(Integer.parseInt(tempS[1]));
							} else{
								if(connectCrash){connectCrash = false;}
								din = new Scanner(MyClient.getInputStream());
								pout = new PrintStream(MyClient.getOutputStream());
								//syntax is: clientId(natural number)+ bookId(naturalNumber) reserve
								//MAYBE PUT TIMEOUT HERE *********************************************************
								pout.println(clientId + " " + tempS[0].substring(1) + " " + tempS[1]);
								pout.flush();
								String result = din.nextLine();
								if(result.equals("success")){
									String client = "c" + clientId;
									System.out.println(client + " " + tempS[0]);
								} else if(result.equals("fail")){
									String client = "c" + clientId;
									System.out.println("fail " + client + " " + tempS[0]);
								} else if(result.equals("success freed")){
									String client = "c" + clientId;
									System.out.println("free " + client + " " + tempS[0]);
								}
							}} catch(IOException|NoSuchElementException e){
								//System.out.println("readline io exception");
								++serverNum;
								serverNum%=numServers;
								connectCrash = true;
								notConnected = true;
								insideInnerLoop = true;
								continue;
							}
							MyClient.close();
							notConnected = true;
							insideInnerLoop = true;
						}
					}
					}catch (IOException e) {
						System.out.println(e);
					}
				}
			}
				++numLines;
			}
		} catch (IOException e) {
			System.err.println("Client aborted, error: " + e);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

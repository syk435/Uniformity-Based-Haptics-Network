import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ServerCommandReaderThread extends Thread{
	
	volatile Master server;
	boolean paused;
	BufferedReader in;
	
	
	public ServerCommandReaderThread(Master server, BufferedReader in){
		this.server = server;
		this.in = in;
	}
	
	public void run(){
		//BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    String s;
	    String[] tempS;
	    
	    try {
			while ((s = in.readLine()) != null && s.length() != 0){ // An empty line or Ctrl-Z terminates the program
				//System.out.println("HELLO WORLD");
				if(!server.crashed){
					tempS = s.split(" ");
					if(tempS[0].equals("crash")){
						//System.out.println("CRASHING");
						server.crashK = Integer.parseInt(tempS[1]);
						server.crashTime = Integer.parseInt(tempS[2]);
					}
				} //else{wait();}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void pauseThread(){
		paused = true;
	}
	
	public void unPauseThread(){
		paused = false;
	}

}

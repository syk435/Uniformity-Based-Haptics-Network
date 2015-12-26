
public class DirectClock {
	public int[] clock;
	int myId;
	
	public DirectClock(int numProc, int id){
		myId = id;
		clock = new int[numProc];
		for(int i = 0;i<numProc;i++){
			clock[i] = 0;
		}
		clock[myId] = 1;
		clock[0] = Integer.MAX_VALUE; //BECAUSE DIS NIGGA NEVER GONNA BE A PROCESS. NO PROCESS 0
	}
	
	public int getValue(int i){
		return clock[i];
	}
	
//	public void setValue(int i, int value){
//		clock[i]=value;
//	}
	
	public void tick(){
		clock[myId]++;
	}
	
	public void sendAction(){
		tick();
	}
	
	public void receiveAction(int sender, int sentValue){
		clock[sender] = this.max(clock[sender], sentValue);
		clock[myId] = this.max(clock[myId], sentValue)+1;
	}
	
	private int max(int a, int b) {
		if (a > b){return a;}
		return b;
	}

}

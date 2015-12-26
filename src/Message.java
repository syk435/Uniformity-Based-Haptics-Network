
public class Message {
	private int timeStamp = -1;
	private int serverId = -1;
	private String type = "";
	private String libraryData = "";
	
	public Message(int time, int id, String type){
		timeStamp = time;
		serverId = id;
		this.type = type;
	}
	
	public Message(int timeStamp, int id, String type, String libraryData){
		serverId = id;
		this.libraryData = libraryData;
		this.timeStamp = timeStamp;
		this.type = type;
	}
	
	public Message(int id, String type){
		serverId = id;
		this.type = type;
	}
	
	@Override
	public String toString(){
		String result = "";
		if(libraryData.equals("") && timeStamp==-1){
			result = serverId + "," + type;
		}
		else if(libraryData.equals("")){
			result = timeStamp + "," + serverId + "," + type;
		}
		else{
			result = timeStamp + "," + serverId + "," + type + "," + libraryData;
		}
		return result;
	}

}

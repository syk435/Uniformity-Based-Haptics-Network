package GargFiles;
import java.io.*; 
import java.rmi.*;
import java.util.*;
public interface MsgHandler extends Remote {
    public void handleMsg(Msg m, int src, String tag);
    public void executeMsg(Msg m);
    public Msg receiveMsg(int fromId) throws IOException;
    public void sendMsg(int destId, Object ...objects);
	public void init(MsgHandler app);
	public void close();
	public int getMyId();	
	public LinkedList<Integer> getNeighbors();
	public void turnPassive();
   }
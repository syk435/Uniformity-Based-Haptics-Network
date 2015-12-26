package GargFiles;
import java.util.*; import java.io.*;import java.net.Socket;
import java.rmi.*; import java.rmi.server.*;
public class Process implements MsgHandler {
        public int myId;
        public int n; // number of neighbors including myself
        //public Properties prop;
        MsgHandler app = null;// upper layer
        MsgHandler comm = null;// lower layer
        public LinkedList<Integer> neighbors = new LinkedList<Integer>();
        public Process(MsgHandler initComm)  {
                comm = initComm;
                if (comm!=null) {
                        myId = comm.getMyId();
                        neighbors = comm.getNeighbors();
                        //prop = comm.getProp();
                        n = neighbors.size() + 1;
                }
        }
        public void init(MsgHandler app){
                this.app = app;
                comm.init(this);
        }
        public synchronized void handleMsg(Msg m, int src, String tag) {
                // System.out.println(m.toString());            
        }
        public synchronized void executeMsg(Msg m) {
                handleMsg(m, m.src, m.tag);
                notifyAll();
                if (app != null)
                        app.executeMsg(m);
        }
        public void sendMsg(int destId, Object ... objects) {
                comm.sendMsg(destId, objects);
        }
        public void sendMsg(LinkedList<Integer> destIds, Object... objects) {
                for (int i : destIds)
                        if (i != myId)
                                sendMsg(i, objects);
        }
        public Msg receiveMsg(int fromId) {
                try {
                        return comm.receiveMsg(fromId);
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return null;
        }
        public synchronized void myWait() {
                try {wait();
                } catch (InterruptedException e) {
                        System.err.println(e);
                }
        }
        public int getMyId() { return myId; }
        //public Properties getProp() { return prop;}
        public int getNumProc() { return n; }
        public LinkedList<Integer> getNeighbors() { return neighbors; }
        public static void println(String s) {
                System.out.println(s);
        }
        public void close() { comm.close(); }
        public void turnPassive() { comm.turnPassive(); }
}
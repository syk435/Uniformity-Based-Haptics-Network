import GargFiles.Lock;
import GargFiles.Msg;
import GargFiles.MsgHandler;
import GargFiles.Process;

public class LamportMutex extends Process implements Lock {
    public DirectClock v;
    public int[] q; // request queue
    public LamportMutex(MsgHandler initComm) {
        super(initComm);
        v = new DirectClock(n, myId);
        q = new int[n];
        for (int j = 0; j < n; j++)
            q[j] = Integer.MAX_VALUE; // infinity
    }
    public synchronized void requestCS() {
        v.tick();
        q[myId] = v.getValue(myId);
        sendMsg(neighbors, "request", q[myId]);
        while (!okayCS())
            myWait();
    }
    public synchronized void releaseCS() {
        q[myId] =  Integer.MAX_VALUE; // infinity
        sendMsg(neighbors, "release", v.getValue(myId));
    }
    public Boolean okayCS() {
        for (int j = 0; j < n; j++){
            if (isGreater(q[myId], myId, q[j], j))
                return false;
            if (isGreater(q[myId], myId, v.getValue(j), j))
                return false;
        }
        return true;
    }
    boolean isGreater(int entry1, int pid1, int entry2, int pid2) {
        return ((entry1 > entry2)
                || ((entry1 == entry2) && (pid1 > pid2)));
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {
        int timeStamp = m.getMessageInt();
        v.receiveAction(src, timeStamp);
        if (tag.equals("request")) {
            q[src] = timeStamp;
            sendMsg(src, "ack", v.getValue(myId));
        } else if (tag.equals("release"))
            q[src] = Integer.MAX_VALUE;
    }
}
package GargFiles;
import java.io.*;
public class ListenerThread extends Thread {
    int channel;
    Linker process = null;
    public ListenerThread(int channel, Linker process) {
        this.channel = channel;
        this.process = process;
    }
    public void run() {
        while (!process.appFinished) {
            // System.out.println("Listening on " + channel);
                        Msg m = process.receiveMsg(channel);
                        process.executeMsg(m);

        }
    }
}
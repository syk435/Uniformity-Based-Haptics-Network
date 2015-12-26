package GargFiles;
import java.util.*;
import java.net.*; import java.io.*;
public class Name {
    BufferedReader din;
    PrintStream pout;
    Socket server;
    public void getSocket() throws IOException {
        server = new Socket(Symbols.nameServer, Symbols.ServerPort);
        din = new BufferedReader(
                    new InputStreamReader(server.getInputStream()));
        pout = new PrintStream(server.getOutputStream());
    }
    public int insertName(String name, String hname, int portnum)
            throws IOException {
        getSocket();
        pout.println("insert " + name + " " + hname + " " + portnum);
        pout.flush();
        int retValue = Integer.parseInt(din.readLine());
	server.close();
        return retValue;
    }
    public InetSocketAddress searchName(String name) throws IOException {
        getSocket();
        pout.println("search " + name);
        pout.flush();
        String result = din.readLine();
        System.out.println("NameServer returned" + result);
        StringTokenizer st = new StringTokenizer(result);
	server.close();
        int portnum = Integer.parseInt(st.nextToken());
        String hname = st.nextToken();
	if (portnum == 0) return null; 
	else return new InetSocketAddress(hname, portnum);
    }
    public void clear() throws IOException {
        getSocket();
        pout.println("clear " );
        pout.flush();
	server.close();
    }
    public static void main(String[] args) {
        Name myClient = new Name();
        try {
            myClient.insertName("hello1", "oak.ece.utexas.edu", 1000);
            InetSocketAddress pa = myClient.searchName("hello1");
            System.out.println(pa.getHostName() + ":" + pa.getPort());
        } catch (Exception e) {
            System.err.println("Server aborted:" + e);
        }
    }
}
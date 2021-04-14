import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

// How to run: Section 7 of ds-sim guide
// 		./ds-server -n -c ds-sample-config01.xml -v all


public class SocketClient {
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		SocketClient socketClient = new SocketClient("Localhost", 50000);
		socketClient.actualProgram();
	}
	
	private Socket socket;
	private InputStreamReader in;
	private BufferedReader bf;
	
	private PrintWriter pr;
	
	public SocketClient(String IP, int port) throws UnknownHostException, IOException {
		socket = new Socket(IP, port);
	}	
	
	public void send(String s) {
		pr.println(s);
		pr.flush();
		
	}
	
	// May have to use try/catch
	public String receive() throws IOException {
		
		return "";
		
	}
	
	public void actualProgram() throws UnknownHostException, IOException {
		//socket = new Socket("Localhost", 50000);
		
		pr = new PrintWriter(socket.getOutputStream());
		in = new InputStreamReader(socket.getInputStream());
		BufferedReader bf = new BufferedReader(in);
		
		send("HELO");
		
		String str = bf.readLine();
		System.out.println("server: "+ str);
		
		send("AUTH xxx");
		
		str = bf.readLine();
		System.out.println("server: "+ str);
		
		send("REDY");
		
		str = bf.readLine();
		System.out.println("server: " + str);	
	}
	

}
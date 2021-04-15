import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

// How to run: Section 7 of ds-sim guide
// 		./ds-server -n -c ds-sample-config01.xml -v all

// https://stackoverflow.com/questions/428073/what-is-the-best-simplest-way-to-read-in-an-xml-file-in-java-application

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;


public class SocketClient {
	
	private Socket socket;
	private PrintWriter pr;
	private InputStreamReader in;	
	private BufferedReader bf;	
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		SocketClient socketClient = new SocketClient("Localhost", 50000);
		socketClient.run();
	}
	
	public SocketClient(String IP, int port) throws UnknownHostException, IOException {
		socket = new Socket(IP, port);
		pr = new PrintWriter(socket.getOutputStream());
		in = new InputStreamReader(socket.getInputStream());
		bf = new BufferedReader(in);			
	}
		
	public void run() throws IOException {
	
		send("HELO");
		
		String str = bf.readLine();
		System.out.println("server: "+ str);
		
		// from announcements forum
		send("AUTH " + System.getProperty("user.name"));
		
		str = bf.readLine();
		System.out.println("server: "+ str);
		
		send("REDY");
		
		str = bf.readLine();
		System.out.println("server: " + str);	
		
	}
	
	public String receive(String str) throws IOException {
		
		return "";
		
	}
	
	// Sends messages to the server
	public void send(String s) {
		pr.println(s);
		pr.flush();
		
	}

}
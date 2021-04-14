import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// How to run: Section 7 of ds-sim guide
// 		./ds-server -n -c ds-sample-config01.xml -v all

// Gross, get rid of all the statics at some point

public class SocketClient {
	
	static Socket socket;
	static InputStreamReader in;
	static BufferedReader bf;
	
	static PrintWriter pr;
	
/*	public clientConstructor(String IP, int port) {

	}	
	
	// we want to have a method for both sending and receiving messages so that we don't have
	// a long list of instructions in our main
	 * 
	 * */
	
	public static void send(String s) {
		pr.println(s);
		pr.flush();
		
	}
	
	// May have to use try/catch
	public static String receive() throws IOException {
		
		return "";
		
	}
	
	
	public static void main(String[] args) throws IOException {
		socket = new Socket("Localhost", 50000);
		
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
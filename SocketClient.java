import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// How to run: Section 7 of ds-sim guide
// 		./ds-server -n -c ds-sample-config01.xml -v all

public class SocketClient {
	
/*	Socket socket;
	InputStreamReader in;
	BufferedReader bf;
	PrintWriter pr = null;
	
	public clientConstructor(String IP, int port) {

	}	
	
	// we want to have a method for both sending and receiving messages so that we don't have
	// a long list of instructions in our main
	
	public void send(String s) {
		
	}
	
	public String receive() {
		
	} */
	
	public static void main(String[] args) throws IOException {
		Socket s = new Socket("Localhost", 50000);
		
		PrintWriter pr = new PrintWriter(s.getOutputStream());
		
		pr.println("HELO");
		pr.flush();
		
		InputStreamReader in = new InputStreamReader(s.getInputStream());
		BufferedReader bf = new BufferedReader(in);
		
		String str = bf.readLine();
		System.out.println("server: "+ str);
		
		pr.println("AUTH xxx");
		pr.flush();
		
		str = bf.readLine();
		System.out.println("server: "+ str);
		
		pr.println("REDY");
		pr.flush();
		
		str = bf.readLine();
		System.out.println("server: " + str);	
	}
}
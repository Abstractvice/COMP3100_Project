import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
	
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/* How to run: Section 7 of ds-sim guide
 		./ds-server -n -c ds-sample-config01.xml -v all
 https://stackoverflow.com/questions/428073/what-is-the-best-simplest-way-to-read-in-an-xml-file-in-java-application

 Things to do:
	- Extract all the information from the xml files and store it here
	- The largest server type in an xml file has the largest coreCount value.
*/

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


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
		
	public String receive(String str) throws IOException {
		
		return "";
		
	}	
	
	private String[][] Servers;
	
	// https://www.javatpoint.com/how-to-read-xml-file-in-java
	public void readXML(String XMLFile) {
		try {
			// Here we are creating a constructor of file class that parses an XML file
			File file = new File(XMLFile);
			
			// An instance of factory that gives us a document builder
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			
			// Here is an instance of builder to parse the specified xml file
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			
			NodeList nodeList = doc.getElementsByTagName("server");
			
			Servers = new String[nodeList.getLength()][7];
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				// Node node = nodeList.item(i);
				Element eElement = (Element)nodeList.item(i); // What does this do?
				
				Servers[i][0] = eElement.getAttribute("type");
				Servers[i][1] = eElement.getAttribute("limit");
				Servers[i][2] = eElement.getAttribute("bootupTime");
				Servers[i][3] = eElement.getAttribute("hourlyRate");
				Servers[i][4] = eElement.getAttribute("coreCount");
				Servers[i][5] = eElement.getAttribute("memory");
				Servers[i][6] = eElement.getAttribute("disk");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() throws IOException {
	
		// Step 1
		send("HELO");
		
		// Step 2
		String str = bf.readLine();
		System.out.println("server: "+ str);
		
		// Step 3
		send("AUTH " + System.getProperty("user.name"));
		
		// Step 4: Need to read xml, call readXML() or something and prescribe them to values
		// inside of a data structure (most likely just an array)
		str = bf.readLine();
		System.out.println("server: "+ str);
		
		// Step 5
		send("REDY");
		
		
		// Step 6
		str = bf.readLine();
		System.out.println("server: " + str);	
		
		if (str.equals("NONE"))
			send("QUIT");
		
		// Step 7 - Client sends something
		
		// Step 8 - Server sends something
		
		// Step 9 - Client does something
		
		// Step 10 - Server sends something
		
		// Step 11 - Client does something
		
		// Step 12 - Client sends quit
		str = bf.readLine();
		System.out.println("server: "+ str);
		
		// Step 13 - Server sends Quit
		
		// Step 14 - Client quits
		
	}
	

	
	// Sends messages to the server
	public void send(String s) {
		pr.println(s);
		pr.flush();
		
	}

}
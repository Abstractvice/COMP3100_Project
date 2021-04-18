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
	- Extract all the information from the xml files and store it here.
		- Only need to extract server information?
	- The largest server type in an xml file has the largest coreCount value.
	- Make a separate class for server data structure. Like C struct????
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
		// TODO Auto-generated method stub
		SocketClient client = new SocketClient("Localhost", 50000);
		
		// Step 1
		client.send("HELO");
		
		// Step 2
		client.receive();
		
		// Step 3
		client.send("AUTH " + System.getProperty("user.name"));
		
		// Step 4
		client.receive();
		
		// Step 4.5: Figure out what goes here
		client.readXML("file");
		
		// Step 5
		client.send("REDY");
		
		// Step 6
		String str = client.receive();
		
		//String[] jobs = new String[7];
		
		boolean looping = true;
		
		// JOBN 37 0 653 3 700 3800
		
		client.send("QUIT"); 
		

		
		// Step 7 - Client sends SCHD: The scheduling decision
		
		
		// Step 8 - Server sends "OK": action succesfully done
		
		// Step 9 - Client sends "OK" or "REDY"????
		
		// Step 10 - Server sends something
		
		// Step 11 - Client does something
		
		// Step 12 - Client sends quit
		client.receive();
//		System.out.println("server: "+ str);
		
		// Step 13 - Server sends Quit
		
		// Step 14 - Client quits
		
	}
	
	public SocketClient(String IP, int port) throws UnknownHostException, IOException {
		socket = new Socket(IP, port);
		pr = new PrintWriter(socket.getOutputStream());
		in = new InputStreamReader(socket.getInputStream());
		bf = new BufferedReader(in);			
	}
	
	private String[][] Servers;
	private int largestServer = 0; // Stores the position of the largest server in the 2D array
	
	// https://www.javatpoint.com/how-to-read-xml-file-in-java
	// Parses the XML file and determines which server is the largest
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
			int coreCount = 0;
			
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
				
				if (Integer.parseInt(eElement.getAttribute("coreCount")) > coreCount) {
					coreCount = Integer.parseInt(eElement.getAttribute("coreCount"));
					largestServer = i;
				}
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// private boolean looping;
	
	public String receive() throws IOException {
		
		return bf.readLine();
		
	}

	
	// Sends messages to the server
	public void send(String s) {
		pr.println(s);
		pr.flush();
		
	}

}
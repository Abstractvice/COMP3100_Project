import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

// ./tests1.sh SocketClient.class -n

/* How to run: Section 7 of ds-sim guide
 		./ds-server -n -c ds-sample-config01.xml -v all
 https://stackoverflow.com/questions/428073/what-is-the-best-simplest-way-to-read-in-an-xml-file-in-java-application

 Things to do/take into account:
	- Change  as many -throws- into -try/catch- as possible
	- Use dataInputStream and dataOutputStream instead of PrintWriter and InputStreamReader if
		problems arise.
	- Figure out WTF is up with newlines
	- Include general error messages
*/

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//import SocketClient.Server;

// ./ds-server -c ds-sample-config01.xml -v all
// ./ds-client -a bf

// Reference implementation: ./ds-server -c ds-sample-config01.xml -v all

public class SocketClient {
	
	private Socket socket;	
	private DataOutputStream out;
	private DataInputStream in;	
	
	private int largestServer = 0; // Stores the position of the largest server in the 2D array
	
	private ArrayList<Server> allServers = new ArrayList<Server>();
	
	
	// Try and remove this, seems pointless
	private class Server {
		public String type;
		public int limit;
		public int bootupTime;
		public float hourlyRate;
		public int coreCount;
		public int memory;
		public int disk;
		
		String getType() {
			return type;
		}
		
		/*int getCoreCount() {
			return coreCount;
		} */
		
		Server(String t, int l, int bT, float hR, int cC, int m, int d) {
			this.type = t;
			this.limit = l;
			this.bootupTime = bT;
			this.hourlyRate = hR;
			this.coreCount = cC;
			this.memory = m;
			this.disk = d;
		}
		
	}	
	
	public SocketClient(String IP, int port) {
		try {
			socket = new Socket(IP, port);
			out = new DataOutputStream(socket.getOutputStream());;
			in = new DataInputStream(socket.getInputStream());
		} catch (Exception e) {
			System.out.print("Error: No Client!");
		}
	}	
	
	public void send(String s) throws IOException {
        try {
        	out.write(s.getBytes());
        } catch (Exception e) {
        	System.out.print("ERROR: Failed to send");
        }
        System.out.println("Message sent: " + s);
        
	}
	
	// Find source from StackOverflow JUST IN CASE
	public String receive() {
		StringBuilder line = new StringBuilder();
		
		try {
			int newLine;
			line = new StringBuilder();
			while ((newLine = in.read()) != '\n') {
				line.append((char) newLine);
			}	
		} catch (Exception e) {
			System.out.print("ERROR: Failed to receive");
		}
		
		System.out.println("Message received: " + line);	
		
		return line.toString();
	}	
	
	// https://www.javatpoint.com/how-to-read-xml-file-in-java
	// Parses the XML file and also determines which server is the largest
	public void readXML(String XMLFile) {
		try {
			// Here we are creating a constructor of file class that parses an XML file
			File file = new File(XMLFile);
			
			// An instance of factory that gives us a document builder
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			
			// Here is an instance of builder to parse the specified xml file
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize(); // What does this do?
			
			NodeList nodeListServer = doc.getElementsByTagName("server");
			
			int coreCount = 0;
			
			for (int i = 0; i < nodeListServer.getLength(); i++) {
				// Node node = nodeList.item(i);
				Element eElement = (Element)nodeListServer.item(i); // What does this do?
				
				String t = eElement.getAttribute("type");
				int l = Integer.parseInt(eElement.getAttribute("limit"));
				int bT = Integer.parseInt(eElement.getAttribute("bootupTime"));
				float hR = Float.parseFloat(eElement.getAttribute("hourlyRate"));
				int cC = Integer.parseInt(eElement.getAttribute("coreCount"));
				int m = Integer.parseInt(eElement.getAttribute("memory"));
				int d = Integer.parseInt(eElement.getAttribute("disk"));
				
				allServers.add(new Server(t, l, bT, hR, cC, m, d));
				
				if (Integer.parseInt(eElement.getAttribute("coreCount")) > coreCount) {
					coreCount = Integer.parseInt(eElement.getAttribute("coreCount"));
					largestServer = i;
				} // Find largest?
				
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		String IP = "Localhost"; // or 127.0.)0.1
		int port = 50000;
		SocketClient client = new SocketClient(IP, port);
		
		// Handshake
		client.send("HELO");
		client.receive(); // Server responds with OK
		client.send("AUTH " + System.getProperty("user.name"));
		client.receive(); // Server responds with OK
		
		client.readXML("ds-system.xml");
		client.send("REDY");
		
		// Step 6: Receives job schedule or "NONE"
		String str = client.receive();
		
		boolean looping = true;
		
		// Continues while connection is open
		// Need 3 layer loop?
		if (!str.equals("NONE")) {
			while (looping) {
				if (str.equals("NONE")) {
					looping = false;
					break;
				}			
				// Server sends job informtion
				if (str.equals("JOBN") || str.equals("JCPL")) { // Is this even necessary?
					client.send("REDY");
					str = client.receive();
					
				} else {
					String[] jobInfo = str.split("\\s+");
					String SCHD = "SCHD";
					int jobID = Integer.parseInt(jobInfo[2]); // May have to be string
					String serverType = client.allServers.get(client.largestServer).getType();
					String serverID = "0";
					
					client.send(SCHD + " " + jobID + " " + serverType + " " + serverID);
				}
			}
			
		}
		
		
		client.send("QUIT"); 
		
		client.receive();
		client.socket.close();
		
	}
}
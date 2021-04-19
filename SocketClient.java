import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

public class SocketClient {
	
	private Socket socket;
	private PrintWriter pr;
	private InputStreamReader in;	
	private BufferedReader bf;	
	
	// This contains the index of the largest server stored in allServers
	private int largestServer = 0; // Stores the position of the largest server in the 2D array
	
	private ArrayList<Server> allServers = new ArrayList<Server>();
	private ArrayList<Job> allJobs = new ArrayList<Job>();
	
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
	
	private class Job {
		int submitTime;
		int jobID;
		int estRuntime;
		int cores;
		int memory;
		int disk;
		
		Job(int sT, int jID, int eR, int c, int m, int d) {
			this.submitTime = sT;
			this.jobID = jID;
			this.estRuntime = eR;
			this.cores = c;
			this.memory = m;
			this.disk = d;
		}
	}
	
	//-----------------------------
	
	public SocketClient(String IP, int port) {
		try {
			socket = new Socket(IP, port);
			pr = new PrintWriter(socket.getOutputStream());
			in = new InputStreamReader(socket.getInputStream());
			bf = new BufferedReader(in);
		} catch (Exception e) {
			System.out.print("Error: No Client!");
		}
	}
	
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
			doc.getDocumentElement().normalize(); // What does this do?
			
			NodeList nodeListServer = doc.getElementsByTagName("server");
			//NodeList nodeListJob = doc.getElementsByTagName("job");
			
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
				}
				
				//System.out.print(coreCount);
				
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public String receive() {
		String message = "";
		try {
			message = bf.readLine();
		} catch (Exception e) {
			System.out.print("Error in receiving message!");
		}
		return message;
	}

	// *** error message?
	public void send(String s) {
		pr.println(s);
		pr.flush();
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		SocketClient client = new SocketClient("Localhost", 50000);
		
		client.send("HELO");
		client.receive();
		client.send("AUTH " + System.getProperty("user.name"));
		client.receive();
		client.readXML("ds-system.xml");
		client.send("REDY");
		
		// Step 6: Receives job schedule or "NONE"
		String str = client.receive();
		
		boolean looping = true;
		
		// Continues while connection is open
		if (!str.equals("NONE")) {
			while (looping) {
				if (str.equals("NONE")) {
					looping = false;
					break;
				}			
				// Server sends job informtion
				if (str.equals("OK")) {
					client.send("REDY");
					str = client.receive();
				}
				
				// [JOBN][submitTime][jobID][estRuntime][core][memory][disk]
				
				// Example: 	SCHD 	jobID 	serverType 	serverID
				//				SCHD 	3 		Joon 		1
				String[] jobInfo = str.split("\\s+");
				int numOfJobs = Integer.parseInt(jobInfo[2]);
				client.send("SCHD " + numOfJobs + " " + client.allServers.get(client.largestServer).type + " " + 0);
			}
			
		}
		
		
		client.send("QUIT"); 
		
		client.receive();
		client.socket.close();
		
	}
}
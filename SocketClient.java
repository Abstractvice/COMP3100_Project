import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * ./ds-server -c ds-sample-config01.xml -v all -n
 * ./ds-server -c ds-config01--wk9.xml -v all -n _> worstFit.xml.log         -i
 *
 * ./ds-server -c ds-sample-config01.xml -v all -n
 * ./ds-server -i -c ds-sim.xml -v all
 * 
 * python3 ./ds_viz.py ./ds-config01--wk9.xml ./worstFit.xml.log -c 10 -s 2
 * python3 ./ds_viz.py ./ds-S1-config02--demo.xml ./worstFit.xml.log -c 10 -s 2
 *
 *	Things to do
 *		- Change byte usage (e.g. s.getBytes(), use something else)
 *		- Spread out classes into different files
 *
 */

public class SocketClient {

	// Input and output byte streams
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;

	// Stores all the parsed by readXML method
	private ArrayList<SocketServer> allServersInitial = new ArrayList<SocketServer>();
	private ArrayList<SocketServerState> allServers = new ArrayList<SocketServerState>();
	
	// Stores the index of the location of the largest server with respect to allServers
	private int largestServer = 0; // Stores the position of the largest server in the 2D array

	// SocketClient constructor
	public SocketClient(String IP, int port) {
		try {
			socket = new Socket(IP, port);
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
		} catch (Exception e) {
			System.out.print("Error: No Client!");
		}
	}
	
	// Sends messages to server
	public void send(String s) {
		try {
			out.write(s.getBytes());
		} catch (Exception e) {
			System.out.print("ERROR: Failed to send");
		}
		System.out.println("Message sent: " + s);

	}

	//Takes in messages from server
	public String receive() throws IOException {
		StringBuilder line = new StringBuilder();

		int newLine;
		line = new StringBuilder();
		
		while ( (newLine = in.read()) != '\n') { // loop stops when newline is encountered, defined here in ASCII
			line.append((char) newLine);
		}

		System.out.println("Message received: " + line);

		return line.toString();
	}

	// https://www.javatpoint.com/how-to-read-xml-file-in-java
	// Parses the XML file and also determines which server is the largest
	public void readXML(String XMLFile) {
		try {
			File file = new File(XMLFile);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();

			NodeList nodeListServer = doc.getElementsByTagName("server");

			int coreCount = 0;

			// Adds all the servers to allServers
			for (int i = 0; i < nodeListServer.getLength(); i++) {
				Element server = (Element) nodeListServer.item(i);

				String t = server.getAttribute("type");
				int l = Integer.parseInt(server.getAttribute("limit"));
				int bT = Integer.parseInt(server.getAttribute("bootupTime"));
				float hR = Float.parseFloat(server.getAttribute("hourlyRate"));
				int cC = Integer.parseInt(server.getAttribute("coreCount"));
				int m = Integer.parseInt(server.getAttribute("memory"));
				int d = Integer.parseInt(server.getAttribute("disk"));

				allServersInitial.add(new SocketServer(t, l, bT, hR, cC, m, d));

				// Determines the index that contains the server with the largest coreCount value
				if (Integer.parseInt(server.getAttribute("coreCount")) > coreCount) {
					coreCount = Integer.parseInt(server.getAttribute("coreCount"));
					largestServer = i;
				} 

			}

		} catch (Exception e) {
			System.out.print("ERROR: Failed to transcribe XML!");
		}
		
		
	}
	
	// Comments will go through this process using the ds-sim protocol as a reference
	public static void main(String args[]) throws IOException {

		String IP = "Localhost";
		int port = 50000;
		SocketClient client = new SocketClient(IP, port);
		
		client.run();

	}	
	

	public int fitnessValue(SocketServerState server, SocketJob job) {
		
		int numOfRequiredCores = job.getCore();
		int serverCores = server.getCoreCount();
		
		return serverCores - numOfRequiredCores;
		
	}
	
	public boolean isActive(SocketServer server) {
		int serverState = server.getBootupTime();
		
		if (serverState == 0 || serverState == 2 || serverState == 3)
			return true;
		
		return false;
	}
	
	// A server can fit a job if the number of cores, memory and disk space is >= to the jobs given specs
	public boolean canFit(SocketServerState server, SocketJob job) {
		
        int requiredCores = job.getCore();
        int requiredMemory = job.getMemory();
        int requiredDiskSpace = job.getDisk();
        int serverCores = server.getCoreCount();
        int serverMemory = server.getMemory();
        int serverDiskSpace = server.getDisk();
        
        if(serverCores >= requiredCores && serverMemory >= requiredMemory 
        		&& serverDiskSpace >= requiredDiskSpace)
            return true;

        return false;
	}
	
	// Returns the index in our allServers arrayList of the most suitable server relative to this algorithm
	public int firstFit(SocketJob job) {
		
		int index = 0;
		
		for (int i = 0; i < allServers.size(); i++) {
			if (canFit(allServers.get(i), job)) {
				//System.out.println(i);
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	/**
	 * 
	 * @param currentJob
	 * @return
	 */
	
	public int bestFit(SocketJob job) {
		
		int bestFit = Integer.MAX_VALUE;
		int index = 0;
		
		for (int i = 0; i < allServers.size(); i++) {
			
			if (canFit(allServers.get(i), job)) {
				
				int fitnessValue = fitnessValue(allServers.get(i), job);
				
				if (fitnessValue < bestFit) {
					bestFit = fitnessValue;
					index = i;
				}
				
			}
			
		}
		
		return index;
		
	} 
	
	public int worstFit(SocketJob job) {
		
		int worstFit = 0; // Integer.MIN_VALUE
		int index = 0;
		
		for (int i = 0; i < allServers.size(); i++) {
			
			if (isServerAvailable(allServers.get(i))) {
			
				if (canFit(allServers.get(i), job)) {
				
					int fitnessValue = fitnessValue(allServers.get(i), job);
				
					if (fitnessValue > worstFit) {
						worstFit = fitnessValue;
						index = i;
					}
				
				}
			
			}
		}
		
		return index;
	}  
	
	public void sortServers(ArrayList<SocketServerState> a) {
		int[] cores = new int[a.size()];
		
		ArrayList<SocketServerState> temp = new ArrayList<>();
		
		
		
	}
	
	public boolean isServerAvailable(SocketServerState server) {
		boolean result = true;
		
		if (server.getServerState().equals("booting"))
			result = false;
		if (server.getServerState().equals("active"))
			result = false;
		if (server.getServerState().equals("unavailable"))
			result = false;
		
		return result;
	}
	
	/**
	 * Client Scheduler
	 * 
	 * JOBN		submitTime	jobID	estRuntime	cores	memory	disk
	 * 0		1			2		3			4		5		6
	 * 
	 * type		limit	bootupTime	hourlyRate	coreCount	memory	disk
	 * 0		1		2			3			4			5		6	
	 * 
	 * serverType	serverID	state	curStartTime	core	memory	disk
	 * 0			1			2		3				4		5		6
	 */
	
	public void run() throws IOException {
		
		// Handshake  and XML parsing (Steps 1 to 4)
		send("HELO\n");
		receive();
		send("AUTH " + System.getProperty("user.name") + "\n");
		receive();
		readXML("ds-system.xml");

		// Step 5
		send("REDY\n");


		// Step 6
		String str = receive(); // Assumes it receives either JOBN or NONE at first
		
		boolean looping = true;
		
		while (looping) {
			// Processing possible conditional Step 6 or Step 10
			
			if (str.equals("NONE")) {
				looping = false;
				break;
			}
			
			// Prompts client to confirm it is ready for the next job (if any)
			if (str.contains("JCPL")) {
				send("REDY\n");
				str = receive();
			// Step 7: the scheduling decision is sent to the server, based directly on ds-sim user guide
			// specifications in section 7 on SCHD
			} 
			
			// GETS ALL | Type serverType | Capable core memory disk | Avail core memory disk
			
			if (str.contains("JOBN")) {
				
				String[] currentJob = str.split("\\s+");
				
				String type = currentJob[0];
				int submitTime = Integer.parseInt(currentJob[1]);
				int id = Integer.parseInt(currentJob[2]);
				int estRunTime = Integer.parseInt(currentJob[3]);
				int core = Integer.parseInt(currentJob[4]);
				int memory = Integer.parseInt(currentJob[5]);
				int disk = Integer.parseInt(currentJob[6]);
				
				SocketJob job = new SocketJob(type, submitTime, id, estRunTime, core, memory, disk);
				
			// -----------------------------------------------------------------------------------
				String GETSCapable = "GETS Capable";
				
				int coreCount = job.getCore();
				int capableCore = job.getMemory();
				int availCore = job.getDisk();
				
				send(GETSCapable + " " + coreCount + " " + capableCore + " " + availCore + "\n");
				str = receive();
				
				String[] dataSplit = str.split("\\s+");
				int numOfItems = Integer.parseInt(dataSplit[1]);
				
				send("OK\n");
				
				for (int i = 0; i < numOfItems; i++) {
					str = receive();
					String[] serverState = str.split("\\s+");
					
					String typeS = serverState[0];
					int serverID = Integer.parseInt(serverState[1]);
					String state = serverState[2];
					int currStartTime = Integer.parseInt(serverState[3]);
					int coreCountS = Integer.parseInt(serverState[4]);
					int memoryS = Integer.parseInt(serverState[5]);
					int diskS = Integer.parseInt(serverState[6]);
					
					allServers.add(new SocketServerState(typeS, serverID, state, currStartTime, coreCountS, 
							memoryS, diskS));
					
				}
				
				//sortServers(allServers);
				
				send("OK\n");
				
				str = receive(); // likely to contain "."
			// ------------------------------------------------------------------------------------
				
				int firstFit = firstFit(job); // Seems to work??????????? Should be TINY
				int bestFit = bestFit(job);
				int worstFit = worstFit(job);
				
				String SCHD = "SCHD";
				int jobID = Integer.parseInt(currentJob[2]);
				String serverType = allServers.get(worstFit).getType(); // What we need to manipulate
				String serverID = "0";

				send(SCHD + " " + jobID + " " + serverType + " " + serverID + "\n");				
				
				str = receive(); // Step 8: Server sends OK for job scheduled
				//send("QUIT\n");
				send("REDY\n");  // Step 9(5): Client assumes there are more jobs, so sends "REDY"
				str = receive(); // Step 10
				
				allServers.clear();
			}
			
		}
		
		// Step 11 contained within the loop but too abstracted to be pointed out specifically
		
		send("QUIT\n"); // Step 12
		receive();	   // Step 13
		out.close();
		socket.close();
		
	}
}
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
 * ./test_results "java myScheduler" -o tt -n -c /home/luigiv/Documents/COMP3100/Testing/configs
 * 
 * ./ds-server -c config100-short-low.xml -v all -n
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

/**
 * Things to do: - Implement either a firstFit or roundRobin scheduler
 *
 */

public class SocketClient {

	// Initialises all the relevant commands that ds-sim responds to
	private final String AUTH = "AUTH " + System.getProperty("user.name") + "\n";	
	private final String CNTJ = "CNTJ";
	private final String GETSCAPABLE = "GETS Capable";
	private final String HELO = "HELO\n";
	private final String LSTJ = "LSTJ\n";
	private final String OK = "OK\n";
	private final String QUIT = "QUIT\n";
	private final String REDY = "REDY\n";
	private final String SCHD = "SCHD";
	
	// Initialises all the commands that we will receive from DS-SIM
	private final String JCPL = "JCPL";
	private final String JOBN = "JOBN";	
	private final String NONE = "NONE";
	
	// Input and output byte streams
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;

	// Our server data, both the initial data derived from our XML as well as the updated server data
	// given to us by ds-sim , which is updated after each job is submitted
	private ArrayList<SocketServer> allServersInitial = new ArrayList<SocketServer>();
	private ArrayList<SocketServerState> allServers = new ArrayList<SocketServerState>();

	// SocketClient constructor
	public SocketClient(String IP, int port) {
		// Establish connection
		try {
			// Attempt to connect to the server
			socket = new Socket(IP, port);
			
			// Initialise output stream that is the commands sent to the server
			out = new DataOutputStream(socket.getOutputStream());
			
			// Initialise input stream that is the commands received from the server
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

	// Takes in messages from server
	public String receive() throws IOException {
		StringBuilder line = new StringBuilder();

		int newLine;
		line = new StringBuilder();

		while ((newLine = in.read()) != '\n') { // loop stops when newline is encountered, defined here in ASCII
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

			}

		} catch (Exception e) {
			System.out.print("ERROR: Failed to transcribe XML!");
		}

	}

	public int fitnessValue(SocketServerState server, SocketJob job) {

		int numOfRequiredCores = job.getCore();
		int serverCores = server.getCoreCount();

		return serverCores - numOfRequiredCores;

	}

/*	public boolean isActive(SocketServer server) {
		int serverState = server.getBootupTime();

		if (serverState == 0 || serverState == 2 || serverState == 3)
			return true;

		return false;
	} */

	// A server can fit a job if the number of cores, memory and disk space is >= to
	// the jobs given specs
	public boolean canFit(SocketServerState server, SocketJob job) {

		int requiredCores = job.getCore();
		int requiredMemory = job.getMemory();
		int requiredDiskSpace = job.getDisk();
		int serverCores = server.getCoreCount();
		int serverMemory = server.getMemory();
		int serverDiskSpace = server.getDisk();

		if (serverCores >= requiredCores && serverMemory >= requiredMemory && serverDiskSpace >= requiredDiskSpace)
			return true;

		return false;
	}
	
	
	public int nextFit(SocketJob job) {
		return -1;
	}
	
	// Returns the index in our allServers arrayList of the most suitable server
	// relative to this algorithm
	public int firstFit(SocketJob job) {

		int index = 0;
		
		boolean check = false;

		for (int i = 0; i < allServers.size(); i++) {
			
			if (canFit(allServers.get(i), job)) {
				
				if (isServerActive(allServers.get(i)))
				// System.out.println(i);
					index = i;
					check = true;
					break;
			}
		}

		return index;
	}
	
	public boolean isServerActive(SocketServerState server) {
		boolean result = false;

		if (server.getServerState().equals("inactive"))
			result = true;
		if (server.getServerState().equals("active"))
			result = true;
		if (server.getServerState().equals("booting"))
			result = true;

		return result;
	}
	
	public boolean waitingJobs(SocketServerState server) {
		boolean result = false;
		
		return true;
		
	}
	
	public void sortServers(ArrayList<SocketServerState> servers) {
		
		SocketServerState temp;
		
		for (int i = 0; i < servers.size(); i++) {
			for (int j = i + 1; j < servers.size(); j++) {
				if (servers.get(i).getCoreCount() > servers.get(j).getCoreCount()) {
					temp = servers.get(i);
					servers.set(i, servers.get(j));
					servers.set(j, temp);
				}
			}
		}
			
	}
	
	public void sortServersDescending(ArrayList<SocketServerState> servers) {
		
		SocketServerState temp;
		
		for (int i = 0; i < servers.size(); i++) {
			for (int j = i + 1; j < servers.size(); j++) {
				if (servers.get(i).getCoreCount() > servers.get(j).getCoreCount()) {
					temp = servers.get(i);
					servers.set(i, servers.get(j));
					servers.set(j, temp);
				}
			}
		}
			
	}

	public SocketJob getJob(String[] jobInfo) {
		String type = jobInfo[0];
		int submitTime = Integer.parseInt(jobInfo[1]);
		int id = Integer.parseInt(jobInfo[2]);
		int estRunTime = Integer.parseInt(jobInfo[3]);
		int core = Integer.parseInt(jobInfo[4]);
		int memory = Integer.parseInt(jobInfo[5]);
		int disk = Integer.parseInt(jobInfo[6]);
		
		return new SocketJob(type, submitTime, id, estRunTime, core, memory, disk);
	}
	
	public SocketServerState getServer(String[] serverInfo) {
		String typeS = serverInfo[0];
		int serverID = Integer.parseInt(serverInfo[1]);
		String state = serverInfo[2];
		int currStartTime = Integer.parseInt(serverInfo[3]);
		int coreCountS = Integer.parseInt(serverInfo[4]);
		int memoryS = Integer.parseInt(serverInfo[5]);
		int diskS = Integer.parseInt(serverInfo[6]);
		
		return new SocketServerState(typeS, serverID, state, currStartTime, coreCountS, memoryS, diskS);
	}
	
	/**
	 * Client Scheduler
	 * 
	 * JOBN submitTime jobID estRuntime cores memory disk 0 1 2 3 4 5 6
	 * 
	 * type limit bootupTime hourlyRate coreCount memory disk 0 1 2 3 4 5 6
	 * 
	 * serverType serverID state curStartTime core memory disk 0 1 2 3 4 5 6
	 */	
	
	public void run() throws IOException {

		// Handshake and XML parsing (Steps 1 to 4)
		send(HELO);
		receive();
		send(AUTH);
		receive();
		readXML("ds-system.xml");

		// Step 5
		send(REDY);

		// Step 6
		String str = receive(); // Assumes it receives either JOBN or NONE at first

		boolean looping = true;

		while (looping) {
			// Processing possible conditional Step 6 or Step 10

			if (str.equals(NONE)) {
				looping = false;
				break;
			}

			// Prompts client to confirm it is ready for the next job (if any)
			if (str.contains(JCPL)) {
				send(REDY);
				str = receive();
				// Step 7: the scheduling decision is sent to the server, based directly on
				// ds-sim user guide
				// specifications in section 7 on SCHD
			}

			// GETS ALL | Type serverType | Capable core memory disk | Avail core memory
			// disk

			if (str.contains(JOBN)) {

				String[] currentJob = str.split("\\s+");

				SocketJob job = getJob(currentJob);

				// -----------------------------------------------------------------------------------

				int coreCount = job.getCore();
				int capableCore = job.getMemory();
				int availCore = job.getDisk();

				send(GETSCAPABLE + " " + coreCount + " " + capableCore + " " + availCore + "\n");
				str = receive();

				String[] dataSplit = str.split("\\s+");
				int numOfItems = Integer.parseInt(dataSplit[1]);

				send(OK);

				for (int i = 0; i < numOfItems; i++) {
					str = receive();
					String[] serverState = str.split("\\s+");

					allServers.add(getServer(serverState));

				}

				sortServers(allServers); //Likely Works?

				send(OK);

				str = receive(); // likely to contain "."
				// ------------------------------------------------------------------------------------

				int firstFit = firstFit(job); // Seems to work??????????? Should be TINY

				int jobID = Integer.parseInt(currentJob[2]);
				String serverType = allServers.get(firstFit).getType(); // What we need to manipulate
				String serverID = "0";

				send(SCHD + " " + jobID + " " + serverType + " " + serverID + "\n");

				str = receive(); // Step 8: Server sends OK for job scheduled
				// send("QUIT\n");
				send(REDY); // Step 9(5): Client assumes there are more jobs, so sends "REDY"
				str = receive(); // Step 10

				allServers.clear();
			}

		}

		// Step 11 contained within the loop but too abstracted to be pointed out
		// specifically

		send(QUIT); // Step 12
		receive(); // Step 13
		out.close();
		socket.close();

	}
	
	// Comments will go through this process using the ds-sim protocol as a
	// reference
	public static void main(String args[]) throws IOException {

		String IP = "Localhost";
		int port = 50000;
		SocketClient client = new SocketClient(IP, port);

		client.run();

	}

}
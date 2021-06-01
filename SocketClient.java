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
 * 
 *  hello
 * ./test_results "java SocketClient" -o tt -n -c /home/luigiv/Documents/COMP3100/Testing/configs/other
 * 
 * ./ds-server -c config20-long-high.xml -v all -n
 * 
 * ./ds-server -c config100-short-low.xml -v all -n
 * ./ds-server -c config20-long-low.xml -v all -n
 * ./ds-server -c ds-sample-config01.xml -v all -n
 * ./ds-server -c ds-config01--wk9.xml -v all -n        -i
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
 *
 * Try something else!!!!
 * 
 * 
 * 
 * # actual simulation end time: 97359, #jobs: 980 (failed 0 times)
   # total #servers used: 20, avg util: 94.81% (ef. usage: 94.76%), total cost: $314.18
# avg waiting time: 1573, avg exec time: 2473, avg turnaround time: 4046

 * 
 * 
 * 
 * 
 * 
 */

public class SocketClient {

	// Initialises all the relevant commands that ds-sim responds to
	private final String AUTH = "AUTH " + System.getProperty("user.name") + "\n";
	private final String CNTJ = "CNTJ";
	private final String GETSALL = "GETS All";
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

	// Our server data, both the initial data derived from our XML as well as the
	// updated server data
	// given to us by ds-sim , which is updated after each job is submitted
//	private ArrayList<SocketServer> allServersInitial = new ArrayList<SocketServer>();
	private ArrayList<SocketServer> allServers = new ArrayList<SocketServer>();

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

	/*
	 * public int fitnessValue(SocketServer server, SocketJob job) {
	 * 
	 * int numOfRequiredCores = job.getCore(); int serverCores =
	 * server.getCoreCount();
	 * 
	 * return serverCores - numOfRequiredCores;
	 * 
	 * }
	 * 
	 * private int nextFitPointer = 0;
	 * 
	 * public int nextFit(SocketJob job) {
	 * 
	 * boolean check = false;
	 * 
	 * if (nextFitPointer < 0) nextFitPointer = 0;
	 * 
	 * int index = nextFitPointer;
	 * 
	 * for (int i = index; i < allServers.size(); i++) { if
	 * (canFit(allServers.get(i), job)) { if (isServerActive(allServers.get(i))) {
	 * index = i; check = true; break; } } if (index == allServers.size() - 1) index
	 * = 0; }
	 * 
	 * if (!check) { for (int i = index; i < allServers.size(); i++) { if
	 * (canFit(allServers.get(i), job)) { index = i; break; } if (index ==
	 * allServers.size() - 1) index = 0; } }
	 * 
	 * nextFitPointer = index - 1;
	 * 
	 * return nextFitPointer + 1;
	 * 
	 * }
	 * 
	 * // Returns the index in our allServers arrayList of the most suitable server
	 * // relative to this algorithm public int firstFit(SocketJob job) {
	 * 
	 * int index = 0;
	 * 
	 * boolean check = false;
	 * 
	 * for (int i = 0; i < allServers.size(); i++) {
	 * 
	 * if (canFit(allServers.get(i), job)) {
	 * 
	 * if (isServerActive(allServers.get(i))) { index = i; check = true; break; } }
	 * }
	 * 
	 * if (!check) { for (int i = 0; i < allServers.size(); i++) { if
	 * (canFit(allServers.get(i), job)) { index = i; break; } } }
	 * 
	 * return index; }
	 * 
	 * public boolean isServerActive(SocketServer server) { boolean result = false;
	 * 
	 * if (server.getServerState().equals("inactive")) result = true; if
	 * (server.getServerState().equals("active")) result = true; if
	 * (server.getServerState().equals("booting")) result = true;
	 * 
	 * return result; }
	 */

	public void sortServers(ArrayList<SocketServer> servers) {

		SocketServer temp;

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

	public SocketServer getServer(String[] serverInfo) {
		String typeS = serverInfo[0];
		int serverID = Integer.parseInt(serverInfo[1]);
		String state = serverInfo[2];
		int currStartTime = Integer.parseInt(serverInfo[3]);
		int coreCountS = Integer.parseInt(serverInfo[4]);
		int memoryS = Integer.parseInt(serverInfo[5]);
		int diskS = Integer.parseInt(serverInfo[6]);
		int waitingJobs = Integer.parseInt(serverInfo[7]);
		int runningJobs = Integer.parseInt(serverInfo[8]);

		return new SocketServer(typeS, serverID, state, currStartTime, coreCountS, memoryS, diskS, waitingJobs,
				runningJobs);
	}

	public boolean noWaitingJobs(SocketServer server) {
		boolean waitingJobs = false;
		if (server.getWaitingJobs() == 0)
			waitingJobs = true;

		return waitingJobs;
	}

	public boolean isInactive(SocketServer server) {
		boolean inactive = false;
		if (server.getServerState().equals("inactive"))
			inactive = true;

		return inactive;
	}

	public boolean isBooting(SocketServer server) {
		boolean booting = false;
		if (server.getServerState().equals("booting"))
			booting = true;

		return booting;
	}

	public boolean isIdle(SocketServer server) {
		boolean idle = false;
		if (server.getServerState().equals("idle"))
			idle = true;

		return idle;
	}

	public boolean isActive(SocketServer server) {
		boolean active = false;
		if (server.getServerState().equals("active"))
			active = true;

		return active;
	}

	public boolean isUnavailable(SocketServer server) {
		boolean unavailable = false;
		if (server.getServerState().equals("unavailable"))
			unavailable = true;

		return unavailable;
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
//		readXML("ds-system.xml");

		send(REDY);
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

				sortServers(allServers); // Likely Works?

				send(OK);

				str = receive(); // likely to contain "."

				// int firstFit = firstFit(job); // Seems to work??????????? Should be TINY
				// int nextFit = nextFit(job);
				int optimalFit = optimalFit(job);

				// _____________________________________________________________________________________________________________________

				int jobID = Integer.parseInt(currentJob[2]);
				String serverType = allServers.get(optimalFit).getType(); // What we need to manipulate
				int serverID = allServers.get(optimalFit).getServerID();

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

	/**
	 * Basic algorithm idea: Prioritise in the following order:
	 * 
	 * 1. Inactive servers: immediately available with no running jobs, pick first
	 * so they can boot and then go idle 
	 * 2. Idle servers: immediately available with
	 * no running jobs 
	 * 3. Active servers: possibly available 3.1. Store the active
	 * servers with NO waiting jobs 3.2. Store the active servers WITH waiting jobs
	 * in separate storagehe one that has the SMALLEST WAIT TIME
	 * 
	 * Add later: server with waiting jobs, t
	 * 
	 * page 14
	 * 
	 * @throws IOException
	 * 
	 */
	public int optimalFit(SocketJob job) throws IOException {

		int optimalServerIndex = 0;

		boolean alreadyObtainedServer = false;

		ArrayList<Integer> allIndexes = new ArrayList<>();
		
		for (int i = 0; i < allServers.size(); i++) {
			allIndexes.add(i);
		}

		ArrayList<Integer> inactiveServers = new ArrayList<>();
		ArrayList<Integer> nonWaitingActiveServers = new ArrayList<>(); // no waiting jobs
		ArrayList<Integer> activeServers = new ArrayList<>();

		for (int i = 0; i < allServers.size(); i++) {
			// best case scenario, if true just use this one
			if (canFit(allServers.get(i), job)) {

				if (isIdle(allServers.get(i))) {
					optimalServerIndex = i;
					alreadyObtainedServer = true;
					break;
				}

				// Next best thing, but must boot
				if (isInactive(allServers.get(i))) {
					inactiveServers.add(i);
				}

				if (isActive(allServers.get(i))) {
					// Store all active servers with no waiting jobs
					if (noWaitingJobs(allServers.get(i))) {
						nonWaitingActiveServers.add(i);
					}
					// Stores the rest of the active servers, the ones with waiting jobs
					else {
						activeServers.add(i);
					}

				}
			}
		}

		if (!alreadyObtainedServer) {
			//
			// do this one last
			if (!nonWaitingActiveServers.isEmpty()) {
				optimalServerIndex = shortestWaitTime(nonWaitingActiveServers, job.getSubmitTime()); // Finds the ACTIVE
																										// server with
																										// the SHORTEST
																										// wait time
			} else if (!inactiveServers.isEmpty()) {
				optimalServerIndex = inactiveServers.get(0); // Finds the SMALLEST server to schedule to, remember we
																// have sorted allServers into ascending order
			} else if (!activeServers.isEmpty()) {
				optimalServerIndex = fewestWaitingJobs(activeServers);// returns the active server with the FEWEST
																		// waiting jobs. Worst case scenario
			}
		}

		optimalServerIndex = fewestWaitingJobs(allIndexes);

		return optimalServerIndex;

	}

	// index of the server with shortest ESTIMATED waiting time if a potential new
	// job is added
	// In essence, we want to schedule our job to a server which will result in the
	// smallest amount of waiting time being added
	public int shortestWaitTime(ArrayList<Integer> nonWaitingActiveServers, int currentJobSubmitTime)
			throws IOException {

		int shortestWaitingTimeIndex = 0;

		// ------------------------------------------------------------------------------------------------------------

		// Here, we need to obtain a collection of all the running jobs in the current
		// server (per iteration)
		// Aligned with nonWaitingActiveServers index, contains the LATEST possible
		// finish time for any given job in each server
		ArrayList<Integer> latestFinishTimes = new ArrayList<>();

		for (int i = 0; i < nonWaitingActiveServers.size(); i++) {

			/**
			 * 1. Collect a list of all the RUNNING JOBS over each iteration 1.1. Use LSTJ
			 * command 2. Find job with the latest estimated completion time - may need 2
			 * FOR loops
			 */

			String serverType = allServers.get(nonWaitingActiveServers.get(i)).getType();

			int serverID = allServers.get(nonWaitingActiveServers.get(i)).getServerID();

			// ---------------------------------- Finding our running jobs on iterated
			// server ------------------------------------------------

			send(LSTJ + " " + serverType + " " + serverID + "\n");

			String str = receive();

			send(OK);

			ArrayList<int[]> activeJobs = new ArrayList<>();

			str = receive();

			while (!(str.equals("."))) {

				String[] jobData = str.split(" ");

				// has our estimated start-time and estimated run-time, as aligned in ds-sim
				// user guide
				// - [2] = job start time
				// - [3] = estimated job run time
				int[] temp = { Integer.parseInt(jobData[2]), Integer.parseInt(jobData[3]) };

				activeJobs.add(temp);

				send(OK);

				str = receive();
			}

			// -------------------------------Finding latest completion time---------------

			int latestFinishTime = 0;

			for (int j = 0; j < activeJobs.size(); j++) {

				// Checks to see if the estimated run time ([0] + [1]) is greater than
				// finishTime
				int currentFinishTime = activeJobs.get(j)[0] + activeJobs.get(j)[1];

				if (currentFinishTime > latestFinishTime)
					latestFinishTime = currentFinishTime;

			}

			latestFinishTimes.add(latestFinishTime);

			//

		}

		// ---------------------------------------------------------------------------------------------------------------------

		// NOW WHAT??????

		int shortestWaitingIndex = 0;
		int shortestWaitingTime = latestFinishTimes.get(0) - currentJobSubmitTime; // We will cycle through it all

		// Find the server that contains the lowest waiting time
		for (int i = 0; i < latestFinishTimes.size(); i++) {

			// Obtains the difference between the submit time of the current job
			if (currentJobSubmitTime < latestFinishTimes.get(i)) {
				int estimatedWaitingTime = latestFinishTimes.get(i) - currentJobSubmitTime;

				if (estimatedWaitingTime < shortestWaitingTime) {
					shortestWaitingIndex = i;
					shortestWaitingTime = estimatedWaitingTime;
				}
			}

			else {
				shortestWaitingIndex = i;
			}

		}

		//

		return nonWaitingActiveServers.get(shortestWaitingIndex);
	}

	public int fewestWaitingJobs(ArrayList<Integer> activeServers) {
		
		int serverWithFewestJobs = activeServers.get(0); // Final index, the server with the fewest waiting job
		int minWaitingJobs = allServers.get(activeServers.get(0)).getWaitingJobs(); // 

		for (int i = 0; i < activeServers.size(); i++) {
			
			int currentWaitingJobs = allServers.get(activeServers.get(i)).getWaitingJobs();
			
			if (currentWaitingJobs < minWaitingJobs) {
				serverWithFewestJobs = activeServers.get(i);
				minWaitingJobs = allServers.get(activeServers.get(i)).getWaitingJobs(); //
			}
		}
		return serverWithFewestJobs;
	}

	public boolean canFit(SocketServer server, SocketJob job) {

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

	// Comments will go through this process using the ds-sim protocol as a
	// reference
	public static void main(String args[]) throws IOException {

		String IP = "Localhost";
		int port = 50000;
		SocketClient client = new SocketClient(IP, port);

		client.run();

	}

}
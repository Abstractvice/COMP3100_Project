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
 * # actual simulation end time: 97359, #jobs: 980 (failed 0 times) # total
 * #servers used: 20, avg util: 94.81% (ef. usage: 94.76%), total cost: $314.18
 * # avg waiting time: 1573, avg exec time: 2473, avg turnaround time: 4046
 * 
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

	// Initializes input and output byte streams
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;

	// ArrayList that stores all of the servers being used by ds-sim, in the form of SocketServer objects
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

	// Will sort a collection of SocketServer objects into ascending order according to a servers number of cores
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

	// Helper method that creates a single instance of a SocketJob object, parsing job information given to us by DS-SIM
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

	// Helper method that creates a single instance of a SocketServer object, parsing server information given to us by DS-SIM
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
	
	/**
	 * 		1. Combine a few of the methods below
	 * 		2. Include isBooting in our main body, will speed up waiting time
	 */

	// Returns a boolean based on whether a server has any waiting jobs on it
	public boolean hasWaitingJobs(SocketServer server) {
		boolean waitingJobs = false;
		if (!(server.getWaitingJobs() == 0))
			waitingJobs = true;

		return waitingJobs;
	}

	// Returns a boolean based on whether a server is inactive or not
	public boolean isInactive(SocketServer server) {
		boolean inactive = false;
		if (server.getServerState().equals("inactive"))
			inactive = true;

		return inactive;
	}

	// Returns a boolean based on whether a server is booting or not
	public boolean isBooting(SocketServer server) {
		boolean booting = false;
		if (server.getServerState().equals("booting"))
			booting = true;

		return booting;
	}

	// Returns a boolean based on whether a server is idle or not
	public boolean isIdle(SocketServer server) {
		boolean idle = false;
		if (server.getServerState().equals("idle"))
			idle = true;

		return idle;
	}

	// Returns a boolean based on whether a server is active or not
	public boolean isActive(SocketServer server) {
		boolean active = false;
		if (server.getServerState().equals("active"))
			active = true;

		return active;
	}

	// Returns a boolean based on whether a server is unavailable or not (possible redundant)
	public boolean isUnavailable(SocketServer server) {
		boolean unavailable = false;
		if (server.getServerState().equals("unavailable"))
			unavailable = true;

		return unavailable;
	}

	/**
	 * Client Scheduler
	 * 
	 * 0			1				2				3				4			5			6
	 * 
	 * JOBN: 		[submitTime] 	[jobID] 		[estRuntime] 	[cores] 	[memory] 	[disk]
	 * 
	 * type: 		[limit] 		[bootupTime] 	[hourlyRate] 	[coreCount] [memory] 	[disk]
	 * 
	 * serverType: 	[serverID] 		[state] 		[curStartTime] 	[core] 		[memory] 	[disk]
	 */
	
	public void run() throws IOException {

		// handshake
		send(HELO);
		receive();
		send(AUTH);
		receive();

		send(REDY);
		String str = receive(); // receives JOBN or NONE at first

		boolean looping = true;

		while (looping) {

			if (str.equals(NONE)) {
				looping = false;
				break;
			}

			// Prompts client to confirm it is ready for the next job (if any)
			if (str.contains(JCPL)) {
				send(REDY);
				str = receive();
			}

			if (str.contains(JOBN)) {

				String[] currentJob = str.split("\\s+");

				SocketJob job = getJob(currentJob);

				// prompts ds-sim to send us a list of capable servers
				int coreCount = job.getCore();
				int capableCore = job.getMemory();
				int availCore = job.getDisk();
				send(GETSCAPABLE + " " + coreCount + " " + capableCore + " " + availCore + "\n");
				str = receive();

				// 
				String[] dataSplit = str.split("\\s+");
				int numOfServers = Integer.parseInt(dataSplit[1]); // Number of capable servers (AKA limit)

				send(OK);

				// adds all the capable servers to our allServers arrayList, as SocketServer objects.
				for (int i = 0; i < numOfServers; i++) {
					str = receive();
					String[] serverState = str.split("\\s+");

					allServers.add(getServer(serverState));

				}

				// Ascending order by coreCount
				sortServers(allServers);

				send(OK);

				str = receive(); // contains "."

				int optimalFit = optimalFit(job);

				int jobID = Integer.parseInt(currentJob[2]);
				String serverType = allServers.get(optimalFit).getType();
				int serverID = allServers.get(optimalFit).getServerID();

				send(SCHD + " " + jobID + " " + serverType + " " + serverID + "\n");

				str = receive();
				send(REDY);
				str = receive();

				allServers.clear();
			}

		}

		send(QUIT); 
		receive(); 
		out.close();
		socket.close();

	}

	/**
	 * Basic algorithm idea: Prioritise in the following order:
	 * 
	 * 		1. Inactive servers: immediately available with no running jobs, pick first so they can boot and then go idle 
	 *      2. Idle servers: immediately available with no running jobs 
	 *      3. Active servers: possibly available 
	 *      	3.1. Store the active servers with NO waiting jobs 
	 *      	3.2. Store the active servers WITH waiting jobs in separate storage, one that has the SMALLEST WAIT TIME
	 * 
	 */
	public int optimalFit(SocketJob currentJob) throws IOException {

		// Index pointing us to the optimal server stored in allServers. What we ultimately want
		int optimalServerIndex = 0;

		ArrayList<Integer> allIndexes = new ArrayList<>();

		for (int i = 0; i < allServers.size(); i++) {
			allIndexes.add(i);
		}

		// ------- Include BOOTING for latter two arrayLists. Booting is soon to be active, should be included
		
		// Stores the indexes pointing to ALL the servers inside allServers that are INACTIVE
		ArrayList<Integer> inactiveServers = new ArrayList<>();	
		// Stores the indexes pointing to ALL the servers inside allServers that are ACTIVE with ZERO waiting jobs
		ArrayList<Integer> freeActiveServers = new ArrayList<>(); // no waiting jobs		
		// Stores the indexes pointing to ALL the servers inside allServers that are ACTIVE, not including those pointed to
		// in the above in freeActiveServers
		ArrayList<Integer> activeServersWithWaitingJobs = new ArrayList<>();
		// Stores the indexes pointing to ALL the servers inside allServers that are BOOTING
		ArrayList<Integer> bootingServers = new ArrayList<>();
		
		// Iterates through every server inside allServers, and allocates them to the above ArrayLists
		for (int i = 0; i < allServers.size(); i++) {
			
			// Ensures that the servers to be allocated at least can fit currentJob
			if (canFit(allServers.get(i), currentJob)) {

				// Best case scenario, allocate job to first idle server if one is found, skip rest of method.
				if (isIdle(allServers.get(i))) {
					return i;
					//alreadyObtainedServer = true;
					//break;
				}

				// Allocates all the inactive servers in allServers to inactiveServers ArrayList
				if (isInactive(allServers.get(i))) {
					inactiveServers.add(i);
				}

				// Allocates active servers of two different kinds
				if (isActive(allServers.get(i))) {
					// Allocates all of our active servers with NO waiting jobs to nonWaitingActiveServers arrayList
					if (!hasWaitingJobs(allServers.get(i))) {
						freeActiveServers.add(i);
					}
					// Stores the remaining active servers, the ones with at least one waiting job
					else {
						activeServersWithWaitingJobs.add(i);
					}

				}
				
				//Allocates booting servers to bootingServers ArrayList
				if (isBooting(allServers.get(i))) {
					bootingServers.add(i);
				}
			}
		}

		// Go through this block of code IF we haven't already obtained an idle server to schedule to.
		// Arranged in a priority
		// *** THIS IS THE BEST PRIORITY DO NOT CHANGE!!!!!!!!!!!!
			
		if (!freeActiveServers.isEmpty()) {
			return shortestWaitTime(freeActiveServers, currentJob.getSubmitTime()); 
		} 
			
			// 
		else if (!inactiveServers.isEmpty()) {
			return inactiveServers.get(0); 
		} 
			
		else if (!activeServersWithWaitingJobs.isEmpty()) {
			return fewestWaitingJobs(activeServersWithWaitingJobs);
		}
			
		else if (!bootingServers.isEmpty()) {
			return fewestWaitingJobs(bootingServers);
		}

		// Goes through all the remaining servers that haven't been caught above
		// Try and remove this, needlessly innefficient
		return fewestWaitingJobs(allIndexes);

	}

	// index of the server with shortest ESTIMATED waiting time if a potential new
	// job is added
	// In essence, we want to schedule our job to a server which will result in the
	// smallest amount of waiting time being added
	public int shortestWaitTime(ArrayList<Integer> nonWaitingActiveServers, int currentJobSubmitTime) throws IOException {

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

	// Returns a boolean based on whether our current job object can "fit" inside a server based on required cores
	public boolean canFit(SocketServer server, SocketJob currentJob) {

		int requiredCores = currentJob.getCore();
		int requiredMemory = currentJob.getMemory();
		int requiredDiskSpace = currentJob.getDisk();
		int serverCores = server.getCoreCount();
		int serverMemory = server.getMemory();
		int serverDiskSpace = server.getDisk();

		if (serverCores >= requiredCores && serverMemory >= requiredMemory && serverDiskSpace >= requiredDiskSpace)
			return true;

		return false;
	}

	// Comments will go through this process using the ds-sim protocol as a reference
	public static void main(String args[]) throws IOException {

		String IP = "Localhost";
		int port = 50000;
		SocketClient client = new SocketClient(IP, port);

		client.run();

	}

}
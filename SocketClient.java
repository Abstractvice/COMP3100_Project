import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SocketClient {

	// Input and output byte streams
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;

	// Stores all the parsed by readXML method
	private ArrayList<Server> allServers = new ArrayList<Server>();

	// Stores the index of the location of the largest server with respect to allServers
	private int largestServer = 0; // Stores the position of the largest server in the 2D array

	// Data structure representing a server as defined by ds-sim
	private class Server {
		String type;
		int limit;
		int bootupTime;
		float hourlyRate;
		int coreCount;
		int memory;
		int disk;

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

				allServers.add(new Server(t, l, bT, hR, cC, m, d));

				// Determines the index that contains the server with the largest coreCount
				// value
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

		// Handshake and XML parsing (Steps 1 to 4)
		client.send("HELO\n");
		client.receive();
		client.send("AUTH " + System.getProperty("user.name") + "\n");
		client.receive();
		client.readXML("ds-system.xml");

		// Step 5
		client.send("REDY\n");

		// Step 6
		String str = client.receive(); // Assumes it receives either JOBN or NONE at first

		boolean looping = true;

		while (looping) {
			// Processing possible conditional Step 6 or Step 10
			if (str.equals("NONE") || str.equals(".")) {
				looping = false;
				break;
			}
			// Prompts client to confirm it is ready for the next job (if any)
			if (str.contains("JCPL")) {
				client.send("REDY\n");
				str = client.receive();
				// Step 7: the scheduling decision is sent to the server, based directly on
				// ds-sim user guide specifications in section 7 on SCHD
			} else {
				String[] strSplit = str.split("\\s+");
				String SCHD = "SCHD";
				int jobID = Integer.parseInt(strSplit[2]);
				String serverType = client.allServers.get(client.largestServer).getType();
				String serverID = "0";

				client.send(SCHD + " " + jobID + " " + serverType + " " + serverID + "\n");

				//
				str = client.receive(); // Step 8: Server sends OK for job scheduled
				client.send("REDY\n"); // Step 9(5): Client assumes there are more jobs, so sends "REDY"
				str = client.receive(); // Step 10
			}
		}

		// Step 11 contained within the loop but too abstracted to be pointed out specifically
		
		client.send("QUIT\n"); // Step 12
		client.receive(); // Step 13
		client.out.close();
		client.socket.close();

	}

}
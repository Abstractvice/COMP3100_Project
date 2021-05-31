
public class SocketServer {

	String type;
	int serverID;
	String serverState;
	int currStartTime;
	int coreCount;
	int memory;
	int disk;

	String getType() {
		return type;
	}
	
	int getServerID() {
		return serverID;
	}
	
	String getServerState() {
		return serverState;
	}
	
	float getCurrStartTime() {
		return currStartTime;
	}
	
	int getCoreCount() {
		return coreCount;
	}
	
	int getMemory() {
		return memory;
	}
	
	int getDisk() {
		return disk;
	}

	SocketServer(String t, int sID, String sS, int cST, int cC, int m, int d) {
		this.type = t;
		this.serverID = sID;
		this.serverState = sS;
		this.currStartTime = cST;
		this.coreCount = cC;
		this.memory = m;
		this.disk = d;
	}
	
}

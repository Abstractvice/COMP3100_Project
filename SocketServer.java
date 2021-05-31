public class SocketServer {

	String type;
	int serverID;
	String serverState;
	int currStartTime;
	int coreCount;
	int memory;
	int disk;
	int waitingJobs;
	int runningJobs;

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
	
	int getWaitingJobs() {
		return waitingJobs;
	}
	
	int getRunningJobs() {
		return runningJobs;
	}

	SocketServer(String t, int sID, String sS, int cST, int cC, int m, int d, int wJ, int rJ) {
		this.type = t;
		this.serverID = sID;
		this.serverState = sS;
		this.currStartTime = cST;
		this.coreCount = cC;
		this.memory = m;
		this.disk = d;
		this.waitingJobs = wJ;
		this.runningJobs = rJ;
	}
}

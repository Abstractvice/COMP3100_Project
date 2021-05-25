
public class SocketJob {
	
	String type;
	int submitTime;
	int jobID;
	int estRuntime;
	int core;
	int memory;
	int disk;
	
	String getType() {
		return type;
	}
	
	int getSubmitTime() {
		return submitTime;
	}
	
	int getJobID() {
		return jobID;
	}
	
	int getEstRuntime() {
		return estRuntime;
	}
	
	int getCore() {
		return core;
	}
	
	int getMemory() {
		return memory;
	}
	
	int getDisk() {
		return disk;
	}
	
	SocketJob(String t, int sT, int jID, int eR, int c, int m, int d) {
		type = t;
		submitTime = sT;
		jobID = jID;
		estRuntime = eR;
		core = c;
		memory = m;
		disk = d;
	}

}

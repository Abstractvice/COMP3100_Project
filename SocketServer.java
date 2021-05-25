public class SocketServer {

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
	
	int getLimit() {
		return limit;
	}
	
	int getBootupTime() {
		return bootupTime;
	}
	
	float getHourlyRate() {
		return hourlyRate;
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

	SocketServer(String t, int l, int bT, float hR, int cC, int m, int d) {
		this.type = t;
		this.limit = l;
		this.bootupTime = bT;
		this.hourlyRate = hR;
		this.coreCount = cC;
		this.memory = m;
		this.disk = d;
	}
	
	
}

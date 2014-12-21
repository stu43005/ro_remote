package common;

public class Time {
	long server_tick = -1;
	long client_tick = -1;
	
	public void updateServerTick(long tick) {
		server_tick = tick;
		client_tick = System.currentTimeMillis();
	}
	
	public long getServerTick() {
		if (server_tick == -1)
			return -1;
		long diff = System.currentTimeMillis() - client_tick;
		return server_tick + diff;
	}
	
	public long diffServerTick(long end_tick) {
		if (server_tick == -1)
			return -1;
		return end_tick - getServerTick();
	}
}

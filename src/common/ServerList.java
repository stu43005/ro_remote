package common;

public class ServerList {
	public byte[] ip;
	public int port;
	public int serverType;
	
	public ServerList(byte[] ip, int port, int serverType) {
		this.ip = ip;
		this.port = port;
		this.serverType = serverType;
	}
	public ServerList(int[] ip, int port, int serverType) {
		this.ip = ipbyte(ip);
		this.port = port;
		this.serverType = serverType;
	}
	public ServerList(int ip1, int ip2, int ip3, int ip4, int port, int serverType) {
		this.ip = ipbyte(new int[] {ip1, ip2, ip3, ip4});
		this.port = port;
		this.serverType = serverType;
	}
	
	public static byte[] ipbyte(int[] ip) {
		byte[] newip = new byte[4];
		for(int i = 0; i < 4; i++) {
			newip[i] = (byte)ip[i];
		}
		return newip;
	}
}

package ro_bot;

import java.io.*;
import java.nio.channels.*;

import packet.Packet;
import common.ServerList;
import common.Sex;
import common.data.SessionData;

public class Login {
	RoBot bot;
	SocketChannel remote;
	SessionData sd;
	
	public Login(RoBot bot) {
		this.bot = bot;
		this.remote = bot.remote;
		this.sd = bot.sd;
	}
	
	void LoginServerCommand(Packet buf, int cmd, int packet_len) {
		switch (cmd) {
		case 0x69:
			// login ok
			System.out.println("login auth ok");
			int server_num = (packet_len - 47) / 32;
			sd.login_id1 = buf.getL(4);
			sd.account_id = buf.getL(8);
			sd.login_id2 = buf.getL(12);
			sd.sex = buf.getB(46);
			System.out.println("login information: account_id=" + sd.account_id + ", sex=" + Sex.num2str(sd.sex));
			System.out.println("server list: (num=" + server_num + ")");
			
			int i = RoBot.serverId;
			byte[] charIp = buf.getRange(47+i*32, 4);
			int charPort = buf.getW(47+i*32+4);
			System.out.println("  " + (i+1) + ". " + Packet.ip2str(charIp) + ":" + charPort);
			
			try {
				bot.connect(new ServerList(charIp, charPort, 2));
				bot.ch = new Char(bot);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 0x6a:
			// Log the result of a failed connection attempt by sd
			System.out.println("login auth failed");
			bot.isRun = false;
			break;
		case 0x81:
			int type = buf.getB(2);
			switch (type) {
			case 1:
				// Server closed
				System.out.println("Server closed");
				break;
			case 8:
				// Server still recognizes your last login
				System.out.println("Server still recognizes your last login");
				break;
			}
			bot.isRun = false;
			break;
		}
	}
	
	/**
	 * request client login (raw password)
	 * <p>
	 * S 0064 &lt;version>.L &lt;username>.24B &lt;password>.24B &lt;clienttype>.B
	 * 
	 * @param remote
	 * @throws IOException send
	 */
	public static void requestLogin(SocketChannel remote, String account, String password) throws IOException {
		Packet buf = Packet.create(55);
		buf.setW(0, 0x64);
		buf.setL(2, 0x64);
		buf.setString(6, 24, account);
		buf.setString(30, 24, password);
		buf.setB(54, 0x5);
		buf.send(remote, 55);
	}
}

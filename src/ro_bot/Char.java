package ro_bot;

import java.io.*;
import java.nio.channels.*;
import java.util.*;

import packet.Packet;
import common.ServerList;
import common.data.CharStatus;
import common.data.SessionData;

public class Char extends Thread {
	RoBot bot;
	SocketChannel remote;
	SessionData sd;
	
	public Char(RoBot bot) {
		this.bot = bot;
		this.remote = bot.remote;
		this.sd = bot.sd;
		start();
	}
	
	public void run() {
		try {
			requestConnect();
			sleep(1000);
			while(bot.isRun && bot.remoteType == 2) {
				clientKeepAlive();
				sleep(12000);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
	}

	void CharServerCommand(Packet buf, int cmd, int packet_len) {
		switch (cmd) {
		case 0x82d:
			// Notify client about charselect window data
			sd.char_slots = buf.getB(4);
			sd.MAX_CHARS = buf.getB(5) + sd.char_slots;
			sd.found_char = new long[sd.MAX_CHARS];
			sd.char_moves = new long[sd.MAX_CHARS];
			try {
				clientKeepAlive();
			} catch (IOException e1) {
			}
			buf.skip(29);
			break;
		case 0x99d:
			int chars = (packet_len - 4) / sd.MAX_CHAR_BUF;
			for(int i = 0; i < chars; i++) {
				CharStatus p = new CharStatus();
				
				p.char_id	= buf.getL(4 + i * sd.MAX_CHAR_BUF + 0);
				p.base_exp	= buf.getL(4 + i * sd.MAX_CHAR_BUF + 4);
				p.zeny		= buf.getL(4 + i * sd.MAX_CHAR_BUF + 8);
				p.job_exp	= buf.getL(4 + i * sd.MAX_CHAR_BUF + 12);
				p.job_level	= buf.getL(4 + i * sd.MAX_CHAR_BUF + 16);
				p.option	= buf.getL(4 + i * sd.MAX_CHAR_BUF + 28);
				p.karma		= buf.getL(4 + i * sd.MAX_CHAR_BUF + 32);
				p.manner	= buf.getL(4 + i * sd.MAX_CHAR_BUF + 36);
				p.status_point = buf.getW(4 + i * sd.MAX_CHAR_BUF + 40);
				p.hp		= buf.getL(4 + i * sd.MAX_CHAR_BUF + 42);
				p.max_hp	= buf.getL(4 + i * sd.MAX_CHAR_BUF + 46);
				p.sp		= buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 46);
				p.max_sp	= buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 48);
				//p.speed		= buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 50);	// DEFAULT_WALK_SPEED
				p.class_	= buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 52);
				p.hair		= buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 54);
				p.weapon	= buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 56);	// p->option&(0x20|0x80000|0x100000|0x200000|0x400000|0x800000|0x1000000|0x2000000|0x4000000|0x8000000) ? 0 : p->weapon;
				p.base_level = buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 58);
				p.skill_point = buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 60);
				p.head_bottom = buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 62);
				p.shield	= buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 64);
				p.head_top	= buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 66);
				p.head_mid	= buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 68);
				p.hair_color = buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 70);
				p.clothes_color = buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 72);
				try {
					p.name = buf.getString(4 + i * sd.MAX_CHAR_BUF + 4 + 74, 24);
				} catch (UnsupportedEncodingException e) {
					p.name = "";
				}
				p.str		= buf.getB(4 + i * sd.MAX_CHAR_BUF + 4 + 98);
				p.agi		= buf.getB(4 + i * sd.MAX_CHAR_BUF + 4 + 99);
				p.vit		= buf.getB(4 + i * sd.MAX_CHAR_BUF + 4 + 100);
				p.int_		= buf.getB(4 + i * sd.MAX_CHAR_BUF + 4 + 101);
				p.dex		= buf.getB(4 + i * sd.MAX_CHAR_BUF + 4 + 102);
				p.luk		= buf.getB(4 + i * sd.MAX_CHAR_BUF + 4 + 103);
				p.slot		= buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 104);
				p.rename	= buf.getW(4 + i * sd.MAX_CHAR_BUF + 4 + 106) == 0 ? 1 : 0;	// ( p->rename > 0 ) ? 0 : 1;
				try {
					p.last_map = buf.getString(4 + i * sd.MAX_CHAR_BUF + 4 + 108, 16);
				} catch (UnsupportedEncodingException e) {
					p.last_map = "";
				}
				p.delete_date = buf.getL(4 + i * sd.MAX_CHAR_BUF + 4 + 124);
				p.robe		= buf.getL(4 + i * sd.MAX_CHAR_BUF + 4 + 128);
				p.character_moves = buf.getL(4 + i * sd.MAX_CHAR_BUF + 4 + 132);	// change slot feature (0 = disabled, otherwise enabled)
				p.rename	= buf.getL(4 + i * sd.MAX_CHAR_BUF + 4 + 106) == 1 ? 1 : 0;	// ( p->rename > 0 ) ? 1 : 0;  (0 = disabled, otherwise displays "Add-Ons" sidebar)

				sd.found_char[p.slot] = p.char_id;
				sd.char_moves[p.slot] = p.character_moves;
				sd.chars.add(p);
			}
			try {
				charSelect(bot.charSlot);
			} catch (IOException e) {
				e.printStackTrace();
			}
			buf.skip(packet_len);
			break;
		case 0x8b9:
			// 0 = disabled / pin is correct
			// 1 = ask for pin - client sends 0x8b8
			// 2 = create new pin - client sends 0x8ba
			// 3 = pin must be changed - client 0x8be
			// 4 = create new pin - client sends 0x8ba
			// 5 = client shows msgstr(1896)
			// 6 = client shows msgstr(1897) Unable to use your KSSN number
			// 7 = char select window shows a button - client sends 0x8c5
			// 8 = pincode was incorrect
			sd.pincode_seed = buf.getL(2);
			sd.account_id = buf.getL(6);
			sd.pincode_state = buf.getW(10);
			buf.skip(12);
			break;
		case 0x71:
			// Send player to map
			System.out.println("Send player to map");
			sd.char_id = buf.getL(2);
			byte[] mapIp = buf.getRange(22, 4);
			int mapPort = buf.getW(26);
			System.out.println("char information: char_id=" + sd.char_id);
			System.out.println("map server: " + Packet.ip2str(mapIp) + ":" + mapPort);

			try {
				bot.connect(new ServerList(mapIp, mapPort, 3));
				bot.mp = new Map(bot);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		case 0x6c:
			// rejected from server
			System.out.println("rejected from server");
			bot.isRun = false;
			buf.skip(3);
			break;
		case 0x81:
			int type = buf.getB(2);
			switch (type) {
			case 1:
				// Server closed
				System.out.println("Server closed");
				break;
			case 2:
				// Someone has already logged in with this id
				System.out.println("Someone has already logged in with this id");
				break;
			case 8:
				// Character already online. KICK KICK KICK
				System.out.println("Character already online");
				break;
			}
			bot.isRun = false;
			buf.skip(3);
			break;
		default:
			if (buf.length() >= 4) {
				long longid = buf.getL(0);
				if (longid == sd.account_id) {
					buf.skip(4);
					if (buf.length() >= 2)
						CharServerCommand(buf, buf.getW(0), buf.getW(2));
				}
			}
			return;
		}
		if (buf.length() >= 2)
			CharServerCommand(buf, buf.getW(0), buf.getW(2));
	}
	
	/**
	 * request to connect
	 * <p>
	 * 0065 &lt;account id>.L &lt;login id1>.L &lt;login id2>.L &lt;???>.W &lt;sex>.B
	 * 
	 * @throws IOException send
	 */
	public void requestConnect() throws IOException {
		Packet buf = Packet.create(17);
		buf.setW(0, 0x65);
		buf.setL(2, sd.account_id);
		buf.setL(6, sd.login_id1);
		buf.setL(10, sd.login_id2);
		buf.setB(16, sd.sex);
		buf.send(remote, 17);
	}

	/**
	 * client keep-alive packet (every 12 seconds)
	 * <p>
	 * R 0187 &lt;account ID>.l
	 * 
	 * @throws IOException send
	 */
	public void clientKeepAlive() throws IOException {
		Packet buf = Packet.create(6);
		buf.setW(0, 0x187);
		buf.setL(2, sd.account_id);
		buf.send(remote, 6);
	}
	
	/**
	 * char select
	 * <p>
	 * 0066 &lt;slot>.B
	 * 
	 * @param slot
	 * @throws IOException send
	 */
	public void charSelect(int slot) throws IOException {
        Iterator<CharStatus> iter = sd.chars.iterator();
        while (iter.hasNext()) {
        	CharStatus status = (CharStatus) iter.next();
        	if (status.slot == slot) {
        		sd.status = status;
        		break;
        	}
        }
		
		Packet buf = Packet.create(3);
		buf.setW(0, 0x66);
		buf.setB(2, slot);
		buf.send(remote, 3);
	}
}

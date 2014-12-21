package packet;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Serf {
	private SocketChannel remote;
	private PacketVer packet_ver;

	public Serf(SocketChannel remote, PacketVer packet_ver) {
		this.remote = remote;
		this.packet_ver = packet_ver;
		
		if (!packet_db.readed) {
			try {
				packet_db.packetdb_readdb();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Request to connect to map-server.
	 * <p>
	 * 0072 &lt;account id>.L &lt;char id>.L &lt;auth code>.L &lt;client time>.L &lt;gender>.B (CZ_ENTER) <br>
	 * 0436 &lt;account id>.L &lt;char id>.L &lt;auth code>.L &lt;client time>.L &lt;gender>.B (CZ_ENTER2) <br>
	 * <p>
	 * There are various variants of this packet, some of them have padding between fields.
	 * 
	 * @throws IOException
	 */
	public void WantToConnection(long account_id, long char_id, long login_id1, int sex) throws IOException {
		int cmd = 0x0072;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet buf = Packet.create(info.len);
		buf.setW(0, cmd);
		buf.setL(info.pos[0], account_id);
		buf.setL(info.pos[1], char_id);
		buf.setL(info.pos[2], login_id1);
		buf.setL(info.pos[3], System.currentTimeMillis());
		buf.setB(info.pos[4], sex);
		buf.send(remote, info.len);
	}
	
	/**
	 * Notification from the client, that it has finished map loading and is about to display player's character (CZ_NOTIFY_ACTORINIT).
	 * <p>
		 * 007d
		 * 
	 * @throws IOException
	 */
	public void LoadEndAck() throws IOException {
		int cmd = 0x007d;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet buf = Packet.create(info.len);
		buf.setW(0, cmd);
		buf.send(remote, info.len);
	}

	/**
	 * Validates and processes whispered messages (CZ_WHISPER).
	 * <p>
	 * 0096 &lt;packet len>.W &lt;nick>.24B &lt;message>.?B
	 */
	public void WisMessage(String nick, String message) throws IOException {
		int cmd = 0x0096;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet buf = Packet.create(0xffff);
		buf.setW(0, cmd);
		buf.setString(info.pos[1], 24, nick);
		int len = buf.setString(info.pos[2], 0xffff-28, message);
		buf.setW(info.pos[0], 28 + len);
		buf.send(remote, 28 + len);
	}

	/**
	 * Request to use a targeted skill.
	 * <p>
	 * 0113 &lt;skill lv>.W &lt;skill id>.W &lt;target id>.L (CZ_USE_SKILL) <br>
	 * 0438 &lt;skill lv>.W &lt;skill id>.W &lt;target id>.L (CZ_USE_SKILL2)
	 * <p>
	 * There are various variants of this packet, some of them have padding between fields.
	 * 
	 * @throws IOException 
	 */
	public void UseSkillToId(int skill_id, int skill_lv, long target_id) throws IOException {
		int cmd = 0x0113;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet p = Packet.create(info.len);
		p.setW(0, cmd);
		p.setW(info.pos[0], skill_lv);
		p.setW(info.pos[1], skill_id);
		p.setL(info.pos[2], target_id);
		p.send(remote, info.len);
	}
	
	/**
	 * Request to use a ground skill.
	 * <p>
	 * 0116 &lt;skill lv>.W &lt;skill id>.W &lt;x>.W &lt;y>.W (CZ_USE_SKILL_TOGROUND) <br>
	 * 0366 &lt;skill lv>.W &lt;skill id>.W &lt;x>.W &lt;y>.W (CZ_USE_SKILL_TOGROUND2)
	 * <p>
	 * There are various variants of this packet, some of them have padding between fields.
	 * 
	 * @throws IOException 
	 */
	public void UseSkillToPos(int skill_id, int skill_lv, int pos_x, int pos_y) throws IOException {
		int cmd = 0x0366;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet p = Packet.create(info.len);
		p.setW(0, cmd);
		p.setW(info.pos[0], skill_lv);
		p.setW(info.pos[1], skill_id);
		p.setW(info.pos[2], pos_x);
		p.setW(info.pos[3], pos_y);
		p.send(remote, info.len);
	}
	
	/**
	 * Answer to map selection dialog (CZ_SELECT_WARPPOINT).
	 * <p>
	 * 011b &lt;skill id>.W &lt;map name>.16B
	 * 
	 * @throws IOException 
	 */
	public void UseSkillMap(int skill_id, String map_name) throws IOException {
		int cmd = 0x011b;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet buf1 = Packet.create(info.len);
		buf1.setW(0, cmd);
		buf1.setW(info.pos[0], skill_id);
		buf1.setString(info.pos[1], 16, map_name);
		buf1.send(remote, info.len);
	}
	
	/**
	 * Request to open a vending shop (CZ_REQ_BUY_FROMMC).
	 * <p>
	 * 0130 &lt;account id>.L
	 * 
	 * @throws IOException 
	 */
	public void VendingListReq(long owner_id) throws IOException {
		int cmd = 0x0130;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet p = Packet.create(info.len);
		p.setW(0, cmd);
		p.setL(info.pos[0], owner_id);
		p.send(remote, info.len);
	}
	
	/**
	 * Request for guild window interface permissions (CZ_REQ_GUILD_MENUINTERFACE).
	 * <p>
	 * 014d
	 * 
	 * @throws IOException 
	 */
	public void GuildCheckMaster() throws IOException {
		int cmd = 0x014d;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet buf = Packet.create(info.len);
		buf.setW(0, cmd);
		buf.send(remote, info.len);
	}
	
	/**
	 * Request for guild window information (CZ_REQ_GUILD_MENU).
	 * <p>
	 * 014f &lt;type>.L <br>
	 * type:
	 * <ol start=0>
	 *     <li>basic info </li>
	 *     <li>member manager </li>
	 *     <li>positio</li>
	 *     <li>skills </li>
	 *     <li>expulsion list </li>
	 *     <li>unknown (GM_ALLGUILDLIST) </li>
	 *     <li>notice</li>
	 * </ol>
	 * 
	 * @throws IOException 
	 */
	public void GuildRequestInfo(int type) throws IOException {
		int cmd = 0x014f;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet buf = Packet.create(info.len);
		buf.setW(0, cmd);
		buf.setL(info.pos[0], type);
		buf.send(remote, info.len);
	}
	
	/**
	 * Request to disconnect from server (CZ_REQ_DISCONNECT).
	 * <p>
	 * 018a &lt;type>.W
	 * <p>
	 * type:
	 *     0 = quit
	 * 
	 * @throws IOException 
	 */
	public void QuitGame(int type) throws IOException {
		int cmd = 0x018a;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet buf1 = Packet.create(info.len);
		buf1.setW(0, cmd);
		buf1.setW(info.pos[0], type);
		buf1.send(remote, info.len);
	}
	
	/**
	 * {@code QuitGame(0);}
	 * @see #QuitGame(int)
	 * @throws IOException
	 */
	public void QuitGame() throws IOException {
		QuitGame(0);
	}
	
	/**
	 * Request to walk to a certain position on the current map.
	 * <p>
	 * 0085 &lt;dest>.3B (CZ_REQUEST_MOVE) <br>
	 * 035f &lt;dest>.3B (CZ_REQUEST_MOVE2) <br>
	 * <p>
	 * There are various variants of this packet, some of them have padding between fields.
	 * 
	 * @throws IOException 
	 */
	public void WalkToXY(int x, int y, int dir) throws IOException {
		int cmd = 0x035f;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet buf = Packet.create(info.len);
		buf.setW(0, cmd);
		buf.setPos(info.pos[0], x, y, dir);
		buf.send(remote, info.len);
	}
	
	/**
	 * {@code WalkToXY(x, y, 0);}
	 * @see #WalkToXY(int, int, int)
	 * @throws IOException
	 */
	public void WalkToXY(int x, int y) throws IOException {
		WalkToXY(x, y, 0);
	}
	
	/**
	 * Request for server's tick.
	 * <p>
	 * 007e &lt;client tick>.L (CZ_REQUEST_TIME) <br>
	 * 0360 &lt;client tick>.L (CZ_REQUEST_TIME2) <br>
	 * <p>
	 * There are various variants of this packet, some of them have padding between fields.
	 * 
	 * @throws IOException 
	 */
	public void TickSend(long tick) throws IOException {
		int cmd = 0x0360;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet buf = Packet.create(info.len);
		buf.setW(0, cmd);
		buf.setL(info.pos[0], tick);
		buf.send(remote, info.len);
	}
	
	/**
	 * {@code TickSend(System.currentTimeMillis());}
	 * @see #TickSend(long)
	 * @throws IOException
	 */
	public void TickSend() throws IOException {
		TickSend(System.currentTimeMillis());
	}
	
	/**
	 * Requesting unit's name.
	 * <p>
	 * 0094 &lt;id>.L (CZ_REQNAME) <br>
	 * 0368 &lt;id>.L (CZ_REQNAME2) <br>
	 * <p>
	 * There are various variants of this packet, some of them have padding between fields.
	 * 
	 * @throws IOException 
	 */
	public void GetCharNameRequest(long id) throws IOException {
		int cmd = 0x0368;
		packet_db.s_packet_db info = packet_db.packet_db[packet_ver.packet_ver][cmd];
		
		Packet buf = Packet.create(info.len);
		buf.setW(0, cmd);
		buf.setL(info.pos[0], id);
		buf.send(remote, info.len);
	}
	
}

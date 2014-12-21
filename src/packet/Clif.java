package packet;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Clif {
	private SocketChannel client;

	public Clif(SocketChannel client) {
		this.client = client;
		
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
	 * Send message (modified by [Yor]) (ZC_NOTIFY_PLAYERCHAT).
	 * <p>
	 * 008e &lt;packet len>.W &lt;message>.?B
	 * 
	 * @throws IOException 
	 */
	public void displaymessage(String message) throws IOException {
		Packet p = Packet.create(260);
		int len;
		
		p.setW(0, 0x8e);
		len = p.setString(4, 255, message);
		p.setW(2, 5 + len);
		p.setB(4 + len, 0);
		p.send(client, 5 + len);
	}
	
	/**
	 * Whisper is transmitted to the destination player (ZC_WHISPER).
	 * <p>
	 * 0097 &lt;packet len>.W &lt;nick>.24B &lt;message>.?B<br>
	 * 0097 &lt;packet len>.W &lt;nick>.24B &lt;isAdmin>.L &lt;message>.?B (PACKETVER >= 20091104)
	 * 
	 * @throws IOException 
	 */
	public void wisMessage(String nick, String message) throws IOException {
		Packet p = Packet.create(0xffff);
		int len;
		
		p.setW(0, 0x97);
		p.setString(4, 24, nick);
		p.setL(28, 0);
		len = p.setString(32, 0xffff-32, message);
		p.setW(2, 32 + len);
		p.send(client, 32 + len);
	}
	
	/**
	 * Marks a position on client's minimap (ZC_COMPASS).
	 * <p>
	 * 0144 &lt;npc id>.L &lt;type>.L &lt;x>.L &lt;y>.L &lt;id>.B &lt;color>.L
	 * <p>
	 * npc id:<br>
	 *     is ignored in the client
	 * <p>
	 * type:
	 * <ol start=0>
	 *     <li>display mark for 15 seconds</li>
	 *     <li>display mark until dead or teleported</li>
	 *     <li>remove mark</li>
	 * </ol>
	 * <p>
	 * color:<br>
	 *     0x00RRGGBB
	 * 
	 * @throws IOException 
	 */
	public void viewpoint(long npc_id, long type, long x, long y, int id, long color) throws IOException {
		int cmd = 0x144;
		int len = packet_db.packet_db[0][cmd].len;
		
		Packet buf1 = Packet.create(len);
		buf1.setW(0, cmd);
		buf1.setL(2, npc_id);
		buf1.setL(6, type);
		buf1.setL(10, x);
		buf1.setL(14, y);
		buf1.setB(18, id);
		buf1.setL(19, color);
		buf1.send(client, len);
	}
}

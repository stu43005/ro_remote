package packet.event;

import java.util.*;

import packet.Packet;

public class PacketEvent extends EventObject {
	private static final long serialVersionUID = 4102494128234155216L;
	
	public int type;
	public Packet buf;
	public int cmd;
	public int packet_len;
	
	public PacketEvent(Object source, int type, Packet buf, int cmd, int packet_len) {
		super(source);
		this.type = type;
		this.buf = buf;
		this.cmd = cmd;
		this.packet_len = packet_len;
	}
}